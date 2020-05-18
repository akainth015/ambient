package me.akainth.ambient.actions

import com.intellij.codeInsight.actions.DirectoryFormattingOptions
import com.intellij.codeInsight.actions.ReformatCodeAction
import com.intellij.codeInsight.actions.TextRangeType
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.SearchScope
import me.akainth.ambient.submitter.Assignment
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.attribute.FileTime
import java.util.*
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Action that attempts to resolve the current module, allows the user to choose where to confirm the action, then
 * compiles, formats, packages, and submits the module
 */
class SubmitAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        try {
            // Resolve the module being submitted automatically
            val module = ModuleUtil.findModuleForFile(e.getData(LangDataKeys.PSI_FILE))
                ?: e.getData(LangDataKeys.MODULE)
                ?: e.project?.let { project ->
                    ModuleManager.getInstance(project).modules.firstOrNull()
                }
                ?: throw IllegalStateException("There are no modules in this project")

            val submissionConfirmationDialog = SubmissionConfirmationDialog(e.project, module)
            if (submissionConfirmationDialog.showAndGet()) {
                // Persist the username and password used for submission
                PropertiesComponent.getInstance()
                    .setValue(SUBMISSION_ROOT, submissionConfirmationDialog.assignmentPicker.sourceUrl)
                val credentials = Credentials(
                    submissionConfirmationDialog.username.text,
                    submissionConfirmationDialog.password.password
                )
                PasswordSafe.instance.set(credentialAttributes, credentials)

                val project = module.project
                // Reformat the project
                if (submissionConfirmationDialog.reformatCheckBox.isSelected) {
                    reformat(
                        project,
                        submissionConfirmationDialog.target,
                        submissionConfirmationDialog.rearrangeCheckBox.isSelected,
                        submissionConfirmationDialog.optimizeImportsCheckBox.isSelected
                    )
                }

                // Compile the project, and submit if there are no errors
                CompilerManager.getInstance(project).compile(module) { aborted, errors, _, _ ->
                    if (!aborted && errors == 0) {
                        val excludes =
                            submissionConfirmationDialog.assignmentPicker.interpreter.model.excludes +
                                    submissionConfirmationDialog.assignment.excludes
                        val archive = archive(module, excludes)
                        submitTo(
                            submissionConfirmationDialog.assignment,
                            credentials,
                            archive,
                            submissionConfirmationDialog.partnersTextField.text,
                            project
                        )
                    }
                }
            }
        } catch (exception: IllegalStateException) {
            val notification = Notification(
                "Ambient.Submit",
                "Could Not Submit Assignment",
                exception.message ?: "An unknown error occurred",
                NotificationType.ERROR
            )
            Notifications.Bus.notify(notification, e.project)
        }
    }

    /**
     * Generate a JAR file with all of the files within the module source roots
     */
    private fun archive(module: Module, excludes: Array<String>): File {
        val archive = File.createTempFile(module.name, ".jar")
        val jarOutputStream = JarOutputStream(archive.outputStream())
        val sourceFilesToInclude = LinkedList<VirtualFile>()
        module.rootManager.sourceRoots.forEach {
            VfsUtil.collectChildrenRecursively(it).forEach { sourceFile ->
                sourceFilesToInclude += sourceFile
            }
        }
        sourceFilesToInclude
            .filter { !it.isDirectory }
            .filter { !excludes.contains(it.name) }
            .forEach { file ->
                val entry = ZipEntry(file.name)
                entry.lastModifiedTime = FileTime.fromMillis(file.modificationStamp)
                jarOutputStream.putNextEntry(entry)
                val inputStream = file.inputStream
                inputStream.copyTo(jarOutputStream)
                inputStream.close()
            }
        jarOutputStream.close()
        return archive
    }

    private fun reformat(project: Project, module: Module, rearrange: Boolean, optimizeImports: Boolean) {
        module.rootManager.sourceRoots.forEach { sourceRoot ->
            ReformatCodeAction.reformatDirectory(
                project,
                PsiManager.getInstance(project).findDirectory(sourceRoot)!!,
                object : DirectoryFormattingOptions {
                    override fun getTextRangeType() = TextRangeType.WHOLE_FILE

                    override fun getSearchScope(): SearchScope? = null

                    override fun isOptimizeImports() = optimizeImports

                    override fun isIncludeSubdirectories() = true

                    override fun isRearrangeCode() = rearrange

                    override fun getFileTypeMask(): String? = null
                }
            )
        }
    }

    private fun submitTo(
        assignment: Assignment,
        credentials: Credentials,
        archive: File,
        partners: String,
        project: Project
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        val username = credentials.userName!!
        val password = credentials.password!!
        assignment.transport.params.forEach { (name, paramValue) ->
            val value = when (paramValue) {
                "\${user}" -> username
                "\${pw}" -> password.toString()
                "\${partners}" -> partners
                else -> paramValue
            }
            requestBody.addFormDataPart(name, value)
        }
        requestBody.addFormDataPart("file1", "$username.jar", archive.asRequestBody())

        val submission = Request.Builder()
            .url(assignment.transport.uri)
            .post(requestBody.build())
            .build()

        val submissionResponse = OkHttpClient().newCall(submission).execute()
        submissionResponse.body?.let { body ->
            val result = "<a href=\"(.+)\">click here to view them.</a>".toRegex().find(body.string())
            if (result != null) {
                val viewSubmissionUrl = result.groupValues[1]
                Desktop.getDesktop().browse(URI(viewSubmissionUrl))
            } else {
                val notification = Notification(
                    "Ambient.Submit",
                    "Submission failed",
                    "There was an unexpected response from WebCAT. Please make sure your username and password are correct",
                    NotificationType.ERROR
                )
                Notifications.Bus.notify(notification, project)
            }
        }
    }

    companion object {
        val credentialAttributes = CredentialAttributes("Ambient.Configuration")

        const val SUBMISSION_ROOT = "SUBMISSION_ROOT"
    }
}