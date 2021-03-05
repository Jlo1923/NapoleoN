package com.naposystems.napoleonchat.model

import androidx.recyclerview.widget.DiffUtil
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.Constants

class FriendShipRequest(
    val id: Int,
    val userOffer: Int,
    val userObtainer: Int,
    val state: String,
    val createdAt: String,
    val contact: ContactEntity,
    val isReceived: Boolean = false
) : FriendShipRequestAdapterType(id, Constants.FriendShipRequestType.FRIENDSHIP_REQUEST_RECEIVED.type) {
    companion object{
        val DiffCallback = object : DiffUtil.ItemCallback<FriendShipRequest>() {
            override fun areItemsTheSame(
                oldItem: FriendShipRequest,
                newItem: FriendShipRequest
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: FriendShipRequest,
                newItem: FriendShipRequest
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}