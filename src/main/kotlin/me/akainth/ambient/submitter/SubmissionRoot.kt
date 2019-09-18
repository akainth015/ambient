package me.akainth.ambient.submitter

import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class SubmissionRoot constructor(submissionRootDocument: Document) {
    private val submissionTargetElement =
        submissionRootDocument.getElementsByTagName("submission-targets").item(0) as Element

    fun buildTreeModel(): DefaultTreeModel {
        val root = DefaultMutableTreeNode(assignmentGroup.name)

        assignments.map { DefaultMutableTreeNode(it) }.forEach {
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

    val assignmentGroup: AssignmentGroup
        get() {
            val assignmentGroupElement =
                submissionTargetElement.getElementsByTagName("assignment-group").item(0) as Element
            return AssignmentGroup(assignmentGroupElement)
        }

    val assignments = assignmentGroup.assignments
}