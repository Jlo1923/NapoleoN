package com.naposystems.napoleonchat.repository.status

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.status.StatusLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfo422DTO
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.source.remote.dto.status.UserStatusReqDTO
import com.naposystems.napoleonchat.source.local.entity.StatusEntity
import com.naposystems.napoleonchat.ui.status.IContractStatus
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class StatusRepository @Inject constructor(
    private val statusLocalDataSourceImp: StatusLocalDataSourceImp,
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSourceImp: UserLocalDataSourceImp
) :
    IContractStatus.Repository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun getStatus(): LiveData<MutableList<StatusEntity>> {
        return statusLocalDataSourceImp.getStatus()
    }

    override suspend fun updateRemoteStatus(userStatus: UserStatusReqDTO): Response<UpdateUserInfoResDTO> {
        return napoleonApi.updateUserStatus(userStatus)
    }

    override suspend fun updateLocalStatus(newStatus: String, firebaseId: String) {
        userLocalDataSourceImp.updateStatus(newStatus, firebaseId)
    }

    override suspend fun insertNewStatus(listStatusEntities: List<StatusEntity>) {
        statusLocalDataSourceImp.insertNewStatus(listStatusEntities)
    }

    override suspend fun deleteStatus(statusEntity: StatusEntity) {
        statusLocalDataSourceImp.deleteStatus(statusEntity)
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