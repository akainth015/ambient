package me.akainth.ambient.packages

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import me.akainth.ambient.configuration.ConfigurationService
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.xml.parsers.DocumentBuilderFactory

class AssignmentBrowser(private val project: Project) : Tree() {
    init {
        val configurationService = ConfigurationService.instance
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(configurationService.snarfSiteUrl)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val notification = Notification(
                "Ambient.Connection",
                "Assignment Browser",
                "Could not connect to the Snarf server",
                NotificationType.ERROR
            )
            Notifications.Bus.notify(notification)
        }
        response.body?.let {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val document = documentBuilder.parse(it.byteStream())
            val snarfSiteNode = document.getElementsByTagName("snarf_site").item(0) as Element

            val root = DefaultMutableTreeNode(snarfSiteNode.getAttribute("name"))

            val categories = mutableMapOf<String, DefaultMutableTreeNode>()

            val packageNodes = snarfSiteNode.getElementsByTagName("package")

            for (i in 0 until packageNodes.length) {
                val packageNode = packageNodes.item(i) as Element
                val packageData = Package(packageNode)

                val categoryTreeNode =
                    categories.getOrPut(packageData.category, { DefaultMutableTreeNode(packageData.category) })
                val packageTreeNode = DefaultMutableTreeNode(packageData)
                categoryTreeNode.add(packageTreeNode)
            }

            categories.values.forEach(root::add)

            model = DefaultTreeModel(root)

            addMouseListener(object : MouseListener {
                override fun mouseReleased(e: MouseEvent) {
                }

                override fun mouseEntered(e: MouseEvent) {
                }

                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount != 2) return
                    val node = getClosestPathForLocation(e.x, e.y).lastPathComponent as DefaultMutableTreeNode
                    val userObject = node.userObject
                    if (userObject is Package) {
                        PackageModal(project, userObject).show()
                    }
                }

                override fun mouseExited(e: MouseEvent) {
                }

                override fun mousePressed(e: MouseEvent) {
                }

            })
        }
    }
}