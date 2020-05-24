package me.akainth.ambient.module

import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleBuilderListener
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import me.akainth.ambient.snarf.Package
import me.akainth.ambient.snarf.SnarfTask

/**
 * The module that will appear in the New Project and New Module dialogs.
 *
 * @author akainth
 */
class SnarfModuleBuilder : JavaModuleBuilder(), ModuleBuilderListener {
    lateinit var snarfPackage: Package

    init {
        addListener(this)
    }

    override fun getPresentableName(): String = "Snarf Package"

    override fun getBuilderId(): String = "me.akainth.ambient.module.SnarfModuleBuilder"

    override fun getDescription() = "Import a package from a Snarf site as a standard Java project"

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> = arrayOf(SnarfModuleWizardStep(this, wizardContext))

    override fun moduleCreated(module: Module) {
        // Add the module directory as a sources root
        val snarfTask = SnarfTask(module, snarfPackage, module.rootManager.contentEntries.first())
        ProgressManager.getInstance().run(snarfTask)
    }
}