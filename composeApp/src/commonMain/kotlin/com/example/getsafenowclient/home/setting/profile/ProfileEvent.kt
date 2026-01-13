package com.example.getsafenowclient.home.setting.profile
import net.folivo.trixnity.core.model.events.UnsignedRoomEventData
sealed interface ProfileEvent {
    data object Back : ProfileEvent
    data object Close : ProfileEvent
    
    // Edit flow
    data object EditDisplayName : ProfileEvent
    data class UpdateDisplayName(val newName: String) : ProfileEvent
    data object SaveDisplayName : ProfileEvent
    data object CancelEditDisplayName : ProfileEvent

    data object EditEmail : ProfileEvent
    data class UpdateEmail(val newEmail: String) : ProfileEvent
    data object SaveEmail : ProfileEvent
    data object CancelEditEmail : ProfileEvent

    data object ResetPassword : ProfileEvent
    data object SignOutClicked : ProfileEvent
    data object ConfirmSignOut : ProfileEvent
    data object DismissSignOutDialog : ProfileEvent
    
    // Dialog events
    data object DismissResetPasswordDialog : ProfileEvent
    data class SubmitResetPassword(val current: String, val new: String) : ProfileEvent
}
