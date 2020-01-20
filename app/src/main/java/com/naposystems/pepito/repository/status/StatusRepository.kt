package com.naposystems.pepito.repository.status

import com.naposystems.pepito.db.dao.status.StatusLocalDataSource
import com.naposystems.pepito.dto.profile.UpdateUserInfo422DTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoErrorDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.entity.Status
import com.naposystems.pepito.ui.status.IContractStatus
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class StatusRepository @Inject constructor(
    private val statusLocalDataSource: StatusLocalDataSource,
    private val napoleonApi: NapoleonApi
) :
    IContractStatus.Repository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun getStatus(): List<Status> {
        return statusLocalDataSource.getStatus()
    }

    override suspend fun updateRemoteStatus(updateUserInfoReqDTO: UpdateUserInfoReqDTO): Response<UpdateUserInfoResDTO> {
        return napoleonApi.updateUserInfo(updateUserInfoReqDTO)
    }

    override suspend fun updateLocalStatus(newStatus: String, firebaseId: String) {
        statusLocalDataSource.updateStatus(newStatus, firebaseId)
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