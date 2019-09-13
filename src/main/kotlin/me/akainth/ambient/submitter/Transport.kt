package me.akainth.ambient.submitter

import org.w3c.dom.Element

class Transport(source: Element) {
    val uri = source.getAttribute("uri")
}