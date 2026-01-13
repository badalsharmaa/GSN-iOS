package com.example.getsafenowclient.home.setting

sealed interface SettingEvent {
    data object Close : SettingEvent
    data object EditProfile : SettingEvent
    data class MenuItemClicked(val item: String) : SettingEvent
}
