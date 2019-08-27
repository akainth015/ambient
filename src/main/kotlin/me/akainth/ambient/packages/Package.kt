package me.akainth.ambient.packages

import org.w3c.dom.Element

class Package(source: Element) {
    val name = source.getAttribute("name")
    val category = source.getAttribute("category")
    val publisher = source.getAttribute("publisher")
    val version = source.getAttribute("version")
    val description = source.getElementsByTagName("description").item(0).textContent
    private val entry = source.getElementsByTagName("entry").item(0) as Element
    val entryUrl = entry.getAttribute("url")

    override fun toString(): String {
        return "$name ($version)"
    }
}