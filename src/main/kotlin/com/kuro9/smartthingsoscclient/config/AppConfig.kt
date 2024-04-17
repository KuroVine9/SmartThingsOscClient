package com.kuro9.smartthingsoscclient.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "app")
data class AppConfig(
    val oscRxPort: Int,
    val oscTxPort: Int,

    val wsUrl: String,

    val mainLight: MainLight,
    val subLight: SubLight?
) {
    data class MainLight(
        val mainLightParamName: String,
        val deviceId: String,
        val componentId: String,
        val capability: String,
        val subscriptionId: String,
    )

    data class SubLight(
        val subLightParamName: String,
        val deviceId: String,
        val componentId: String,
        val capability: String,
        val subscriptionId: String,
    )
}
