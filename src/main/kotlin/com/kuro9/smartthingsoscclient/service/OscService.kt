package com.kuro9.smartthingsoscclient.service

import com.illposed.osc.OSCBadDataEvent
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacketEvent
import com.illposed.osc.OSCPacketListener
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import com.kuro9.smartthingsoscclient.config.AppConfig
import com.kuro9.smartthingsoscclient.vo.DeviceStateChangeResponse
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.LocalDateTime
import kotlin.math.pow

@Service
class OscService(
    private val config: AppConfig
) : OSCPacketListener, Broadcaster<OSCMessage>(), Subscriber<DeviceStateChangeResponse> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var oscTX: OSCPortOut
    private lateinit var oscRX: OSCPortIn

    private val parameterAdressPrefix = "/avatar/parameters/"

    private var lastControlMap = mutableMapOf<String, LocalDateTime>()

    @PostConstruct
    fun onStart() {
        var successFlag = false
        lateinit var exception: Throwable

        for(i in 0..5) {
            if(kotlin.runCatching {
                oscTX = OSCPortOut(InetSocketAddress(InetAddress.getByAddress(byteArrayOf(127,0,0,1)), config.oscTxPort))
                oscRX = OSCPortIn(InetSocketAddress(InetAddress.getByAddress(byteArrayOf(127,0,0,1)), config.oscRxPort))
            }.onFailure {
                logger.error("Error During Creating Osc Connection. RetryCount=${i}/5", it)
                    exception = it
                Thread.sleep(1000L * (2.0.pow(i + 1.0)).toLong())
            }.onSuccess { successFlag = true }.isSuccess) break
        }

        if (successFlag.not()) throw RuntimeException("Osc Service Failed with Err. Terminate.", exception)


        with(oscRX) {
            addPacketListener(this@OscService)
            startListening()
            logger.info("Osc Started! listening=$isListening, connected=$isConnected")
        }
        logger.info("OscService started: Rx=${config.oscRxPort}, Tx=${config.oscTxPort}")
    }

    override fun onMessage(message: DeviceStateChangeResponse) {
        val type = message.deviceType
        val value = message.deviceState

        when (type) {
            DeviceStateChangeResponse.InternalDeviceType.MAIN_LIGHT -> config.mainLight.mainLightParamName
            DeviceStateChangeResponse.InternalDeviceType.SUB_LIGHT -> config.subLight?.subLightParamName
        }?.let {
            sendMsg(parameterAdressPrefix + it, value.booleanValue)
        } ?: logger.info("no parameter name for $type. check properties file.")
    }

    fun <T> sendMsg(path: String, payload: T) {
        val msg = OSCMessage(path, listOf(payload))
        oscTX.send(msg)
        logger.info("TX: {}", path)
    }

    override fun handlePacket(event: OSCPacketEvent?) {
        val message = event!!.packet as OSCMessage
        if(!message.address.endsWith("Light")) return

        val lastControl = lastControlMap[message.address] ?: LocalDateTime.MIN
        if (LocalDateTime.now().isAfter(lastControl.plusSeconds(2L))) {
            broadcast(message)
            logger.info("${message.address}: ${message.arguments.firstOrNull()}")
        }
        else {
            logger.info("[IGNORE] ${message.address}: ${message.arguments.firstOrNull()}")
        }

        lastControlMap[message.address] = LocalDateTime.now()
    }

    override fun handleBadData(event: OSCBadDataEvent?) {
        logger.warn("bad data?: $event")
    }
}