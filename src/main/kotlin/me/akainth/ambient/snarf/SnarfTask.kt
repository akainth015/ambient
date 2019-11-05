package me.akainth.ambient.snarf

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class SnarfTask(
    private val module: Module,
    private val snarfPackage: Package,
    private val contentEntry: ContentEntry
) :
    Task.Backgroundable(module.project, "Snarfing ${snarfPackage.name}", false) {
    private val defaultFs = FileSystems.getDefault()

    override fun run(indicator: ProgressIndicator) {
        val filesToSkip = arrayOf(".classpath", ".project", "**/*.class").map { glob ->
            defaultFs.getPathMatcher("glob:$glob")
        }
        val jarFilePattern = defaultFs.getPathMatcher("glob:*.jar")
        val inputStream = URL(snarfPackage.entry).openStream()
        val zipInputStream = ZipInputStream(inputStream)

        WriteCommandAction.runWriteCommandAction(module.project) {
            var zipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                val path = Path.of(zipEntry.name)
                if (!zipEntry.isDirectory && filesToSkip.none { it.matches(path) }) {
                    val file = createFile(zipEntry, zipInputStream)
                    if (jarFilePattern.matches(path)) {
                        val jarIoFile = VfsUtil.virtualToIoFile(file)
                        val jarFileUrl = VfsUtil.getUrlForLibraryRoot(jarIoFile)
                        ModuleRootModificationUtil.addModuleLibrary(
                            module, file.nameWithoutExtension, listOf(jarFileUrl), emptyList()
                        )
                    }
                }
                zipEntry = zipInputStream.nextEntry
            }
            zipInputStream.close()
        }
    }

    private fun createFile(entry: ZipEntry, zipInputStream: ZipInputStream): VirtualFile {
        val parts = entry.name.split("/")
        val initial = when (entry.name.endsWith(".java")) {
            true -> contentEntry.sourceFolderFiles.first()
            false -> contentEntry.file ?: throw IllegalStateException("The configured content entry does not exist")
        }
        val parent = parts.dropLast(1)
            .fold(initial) { path, part ->
                path.findChild(part) ?: path.createChildDirectory(this, part)
            }
        val file = parent.findOrCreateChildData(this, parts.last())
        val outputStream = file.getOutputStream(this)
        zipInputStream.copyTo(outputStream)
        outputStream.close()

        return file
    }
}