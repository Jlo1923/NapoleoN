package com.naposystems.pepito.model.napoleonEmoji

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
@JsonClass(generateAdapter = true)
data class NapoleonEmojiRemoteConfig(
    @Json(name = "Type") val type: String,
    @Json(name = "Count") val count: Int
): Parcelable, Serializable