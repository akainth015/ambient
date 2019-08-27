package me.akainth.ambient

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import me.akainth.ambient.packages.AssignmentBrowser

class ToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val assignmentBrowser = AssignmentBrowser(project)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val assignmentBrowserContent = contentFactory.createContent(assignmentBrowser, "Assignments", false)
        toolWindow.contentManager.addContent(assignmentBrowserContent)
    }
}