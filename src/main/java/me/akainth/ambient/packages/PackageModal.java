package me.akainth.ambient.packages;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleTypeId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackageModal extends DialogWrapper {
    private Package myPackage;
    private Project project;
    private JLabel moduleName;
    private JLabel publisher;
    private JLabel version;
    private JLabel description;
    private JPanel panel;
    PackageModal(Project project, Package packageData) {
        super(false);
        init();

        myPackage = packageData;
        this.project = project;

        moduleName.setText(packageData.getName());
        publisher.setText(packageData.getPublisher());
        version.setText(packageData.getVersion());
        description.setText(packageData.getDescription());
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        // Create the module in the IntelliJ project
        String basePath = project.getBasePath();
        if (basePath == null) {
            throw new IllegalArgumentException("The project selected does not exist on the file system");
        }
        Runnable createModule = () -> {
            File moduleDirectory = new File(String.format("%s/%s/", basePath, myPackage.getName()));
            moduleDirectory.mkdirs();
            Module module = ModuleManager.getInstance(project)
                    .newModule(
                            moduleDirectory.getPath() + "/" + myPackage.getName() + ".iml",
                            ModuleTypeId.JAVA_MODULE
                    );

            try {
                // Download the ZIP file and stream it into the directory
                Request request = new Request.Builder()
                        .url(myPackage.getEntryUrl())
                        .build();
                Response response = new OkHttpClient().newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException(String.format("Could not download the package %s from %s", myPackage.getName(), myPackage.getEntryUrl()));
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new IllegalStateException("The response from the server did not have a body");
                }

                ZipInputStream jarStream = new ZipInputStream(body.byteStream());
                ZipEntry zipEntry = jarStream.getNextEntry();

                new File(moduleDirectory.getPath() + "/src").mkdir();

                while (zipEntry != null) {
                    if (zipEntry.isDirectory()) {
                        new File(moduleDirectory.getPath() + "/" + zipEntry.getName()).mkdir();
                    } else {
                        String[] exclusions = new String[]{".classpath", ".project"};
                        String name = zipEntry.getName();
                        if (ArrayUtils.indexOf(exclusions, name) == -1) {
                            if (name.endsWith(".java")) {
                                name = "src/" + name;
                            }
                            Path outputPath = FileSystems.getDefault().getPath(moduleDirectory.getPath(), name);
                            Files.copy(jarStream, outputPath);
                        }
                        jarStream.closeEntry();
                    }
                    zipEntry = jarStream.getNextEntry();
                }

                jarStream.close();
            } catch (IOException e) {
                Notification notification = new Notification(
                        "Ambient.PackageImport",
                        myPackage.getName(),
                        e.getLocalizedMessage(),
                        NotificationType.ERROR
                );
                Notifications.Bus.notify(notification);
            }

            ModifiableRootModel modifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel();

            String modulePath = "file://" + moduleDirectory.getAbsolutePath().replace("\\", "/");
            ContentEntry entry = modifiableRootModel.addContentEntry(modulePath);
            entry.addSourceFolder(modulePath + "/src", false);

            modifiableRootModel.inheritSdk();

            modifiableRootModel.commit();
        };
        WriteCommandAction.runWriteCommandAction(project, createModule);
    }
}
