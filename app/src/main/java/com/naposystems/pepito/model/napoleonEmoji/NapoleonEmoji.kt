package com.naposystems.pepito.model.napoleonEmoji

import com.google.firebase.storage.StorageReference
import java.io.Serializable

data class NapoleonEmoji(
    val storageReference: StorageReference,
    val items: List<StorageReference>
) : Serializable