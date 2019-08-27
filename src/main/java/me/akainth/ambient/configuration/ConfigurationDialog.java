package me.akainth.ambient.configuration;

import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigurationDialog extends DialogWrapper {

    private final ConfigurationService configurationService = ConfigurationService.Companion.getInstance();
    private JPanel panel;
    private JTextField snarfServerUrl;
    private JTextField webCatRoot;
    private JTextField username;
    private JPasswordField password;
    public ConfigurationDialog() {
        super(false);
        init();

        setResizable(false);
        setTitle("Ambient Configuration");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        snarfServerUrl.setText(configurationService.getSnarfSiteUrl());
        webCatRoot.setText(configurationService.getWebCatRoot());
        Credentials credentials = configurationService.getCredentials();
        if (credentials != null) {
            username.setText(credentials.getUserName());
            password.setText(credentials.getPasswordAsString());
        }
        return panel;
    }

    @Override
    protected void doOKAction() {
        configurationService.setSnarfSiteUrl(snarfServerUrl.getText());
        configurationService.setWebCatRoot(webCatRoot.getText());
        configurationService.setCredentials(new Credentials(username.getText(), password.getPassword()));
        close(0, true);
    }
}