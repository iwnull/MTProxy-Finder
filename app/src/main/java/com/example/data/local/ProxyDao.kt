package com.example.data.local

import androidx.room.*
import com.example.data.model.ProxyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProxyDao {
    @Query("SELECT * FROM proxies ORDER BY isOnline DESC, ping ASC")
    fun getAllProxies(): Flow<List<ProxyEntity>>

    @Query("SELECT * FROM proxies WHERE isOnline = 1 ORDER BY ping ASC")
    fun getOnlineProxies(): Flow<List<ProxyEntity>>

    @Query("SELECT * FROM proxies WHERE server = :server AND port = :port LIMIT 1")
    suspend fun getProxyByAddress(server: String, port: Int): ProxyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProxy(proxy: ProxyEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProxies(proxies: List<ProxyEntity>)

    @Update
    suspend fun updateProxy(proxy: ProxyEntity)

    @Delete
    suspend fun deleteProxy(proxy: ProxyEntity)

    @Delete
    suspend fun deleteProxies(proxies: List<ProxyEntity>)

    @Query("DELETE FROM proxies")
    suspend fun clearAllProxies()

    @Query("SELECT * FROM proxies WHERE source != 'manual'")
    suspend fun getGitHubProxies(): List<ProxyEntity>
}
