package com.naposystems.pepito.ui.selfDestructTime

import androidx.lifecycle.LiveData

interface IContractSelfDestructTime {

    interface ViewModel {
        fun getSelfDestructTime()
        fun setSelfDestructTime(selfDestructTime: Int)
        fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId : Int)
        fun getSelfDestructTimeByContact(contactId : Int)
        fun getMessageSelfDestructTimeNotSent()
    }

    interface Repository {
        fun getSelfDestructTime(): Int
        fun setSelfDestructTime(selfDestructTime: Int)
        suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId : Int)
        suspend fun getSelfDestructTimeByContact(contactId : Int) : LiveData<Int>
        fun getMessageSelfDestructTimeNotSent(): Int
    }
}