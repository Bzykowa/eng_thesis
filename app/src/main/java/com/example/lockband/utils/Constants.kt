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
const val HR_TIMEOUT = 12000L

//BLE operation delay
const val OP_TIMEOUT = 500L

//encrypted files names
const val PASS_FILE = "pass.txt"

//Device scan duration
const val SCAN_TIMEOUT: Long = 10000

//Authentication key
val KEY = byteArrayOf(0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45)  //\xf5\xd2\x29\x87\x65\x0a\x1d\x82\x05\xab\x82\xbe\xb9\x38\x59\xcf

