package me.akainth.ambient.snarf

import org.w3c.dom.Element
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Describes a package in a Snarf site
 */
@Suppress("unused")
class Package(source: Element) {
    /**
     * The package's name, according to the Snarf site
     */
    val name: String = source.getAttribute("name")
    /**
     * The category that the package belongs to
     */
    val category: String = source.getAttribute("category")
    /**
     * The user that published the package
     */
    val publisher: String = source.getAttribute("publisher")
    /**
     * The package version, unused according to the Ambient site
     */
    val version: String = source.getAttribute("version")
    /**
     * The package's type. Currently, only Java is supported, although support for C++ is on the roadmap
     */
    val projectType: String = source.getAttribute("project_type")

    private val descriptionElement = source.getElementsByTagName("description").item(0)
    /**
     * A textual, human readable description of the assignment
     */
    val description: String = descriptionElement.textContent

    private val entryElement = source.getElementsByTagName("entry").item(0) as Element
    /**
     * The URL of the ZIP file containing the package source
     */
    val entry: String = entryElement.getAttribute("url")

    val treeNode
        get() = DefaultMutableTreeNode(this)

    override fun toString(): String {
        return name
    }
}