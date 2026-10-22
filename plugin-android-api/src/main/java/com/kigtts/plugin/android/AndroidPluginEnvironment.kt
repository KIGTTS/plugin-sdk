package com.kigtts.plugin.android

import android.content.Context
import com.kigtts.plugin.api.PluginEnvironment

/** Context belongs to a worker service, never an Activity. Do not use its files/cache dirs. */
interface AndroidPluginEnvironment : PluginEnvironment {
    val context: Context
}
