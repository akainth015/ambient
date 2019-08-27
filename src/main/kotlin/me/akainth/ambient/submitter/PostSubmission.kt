package me.akainth.ambient.submitter

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.ui.Messages
import me.akainth.ambient.configuration.ConfigurationService
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.w3c.dom.Element
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory


class PostSubmission(credentials: Credentials, jar: File) {
    init {
        val configurationService = ConfigurationService.instance

        val request = Request.Builder()
            .url(configurationService.webCatRoot + "/wa/assignments/eclipse")
            .build()

        val response = OkHttpClient().newCall(request).execute()

        require(response.isSuccessful) { "Configured WebCAT root is not returning assignments" }

        response.body?.let {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val document = documentBuilder.parse(it.byteStream())

            val submissionTarget = document.getElementsByTagName("submission-targets").item(0) as Element
            val assignmentNodes = submissionTarget.getElementsByTagName("assignment")
            val assignments = mutableListOf<String>()
            for (i in 0 until assignmentNodes.length) {
                val assignmentElement = assignmentNodes.item(i) as Element
                assignments += assignmentElement.getAttribute("name")
            }
            val assignmentName = Messages.showEditableChooseDialog(
                "Choose an assignment to submit to",
                "Submit Assignment",
                null,
                assignments.toTypedArray(),
                null,
                null
            )
            for (i in 0 until assignmentNodes.length) {
                val assignment = assignmentNodes.item(i) as Element
                if (assignment.getAttribute("name") == assignmentName) {
                    val transport = assignment.getElementsByTagName("transport").item(0) as Element
                    val submitTo = transport.getAttribute("uri")

                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                    val params = transport.getElementsByTagName("param")
                    val username = credentials.userName!!
                    val password = credentials.password!!
                    for (j in 0 until params.length) {
                        val param = params.item(j) as Element
                        val value = when (val paramValue = param.getAttribute("value")) {
                            "\${user}" -> username
                            "\${pw}" -> password.toString()
                            else -> paramValue
                        }
                        requestBody.addFormDataPart(param.getAttribute("name"), value)
                    }
                    requestBody.addFormDataPart("file1", "$username.jar", jar.asRequestBody())

                    val submission = Request.Builder()
                        .url(submitTo)
                        .post(requestBody.build())
                        .build()

                    val submissionResponse = OkHttpClient().newCall(submission).execute()
                    val p =
                        Pattern.compile("<a href=\"(.+)\">click here to view them.</a>")
                    val m = p.matcher(submissionResponse.body!!.string())
                    while (m.find()) {
                        val viewSubmissionUrl = m.group(1)
                        Desktop.getDesktop().browse(URI(viewSubmissionUrl))
                    }
                    break
                }
            }
        }
    }
}