package me.akainth.ambient.actions;

import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TreeSpeedSearch;
import me.akainth.ambient.submitter.Assignment;
import me.akainth.ambient.submitter.SubmissionRoot;
import me.akainth.ambient.ui.SyncedTreeView;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * Shows the user information about what they are submitting, and to which assignment. They can either confirm or cancel
 * the submission, as well as edit the source and target.
 *
 * @author akainth
 */
public class SubmissionConfirmationDialog extends DialogWrapper {
    private JPanel root;
    private JComboBox<String> moduleBox;
    private SyncedTreeView<SubmissionRoot> assignmentPicker;
    private JTextField username;
    private JPasswordField password;
    private JCheckBox reformatCheckBox;
    private JCheckBox reorganizeCheckBox;
    private JCheckBox optimizeImportsCheckBox;
    private JTextField partnersTextField;

    private Module target;

    public SubmissionConfirmationDialog(Project project, Module target) {
        super(project);

        this.target = target;

        Module[] modules = ModuleManager.getInstance(project).getModules();
        String[] moduleNames = new String[modules.length];
        for (int i = 0; i < moduleNames.length; i++) {
            moduleNames[i] = modules[i].getName();
        }

        moduleBox.addItemListener(itemEvent -> this.target = ModuleManager.getInstance(project).findModuleByName((String) itemEvent.getItem()));
        moduleBox.setModel(new DefaultComboBoxModel<>(moduleNames));
        moduleBox.setSelectedItem(target.getName());

        assignmentPicker.addConfirmationListener(selectedNodes -> close(OK_EXIT_CODE, true));

        reformatCheckBox.addChangeListener(changeEvent -> {
            if (reformatCheckBox.isSelected()) {
                reorganizeCheckBox.setEnabled(true);
                optimizeImportsCheckBox.setEnabled(true);
            } else {
                reorganizeCheckBox.setSelected(false);
                reorganizeCheckBox.setEnabled(false);
                optimizeImportsCheckBox.setSelected(false);
                optimizeImportsCheckBox.setEnabled(false);
            }
        });

        Credentials credentials = PasswordSafe.getInstance().get(SubmitAction.Companion.getCredentialAttributes());
        if (credentials != null) {
            username.setText(credentials.getUserName());
            partnersTextField.setText(credentials.getUserName());
            password.setText(credentials.getPasswordAsString());
        }

        assignmentPicker.getTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        new TreeSpeedSearch(assignmentPicker.getTree());

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    private void createUIComponents() {
        assignmentPicker = new SyncedTreeView<>("Assignment Source", PropertiesComponent.getInstance().getValue(SubmitAction.SUBMISSION_ROOT, "http://"), new SyncedTreeView.DocumentInterpreter<SubmissionRoot>() {
            private SubmissionRoot model;

            @Override
            public SubmissionRoot getModel() {
                return model;
            }

            @Override
            public TreeModel interpret(Document document) {
                model = new SubmissionRoot(document);
                return model.buildTreeModel();
            }
        });
    }

    public SyncedTreeView<SubmissionRoot> getAssignmentPicker() {
        return assignmentPicker;
    }

    public Assignment getAssignment() {
        return (Assignment) assignmentPicker.getTree()
                .getSelectedNodes(DefaultMutableTreeNode.class, node -> node.getUserObject() instanceof Assignment)[0]
                .getUserObject();
    }

    public Module getTarget() {
        return target;
    }

    public JTextField getUsername() {
        return username;
    }

    public JPasswordField getPassword() {
        return password;
    }

    public JCheckBox getReformatCheckBox() {
        return reformatCheckBox;
    }

    public JCheckBox getRearrangeCheckBox() {
        return reorganizeCheckBox;
    }

    public JCheckBox getOptimizeImportsCheckBox() {
        return optimizeImportsCheckBox;
    }

    public JTextField getPartnersTextField() {
        return partnersTextField;
    }
}
