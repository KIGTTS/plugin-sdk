package com.kigtts.plugin.api

/** Public catalog location, never an account token. Package signature trust remains independent. */
object OfficialPluginSource {
    const val ID = "kigtts.official"
    const val NAME = "KIGTTS 官方插件源"
    const val HOMEPAGE = "https://modelscope.cn/datasets/LHTSTUDIO/KIGTTS-Official-Plugins"
    const val URL = "$HOMEPAGE/resolve/master/index.json?view=false"
}
