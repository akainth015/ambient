package me.akainth.ambient.submitter

import org.w3c.dom.Element

/**
 * A type-safe representation of a transport
 *
 * @author akain
 */
class Transport(source: Element) {
    val params: Array<Param>
    val uri = source.getAttribute("uri")!!

    init {
        val paramsNodes = source.getElementsByTagName("param")
        params = Array(paramsNodes.length) {
            val element = paramsNodes.item(it) as Element
            Param(element.getAttribute("name"), element.getAttribute("value"))
        }
    }
}