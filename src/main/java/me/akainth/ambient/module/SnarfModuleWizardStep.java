package me.akainth.ambient.module;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import me.akainth.ambient.snarf.Package;
import me.akainth.ambient.snarf.SnarfSite;
import me.akainth.ambient.ui.SyncedTreeView;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * Allows a user to configure which package they will import from a snarf site
 *
 * @author akainth
 */
public class SnarfModuleWizardStep extends ModuleWizardStep {
    private static final String SNARF_SITE = "Ambient Snarf Site";
    private JPanel root;
    private SyncedTreeView<SnarfSite> packagesPreview;
    private final SnarfModuleBuilder moduleBuilder;
    private final WizardContext wizardContext;

    public SnarfModuleWizardStep(SnarfModuleBuilder moduleBuilder, WizardContext wizardContext) {
        this.moduleBuilder = moduleBuilder;
        this.wizardContext = wizardContext;

        packagesPreview.getTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        packagesPreview.addConfirmationListener(selectedNodes -> wizardContext.requestNextStep());
    }

    @Override
    public JComponent getComponent() {
        return root;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return packagesPreview.getTree();
    }

    private Package getSelectedPackage() {
        return (Package) packagesPreview.getTree()
                .getSelectedNodes(DefaultMutableTreeNode.class, node -> node.getUserObject() instanceof Package)[0]
                .getUserObject();
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (packagesPreview.getTree().getSelectionCount() != 1) {
            throw new ConfigurationException("Choose a package to import from the snarf site");
        }
        return true;
    }

    @Override
    public void updateDataModel() {
        PropertiesComponent.getInstance().setValue(SNARF_SITE, packagesPreview.getSourceUrl());
        moduleBuilder.setSnarfPackage(getSelectedPackage());
        wizardContext.setDefaultModuleName(getSelectedPackage().getName());
    }

    private void createUIComponents() {
        packagesPreview = new SyncedTreeView<>(
                "Snarf Site", PropertiesComponent.getInstance().getValue(SNARF_SITE), new SyncedTreeView.DocumentInterpreter<SnarfSite>() {
            private SnarfSite myModel;

            @Override
            public SnarfSite getModel() {
                return myModel;
            }

            @Override
            public TreeModel interpret(Document document) {
                myModel = new SnarfSite(document);
                return myModel.getTreeModel();
            }
        });
    }
}
