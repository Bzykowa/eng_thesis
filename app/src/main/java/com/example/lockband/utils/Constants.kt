package com.example.lockband.utils

import com.example.lockband.R
import com.example.lockband.data.SettingsItem

//navigation
const val BAND_PAGE_INDEX = 0
const val STATS_PAGE_INDEX = 1
const val SETTINGS_PAGE_INDEX = 2
//settings subpages
const val BLOCKED_APPS_LIST_PAGE_INDEX = 3
const val CHANGE_PASS_PAGE_INDEX = 4
const val ABOUT_PAGE_INDEX = 5

const val DATABASE_NAME = "lockband_db"

val SETTINGS: List<SettingsItem> = listOf(
    SettingsItem(
        R.drawable.outline_playlist_add_check_black_18dp,
        "Blocked apps",
        BLOCKED_APPS_LIST_PAGE_INDEX
    ),
    SettingsItem(
        R.drawable.outline_vpn_key_black_18dp,
        "Change password",
        CHANGE_PASS_PAGE_INDEX
    )
)
//foreground scanning timeout
const val DEFAULT_TIMEOUT = 200L