package me.akainth.ambient.ui;

import com.intellij.ui.TreeUIHelper;
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
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * An interface component that updates a tree view with data from a remote source
 *
 * @author akainth
 */
public class SyncedTreeView<T> {
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private JTextField sourceInput;
    private Tree tree;
    @SuppressWarnings("unused")
    private JPanel root;
    private JLabel sourceInputLabel;
    private final DocumentInterpreter<T> interpreter;
    private final List<Listener> confirmationListeners = new ArrayList<>();

    private Thread updateThread = null;

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
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree);

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
        for (Listener listener : confirmationListeners) {
            listener.run(tree.getSelectedNodes(TreeNode.class, null));
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

    public void addConfirmationListener(Listener listener) {
        confirmationListeners.add(listener);
    }

    private void updatePreview() {
        if (updateThread != null) {
            updateThread.interrupt();
        }
        Runnable runnable = () -> {
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

                InputStream documentStream = new URI(sourceInput.getText()).toURL().openStream();
                Document document = documentBuilder.parse(documentStream);

                TreeModel model = interpreter.interpret(document);
                tree.setModel(model);
                EventQueue.invokeLater(() -> {
                    for (int i = 0; i < tree.getRowCount(); i++) {
                        tree.expandRow(i);
                    }
                });
            } catch (IOException | ParserConfigurationException | SAXException | URISyntaxException |
                     IllegalArgumentException e) {
                clearTree();
            }
        };
        updateThread = new Thread(runnable);
        updateThread.start();
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

    public interface Listener {
        void run(TreeNode[] selectedNodes);
    }
}
