package me.akainth.ambient.module

import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleBuilderListener
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.VfsUtil
import me.akainth.ambient.snarf.EntryImporter
import me.akainth.ambient.snarf.Package
import java.io.IOException

/**
 * The module that will appear in the New Project and New Module dialogs.
 */
class SnarfModuleBuilder : JavaModuleBuilder(), ModuleBuilderListener {
    lateinit var snarfPackage: Package

    init {
        addListener(this)
    }

    override fun getPresentableName(): String {
        return "Snarf Package"
    }

    override fun getBuilderId(): String? {
        return "me.akainth.ambient.module.SnarfModuleBuilder"
    }

    override fun getDescription(): String {
        return "Import a package from a Snarf site as a standard Java project"
    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {
        return arrayOf(
            SnarfModuleWizardStep(this, wizardContext)
        )
    }

    override fun moduleCreated(module: Module) = try {
        val rootManager = module.rootManager

        WriteAction.run<Exception> {
            val importPackageTask = Runnable {
                val importer = EntryImporter(rootManager.contentRoots[0], rootManager.sourceRoots[0])
                    .addExclusionPattern("\\.classpath")
                    .addExclusionPattern("\\.project")
                    .addExclusionPattern(".+\\.class")
                    .addSourcePattern(".+\\.java")
                importer.listeners += { (_, file) ->
                    if (file.extension == "jar") {
                        val jarIoFile = VfsUtil.virtualToIoFile(file)
                        val jarFileUrl = VfsUtil.getUrlForLibraryRoot(jarIoFile)
                        ModuleRootModificationUtil.addModuleLibrary(
                            module,
                            file.nameWithoutExtension,
                            listOf(
                                jarFileUrl
                            ),
                            emptyList()
                        )
                    }
                }
                importer.snarf(snarfPackage)
            }
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                importPackageTask,
                "Importing ${snarfPackage.name}",
                false,
                module.project
            )
        }
    } catch (e: IOException) {
        e.printStackTrace()
        val importFailed = Notification(
            "Ambient.Snarf",
            "Failed to import ${snarfPackage.name}",
            "Ambient could not import ${snarfPackage.name} from its <a href=\"${snarfPackage.entry}\">source</a>",
            NotificationType.ERROR
        )
        Notifications.Bus.notify(importFailed, module.project)
    }
}