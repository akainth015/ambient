package me.akainth.ambient.submitter

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.ui.Messages
import com.jetbrains.rd.util.printlnError
import me.akainth.ambient.configuration.ConfigurationDialog
import me.akainth.ambient.configuration.ConfigurationService
import java.io.File
import java.io.IOException

class SubmitAction : AnAction() {
    private val configurationService = ConfigurationService.instance

    override fun actionPerformed(e: AnActionEvent) {
        val credentials = configurationService.credentials

        if (credentials == null) {
            val updatedConfiguration = ConfigurationDialog().showAndGet()
            if (updatedConfiguration) {
                actionPerformed(e)
                return
            } else {
                val notification = Notification(
                    "Ambient.Submit",
                    "Submit Assignment",
                    "Cannot submit an assignment without providing credentials",
                    NotificationType.ERROR
                )
                Notifications.Bus.notify(notification)
            }
        } else {
            val modules = ModuleManager.getInstance(e.project!!).modules
            if (modules.isEmpty()) {
                val notification = Notification(
                    "Ambient.Submit",
                    "Submit Assignment",
                    "Cannot submit an assignment without a module",
                    NotificationType.ERROR
                )
                Notifications.Bus.notify(notification)
            }
            val chosenModule = Messages.showEditableChooseDialog(
                "Choose a module to submit",
                "Submit Assignment",
                null,
                modules.map { it.name }.toTypedArray(),
                modules[0].name,
                null
            )
            val module = e.getData(LangDataKeys.MODULE) ?: modules.find {
                it.name == chosenModule
            } as Module

            try {
                val file = File.createTempFile(credentials.userName.toString(), ".jar")
                val process = ProcessBuilder(
                    """"${module.rootManager.sdk?.homePath}/bin/jar" cvf "${file.absolutePath}" "${module.rootManager.sourceRootUrls.map {
                        it.replace(
                            "file:/",
                            ""
                        )
                    }.joinToString(
                        " "
                    )}""""
                ).directory(File(module.moduleFilePath).parentFile).inheritIO().start()
                if (process.waitFor() == 0) {
                    try {
                        PostSubmission(credentials, file)
                    } catch (e: IllegalArgumentException) {
                        val notification = Notification(
                            "Ambient.Submit",
                            "Submit Assignment",
                            "Could not fetch a list of assignments. Check your Ambient configuration",
                            NotificationType.ERROR
                        )
                        Notifications.Bus.notify(notification)
                    }
                } else {
                    val notification = Notification(
                        "Ambient.Submit",
                        "Submit Assignment",
                        "Could not build a JAR from module ${module.name}",
                        NotificationType.ERROR
                    )
                    Notifications.Bus.notify(notification)
                }
            } catch (e: IOException) {
                printlnError(e.localizedMessage)
                val notification = Notification(
                    "Ambient.Submit",
                    "Submit Assignment",
                    "Please choose an SDK with a JARchiver (jar.exe)",
                    NotificationType.ERROR
                )
                Notifications.Bus.notify(notification)
            }
        }
    }
}