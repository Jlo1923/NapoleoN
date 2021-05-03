package com.naposystems.napoleonchat.repository.base

interface BaseRepository {
    suspend fun outputControl(state: Int)
    suspend fun getOutputControl(): Int
    fun connectSocket()
}