package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proxies")
data class ProxyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val server: String,
    val port: Int,
    val secret: String,
    val ping: Long = -1,
    val isOnline: Boolean = false,
    val lastChecked: Long = 0,
    val source: String = "manual",
    val stabilityScore: Float = 0.0f,
    val totalChecks: Int = 0,
    val successfulChecks: Int = 0
) {
    /**
     * Helper to get the click connection URI for Telegram.
     */
    val connectionUrl: String
        get() = "tg://proxy?server=$server&port=$port&secret=$secret"
        
    /**
     * Friendly display string for the server and port.
     */
    val displayAddress: String
        get() = "$server:$port"
}
