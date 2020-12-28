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
const val DEFAULT_TIMEOUT = 5000L

//heart rate scanning timeout
const val HR_TIMEOUT = 15000L

//encrypted files names
const val PASS_FILE = "pass.txt"

//Device scan duration
const val SCAN_TIMEOUT: Long = 10000

//Pairing key
val KEY = "01234567890122233445566778899002"  //'\x01\x23\x45\x67\x89\x01\x22\x23\x34\x45\x56\x67\x78\x89\x90\x02'

