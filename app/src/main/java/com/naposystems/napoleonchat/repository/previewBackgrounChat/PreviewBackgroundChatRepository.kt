package com.naposystems.napoleonchat.repository.previewBackgrounChat

interface PreviewBackgroundChatRepository {
    suspend fun updateChatBackground(newBackground: String)
}