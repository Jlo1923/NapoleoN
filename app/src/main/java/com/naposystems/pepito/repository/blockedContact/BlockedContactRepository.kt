package com.naposystems.pepito.repository.blockedContact

import com.naposystems.pepito.db.dao.blockedContacts.BlockedContactsLocalDataSource
import com.naposystems.pepito.dto.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.entity.BlockedContact
import com.naposystems.pepito.ui.blockedContacts.IContractBlockedContact
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.webService.NapoleonApi
import timber.log.Timber

class BlockedContactRepository constructor(
    private val napoleonApi: NapoleonApi,
    private val blockedContactsLocalDataSource: BlockedContactsLocalDataSource
) : IContractBlockedContact.Repository {

    override suspend fun getBlockedContacts(): List<BlockedContact> {
        val blockedContacts: MutableList<BlockedContact> = arrayListOf()

        val localBlockedContacts = blockedContactsLocalDataSource.getBlockedContacts()

        try {
            val response = napoleonApi.getBlockedContacts(Constants.FriendShipState.BLOCKED.state)

            if (response.isSuccessful) {

                if (localBlockedContacts.isNotEmpty()) {
                    blockedContactsLocalDataSource.clearTable()
                }

                blockedContacts.addAll(BlockedContactResDTO.toEntityList(response.body()!!))
                blockedContactsLocalDataSource.insertBlockedContacts(blockedContacts)

            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return blockedContacts
    }
}