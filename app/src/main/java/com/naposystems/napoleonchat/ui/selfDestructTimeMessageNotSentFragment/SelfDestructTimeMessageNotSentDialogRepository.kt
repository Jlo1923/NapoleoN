package com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment

interface SelfDestructTimeMessageNotSentDialogRepository {
        suspend fun getSelfDestructTimeMessageNotSent(): Int
        suspend fun setSelfDestructTimeMessageNotSent(time: Int)
}