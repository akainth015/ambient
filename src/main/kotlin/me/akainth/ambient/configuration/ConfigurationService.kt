package me.akainth.ambient.configuration

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.ServiceManager
import kotlin.reflect.KProperty

class ConfigurationService {
    var snarfSiteUrl by Property(
        SNARF_SITE_URL,
        "https://www2.cs.duke.edu/csed/ambient/prof_help/snarf.xml"
    )
    var webCatRoot by Property(
        WEB_CAT_ROOT,
        "https://web-cat.cs.vt.edu/Web-CAT/WebObjects/Web-CAT.woa"
    )

    var credentials: Credentials?
        get() {
            return PasswordSafe.instance.get(attributes)
        }
        set(value) {
            PasswordSafe.instance.set(attributes, value)
        }

    private val attributes = CredentialAttributes("Ambient.Configuration")

    companion object {
        /**
         * A delegate that stores persists its values across IDE restarts
         */
        private class Property(val key: String, val default: String) {
            private var memoryValue = PropertiesComponent.getInstance().getValue(key, default)

            operator fun getValue(service: ConfigurationService, property: KProperty<*>): String {
                return memoryValue
            }

            operator fun setValue(service: ConfigurationService, property: KProperty<*>, value: String) {
                memoryValue = value
                PropertiesComponent.getInstance().setValue(key, value, default)
            }
        }

        private const val SNARF_SITE_URL = "Ambient.SnarfSiteUrl"
        private const val WEB_CAT_ROOT = "Ambient.WebCatRoot"

        val instance: ConfigurationService
            get() = ServiceManager.getService(ConfigurationService::class.java)
    }
}