package me.akainth.ambient.configuration

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ConfigureAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        ConfigurationDialog().show()
    }
}
