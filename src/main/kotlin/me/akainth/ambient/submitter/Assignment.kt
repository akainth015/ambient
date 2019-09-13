package me.akainth.ambient.submitter

import org.w3c.dom.Element

class Assignment(source: Element) {
    val name: String = source.getAttribute("name")
    val transport: Transport

    init {
        val transportElement = source.getElementsByTagName("transport").item(0) as Element
        transport = Transport(transportElement)
    }

    override fun toString(): String = name
}