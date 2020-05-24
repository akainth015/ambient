package me.akainth.ambient.snarf

import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/**
 * Type-safe representation of a snarf site
 *
 * @author akainth
 */
class SnarfSite constructor(snarfSiteDocument: Document) {
    private val snarfSiteElement = snarfSiteDocument.getElementsByTagName("snarf_site").item(0) as Element

    private val packages: Array<out Package>
    val treeModel: TreeModel

    init {
        val packageNodes = snarfSiteElement.getElementsByTagName("package")
        packages = Array(packageNodes.length) {
            Package(packageNodes.item(it) as Element)
        }

        val root = DefaultMutableTreeNode(snarfSiteElement.getAttribute("name"))
        packages.groupBy { it.category }.forEach { (category, packages) ->
            val categoryTreeNode = DefaultMutableTreeNode(category)
            packages.forEach {
                categoryTreeNode.add(it.treeNode)
            }
            root.add(categoryTreeNode)
        }
        treeModel = DefaultTreeModel(root)
    }
}