package com.naposystems.napoleonchat.utility.business

import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.utility.Constants

fun getDrawableSelfDestruction(selfDestruction: Int): Int {
    return when (selfDestruction) {
        Constants.SelfDestructTime.EVERY_FIVE_SECONDS.time -> R.drawable.ic_five_seconds
        Constants.SelfDestructTime.EVERY_FIFTEEN_SECONDS.time -> R.drawable.ic_fifteen_seconds
        Constants.SelfDestructTime.EVERY_THIRTY_SECONDS.time -> R.drawable.ic_thirty_seconds
        Constants.SelfDestructTime.EVERY_ONE_MINUTE.time -> R.drawable.ic_one_minute
        Constants.SelfDestructTime.EVERY_TEN_MINUTES.time -> R.drawable.ic_ten_minutes
        Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time -> R.drawable.ic_thirty_minutes
        Constants.SelfDestructTime.EVERY_ONE_HOUR.time -> R.drawable.ic_one_hour
        Constants.SelfDestructTime.EVERY_TWELVE_HOURS.time -> R.drawable.ic_twelve_hours
        Constants.SelfDestructTime.EVERY_ONE_DAY.time -> R.drawable.ic_one_day
        Constants.SelfDestructTime.EVERY_SEVEN_DAY.time -> R.drawable.ic_seven_days
        else -> R.drawable.ic_five_seconds
    }
}
