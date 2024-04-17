package com.kuro9.smartthingsoscclient.vo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.smartthings.sdk.client.models.AttributeState

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeviceStateChangeResponse @JsonCreator constructor(
    @JsonProperty("capability") val capability: String,
    @JsonProperty("componentId") val componentId: String,
    @JsonProperty("deviceId") val deviceId: String,
    @JsonProperty("internalId") val internalId: String,
    @JsonProperty("state") val state: AttributeState
) {
    enum class InternalDeviceType(val value: String) {
        MAIN_LIGHT("main-light-20240414"),
        SUB_LIGHT("sub-light-20240414"),
    }

    enum class SwitchState(val value: String, val booleanValue: Boolean) {
        ON("on", true),
        OFF("off", false);

        companion object {
            fun getSwitchState(booleanValue: Boolean): SwitchState {
                return if (booleanValue) ON else OFF
            }
        }
    }

    val deviceType: InternalDeviceType
        get() = InternalDeviceType.entries.first { it.value == internalId }

    val deviceState: SwitchState
        get() = SwitchState.entries.first { it.value == state.value }
}