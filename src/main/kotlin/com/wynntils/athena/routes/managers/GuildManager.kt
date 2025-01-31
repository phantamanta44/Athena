package com.wynntils.athena.routes.managers

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.database.objects.GuildProfile
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

object GuildManager {

    private val weakCache = WeakHashMap<String, GuildProfile>()

    /**
     * Returns or try to generate a Guild Profile
     * @return a GuildProfile instance if present
     */
    fun getGuildData(name: String?): GuildProfile? {
        return weakCache.getOrPut(name) { DatabaseManager.getGuildProfile(name) ?: generateGuildData(name) }
    }

    private fun generateGuildData(name: String?): GuildProfile? {
        if (name == null) {
            return GuildProfile("None", "NONE", "#ffffff")
        }

        val connection = URL("${apiConfig.wynnGuildInfo}${name.replace(" ", "%20")}").openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        val input = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONObject>()
        if (input.containsKey("error")) return null

        val profile = GuildProfile(name, input["prefix"] as String)
        profile.asyncSave()

        return profile
    }

}