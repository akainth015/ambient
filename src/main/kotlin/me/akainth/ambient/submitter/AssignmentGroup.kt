package me.akainth.ambient.submitter

import org.w3c.dom.Element

/**
 * A type-safe representation of an assignment group
 *
 * @author akainth
 */
class AssignmentGroup(source: Element) {
    val assignments: Array<Assignment>

    val name = source.getAttribute("name")!!

    init {
        val assignmentNodes = source.getElementsByTagName("assignment")
        assignments = Array(assignmentNodes.length) {
            val assignmentElement = assignmentNodes.item(it) as Element
            Assignment(assignmentElement)
        }
    }
}