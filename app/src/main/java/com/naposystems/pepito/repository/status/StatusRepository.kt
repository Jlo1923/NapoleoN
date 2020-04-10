package com.naposystems.pepito.repository.status

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.status.StatusLocalDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.profile.UpdateUserInfo422DTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoErrorDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.dto.status.UserStatusReqDTO
import com.naposystems.pepito.entity.Status
import com.naposystems.pepito.ui.status.IContractStatus
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class StatusRepository @Inject constructor(
    private val statusLocalDataSource: StatusLocalDataSource,
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSource: UserLocalDataSource
) :
    IContractStatus.Repository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun getStatus(): LiveData<List<Status>> {
        return statusLocalDataSource.getStatus()
    }

    override suspend fun updateRemoteStatus(userStatus: UserStatusReqDTO): Response<UpdateUserInfoResDTO> {
        return napoleonApi.updateUserStatus(userStatus)
    }

    override suspend fun updateLocalStatus(newStatus: String, firebaseId: String) {
        userLocalDataSource.updateStatus(newStatus, firebaseId)
    }

    override suspend fun insertNewStatus(listStatus: List<Status>) {
        statusLocalDataSource.insertNewStatus(listStatus)
    }

    override suspend fun deleteStatus(status: Status) {
        statusLocalDataSource.deleteStatus(status)
    }

    fun get422Error(response: Response<UpdateUserInfoResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(UpdateUserInfo422DTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(enterCodeError!!)
    }

    fun getDefaultError(response: Response<UpdateUserInfoResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(UpdateUserInfoErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }
}