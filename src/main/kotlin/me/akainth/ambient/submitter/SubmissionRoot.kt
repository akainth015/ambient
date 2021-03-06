package me.akainth.ambient.submitter

import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * A type-safe representation of a submission root
 *
 * @author akainth
 */
class SubmissionRoot constructor(submissionRootDocument: Document) {
    private val submissionTargetElement =
        submissionRootDocument.getElementsByTagName("submission-targets").item(0) as Element

    fun buildTreeModel(): DefaultTreeModel {
        val root = DefaultMutableTreeNode("Assignment Groups")

        assignmentGroups.map {
            val node = DefaultMutableTreeNode(it)
            it.assignments
                .map { DefaultMutableTreeNode(it) }
                .forEach { node.add(it) }
            node
        }.forEach {
            root.add(it)
        }

        return DefaultTreeModel(root)
    }

    val excludes: Array<String>
        get() {
            val excludeNodes = submissionTargetElement.getElementsByTagName("exclude")
            return Array(excludeNodes.length) {
                val excludeElement = excludeNodes.item(it) as Element
                excludeElement.getAttribute("pattern")
            }
        }

    private val assignmentGroups: Array<AssignmentGroup>
        get() {
            val assignmentGroupNodes = submissionTargetElement.getElementsByTagName("assignment-group")
            return Array(assignmentGroupNodes.length) {
                val assignmentGroupElement = assignmentGroupNodes.item(it) as Element
                AssignmentGroup(assignmentGroupElement)
            }
        }
}
