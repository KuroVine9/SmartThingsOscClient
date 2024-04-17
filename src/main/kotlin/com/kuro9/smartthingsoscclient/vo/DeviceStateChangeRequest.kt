package com.kuro9.smartthingsoscclient.vo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeviceStateChangeRequest @JsonCreator constructor(
    @JsonProperty("capability") val capability: String,
    @JsonProperty("componentId") val componentId: String,
    @JsonProperty("deviceId") val deviceId: String,
    @JsonProperty("subscriptionId") val subscriptionId: String,
    @JsonProperty("value") val value: String
)