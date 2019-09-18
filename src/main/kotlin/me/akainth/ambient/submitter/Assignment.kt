package me.akainth.ambient.submitter

import org.w3c.dom.Element

class Assignment(private val source: Element) {
    val excludes: Array<String>
        get() {
            val excludeNodes = source.getElementsByTagName("exclude")
            return Array(excludeNodes.length) {
                val excludeElement = excludeNodes.item(it) as Element
                excludeElement.getAttribute("pattern")
            }
        }

    val name: String = source.getAttribute("name")
    val transport: Transport

    init {
        val transportElement = source.getElementsByTagName("transport").item(0) as Element
        transport = Transport(transportElement)
    }

    override fun toString(): String = name
}