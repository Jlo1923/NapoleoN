package com.naposystems.napoleonchat.ui.multipreview.listeners.events

sealed class ViewPreviewVideoEvent {

    object AddFlagsKeepScreen : ViewPreviewVideoEvent()
    object VideoEnded : ViewPreviewVideoEvent()
    object PlayingVideo : ViewPreviewVideoEvent()
    object PauseVideo : ViewPreviewVideoEvent()

}