package me.akainth.ambient.actions;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import me.akainth.ambient.submitter.Assignment;
import me.akainth.ambient.submitter.SubmissionRoot;
import me.akainth.ambient.ui.SyncedTreeView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 * Shows the user information about what they are submitting, and to which assignment. They can either confirm or cancel
 * the submission, as well as edit the source and target.
 *
 * @author akain
 */
public class SubmissionConfirmationDialog extends DialogWrapper {
    private JPanel root;
    private JComboBox<String> moduleBox;
    private SyncedTreeView assignmentPicker;

    private Module target;

    public SubmissionConfirmationDialog(Project project, Module target) {
        super(project);

        this.target = target;

        Module[] modules = ModuleManager.getInstance(project).getModules();
        String[] moduleNames = new String[modules.length];
        for (int i = 0; i < moduleNames.length; i++) {
            moduleNames[i] = modules[i].getName();
        }

        moduleBox.setModel(new DefaultComboBoxModel<>(moduleNames));
        moduleBox.setSelectedItem(target.getName());

        assignmentPicker.getTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    private void createUIComponents() {
        assignmentPicker = new SyncedTreeView("Assignment Source", null, document -> new SubmissionRoot(document).buildTreeModel());
    }

    public Assignment getAssignment() {
        return (Assignment) assignmentPicker.getTree()
                .getSelectedNodes(DefaultMutableTreeNode.class, node -> node.getUserObject() instanceof Assignment)[0]
                .getUserObject();
    }

    public Module getTarget() {
        return target;
    }
}
