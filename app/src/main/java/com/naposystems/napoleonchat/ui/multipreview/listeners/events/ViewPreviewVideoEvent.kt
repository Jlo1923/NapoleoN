package com.naposystems.napoleonchat.ui.multipreview.listeners.events

sealed class ViewPreviewVideoEvent {

    object AddFlagsKeepScreen : ViewPreviewVideoEvent()
    object RemoveFlagsKeepScreen : ViewPreviewVideoEvent()
    object PlayingVideo : ViewPreviewVideoEvent()
    object PauseVideo : ViewPreviewVideoEvent()

}