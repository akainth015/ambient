package me.akainth.ambient.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtil

/**
 * Action that attempts to resolve the current module, allows the user to choose where to confirm the action, then
 * compiles, formats, packages, and submits the module
 */
class SubmitAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        try {
            val module = ModuleUtil.findModuleForFile(e.getData(LangDataKeys.PSI_FILE))
                ?: e.getData(LangDataKeys.MODULE)
                ?: e.project?.let { project ->
                    ModuleManager.getInstance(project).modules.firstOrNull()
                }
                ?: throw IllegalStateException("There are no modules in this project")
            val submissionConfirmationDialog = SubmissionConfirmationDialog(e.project, module)
            submissionConfirmationDialog.showAndGet()
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
}