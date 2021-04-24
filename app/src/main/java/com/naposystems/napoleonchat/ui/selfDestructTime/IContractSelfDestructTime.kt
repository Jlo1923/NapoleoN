package com.naposystems.napoleonchat.ui.selfDestructTime

import androidx.lifecycle.LiveData

interface IContractSelfDestructTime {

    interface ViewModel {

        fun getSelfDestructTime()

        fun setSelfDestructTime(selfDestructTime: Int)

        fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int)

        fun getSelfDestructTimeByContact(contactId: Int)

        fun getMessageSelfDestructTimeNotSent()

    }

    interface Repository {

        fun getSelfDestructTime(): Int

        fun setSelfDestructTime(selfDestructTime: Int)

        suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int)

        suspend fun getSelfDestructTimeByContact(contactId: Int): LiveData<Int>
        
        suspend fun getSelfDestructTimeAsIntByContact(contactId: Int): Int

        fun getMessageSelfDestructTimeNotSent(): Int

    }
}