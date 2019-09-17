package me.akainth.ambient.snarf

import com.intellij.openapi.vfs.VirtualFile
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class EntryImporter(
    private val contentRoot: VirtualFile,
    private val sourceRoot: VirtualFile
) {

    private val excludes = arrayListOf<Regex>()
    private val sources = arrayListOf<Regex>()

    val listeners = mutableSetOf<(Pair<ZipEntry, VirtualFile>) -> Any>()

    /**
     * Add a regex that specifies which files will not be imported
     */
    fun addExclusionPattern(blob: String): EntryImporter {
        excludes += blob.toRegex()
        return this
    }

    /**
     * Add a regex that specifies which files will be added to the source root
     */
    fun addSourcePattern(blob: String): EntryImporter {
        sources += blob.toRegex()
        return this
    }

    /**
     * Import the specified package into the root directory
     */
    fun snarf(snarfPackage: Package): EntryImporter {
        val inputStream = URL(snarfPackage.entry).openStream()
        val zipInputStream = ZipInputStream(inputStream)

        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            if (!zipEntry.isDirectory && !excludes.any { zipEntry.name.matches(it) }) {
                val created = createFile(zipEntry, zipInputStream)
                listeners.forEach { it(created) }
            }

            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        return this
    }

    private fun createFile(entry: ZipEntry, zipInputStream: ZipInputStream): Pair<ZipEntry, VirtualFile> {
        val parts = entry.name.split("/")
        val root = when {
            sources.any { it.matches(entry.name) } -> sourceRoot
            else -> contentRoot
        }
        val parent = parts.dropLast(1)
            .fold(root) { path, part ->
                path.findChild(part) ?: path.createChildDirectory(this, part)
            }
        val file = parent.findOrCreateChildData(this, parts.last())
        val outputStream = file.getOutputStream(this)
        zipInputStream.copyTo(outputStream)
        outputStream.close()

        return Pair(entry, file)
    }
}