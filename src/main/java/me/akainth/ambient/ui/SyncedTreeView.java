package me.akainth.ambient.ui;

import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.sun.istack.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An interface component that updates a tree view with data from a remote source
 */
public class SyncedTreeView<T> {
    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private JTextField sourceInput;
    private Tree tree;
    @SuppressWarnings("unused")
    private JPanel root;
    private JLabel sourceInputLabel;
    private DocumentInterpreter<T> interpreter;
    private List<ConfirmationListener> confirmationListeners = new ArrayList<>();

    /**
     * Creates a tree view with a label, input field, and the specified document interpreter
     *
     * @param label       text that appears next to the URL input field
     * @param defaultText the text that populates the input field by default
     * @param interpreter transforms a given XML document into a TreeModel for display
     */
    public SyncedTreeView(String label, @Nullable String defaultText, DocumentInterpreter<T> interpreter) {
        this.interpreter = interpreter;

        sourceInputLabel.setText(label);
        sourceInput.setText(defaultText);
        clearTree();
        updatePreview();

        tree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    runAllConfirmationListeners();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        new TreeSpeedSearch(tree);

        sourceInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview();
            }
        });
    }

    private void runAllConfirmationListeners() {
        for (ConfirmationListener listener : confirmationListeners) {
            listener.onConfirmation(tree.getSelectedNodes(TreeNode.class, null));
        }
    }

    /**
     * Sets the preview's model to null, causing it to show the default empty text
     */
    private void clearTree() {
        tree.setModel(null);
    }

    /**
     * Can be used to find out additional information from the document that isn't part of the tree model
     *
     * @return the {@link DocumentInterpreter} that converts the documents to {@link TreeModel}
     */
    public DocumentInterpreter<T> getInterpreter() {
        return interpreter;
    }

    /**
     * Configuration for the tree should be performed through this, such as limiting selection to one node
     *
     * @return the tree that is used to show the result of the interpretation
     */
    public Tree getTree() {
        return tree;
    }

    /**
     * Can be used to persist the source url across sessions
     *
     * @return the string currently in the source input box
     */
    public String getSourceUrl() {
        return sourceInput.getText();
    }

    public void addConfirmationListener(ConfirmationListener listener) {
        confirmationListeners.add(listener);
    }

    private void updatePreview() {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            InputStream documentStream = new URL(sourceInput.getText()).openStream();
            Document document = documentBuilder.parse(documentStream);

            TreeModel model = interpreter.interpret(document);
            tree.setModel(model);
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            clearTree();
        }
    }

    /**
     * Interprets XML documents according to their implementation
     */
    public interface DocumentInterpreter<T> {
        T getModel();

        /**
         * Transforms an XML document into a TreeModel for display
         *
         * @param document the document as parsed from the remote source
         * @return a {@link TreeModel} that can be shown in a {@link JTree}
         */
        TreeModel interpret(Document document);
    }

    public interface ConfirmationListener {
        void onConfirmation(TreeNode[] selectedNodes);
    }
}
