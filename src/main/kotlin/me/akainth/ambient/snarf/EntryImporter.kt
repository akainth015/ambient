package me.akainth.ambient.snarf

import com.intellij.openapi.vfs.VirtualFile
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class EntryImporter(var root: VirtualFile) {

    private val excludes = arrayListOf<Regex>()

    /**
     * Add a regex that specifies which files will not be imported
     */
    fun addExclusionPattern(blob: String): EntryImporter {
        excludes += blob.toRegex()
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
            if (excludes.any { zipEntry.name.matches(it) }) {
                zipEntry = zipInputStream.nextEntry
                continue
            }
            if (!zipEntry.isDirectory) {
                createFile(zipEntry, zipInputStream)
            }

            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        return this
    }

    private fun createFile(entry: ZipEntry, zipInputStream: ZipInputStream) {
        val parts = entry.name.split("/")
        val parent = parts.dropLast(1).fold(root) { path, part ->
            path.findChild(part) ?: path.createChildDirectory(this, part)
        }
        val file = parent.findOrCreateChildData(this, parts.last())
        val outputStream = file.getOutputStream(this)

        val buffer = ByteArray(16 * 1024) // A 16  kB buffer
        while (true) {
            val bytesRead = zipInputStream.read(buffer)
            if (bytesRead == -1)
                break
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.close()
    }
}