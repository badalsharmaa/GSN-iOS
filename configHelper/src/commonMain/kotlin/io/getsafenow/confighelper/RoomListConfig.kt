package io.getsafenow.confighelper

object RoomListConfig {
    const val SHOW_INVITE_MENU_ITEM = false
    const val SHOW_REPORT_PROBLEM_MENU_ITEM = false

    const val HAS_DROP_DOWN_MENU = SHOW_INVITE_MENU_ITEM || SHOW_REPORT_PROBLEM_MENU_ITEM
}
