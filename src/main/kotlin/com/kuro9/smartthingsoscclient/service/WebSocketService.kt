package com.kuro9.smartthingsoscclient.service

import com.illposed.osc.OSCMessage
import com.kuro9.smartthingsoscclient.config.AppConfig
import com.kuro9.smartthingsoscclient.vo.DeviceStateChangeRequest
import com.kuro9.smartthingsoscclient.vo.DeviceStateChangeResponse
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandler
import org.springframework.stereotype.Service
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type

@Service
class WebSocketService(
    private val config: AppConfig
) : StompSessionHandler, Broadcaster<DeviceStateChangeResponse>(), Subscriber<OSCMessage> {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val stompClient = WebSocketStompClient(StandardWebSocketClient()).apply {
        messageConverter = MappingJackson2MessageConverter()
    }
    private val stompSession: StompSession by lazy {
        stompClient.connectAsync(config.wsUrl, this).get()
    }

    @PostConstruct
    fun onStart() {
        logger.info("WebSocketService started: $stompSession")
    }

    fun send(type: Endpoint, payload: DeviceStateChangeRequest) {
        stompSession.send(type.url, payload)
    }


    override fun getPayloadType(headers: StompHeaders): Type {
        return DeviceStateChangeResponse::class.java
    }

    override fun handleFrame(headers: StompHeaders, payload: Any?) {
        (payload as? DeviceStateChangeResponse?)?.let {
            logger.info("Received Response From Server: $payload")
            broadcast(it)
        } ?: logger.warn("Received unknown payload: $payload")
    }

    override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
        session.subscribe("/sub", this)
    }

    override fun handleException(
        session: StompSession,
        command: StompCommand?,
        headers: StompHeaders,
        payload: ByteArray,
        exception: Throwable
    ) {
        exception.printStackTrace()
    }

    override fun handleTransportError(session: StompSession, exception: Throwable) {
        logger.error("Transport error", exception)
    }

    enum class Endpoint(val url: String) {
        REQUEST("/pub/request"),
        STATE("/pub/state")
    }

    override fun onMessage(message: OSCMessage) {
        val receivedParam = message.address.split("/").last()
        val info = message.info.argumentTypeTags

        if (receivedParam == config.mainLight.mainLightParamName) {
            val booleanValue = when (info) {
                "T" -> true
                "F" -> false
                else -> return
            }
            send(
                Endpoint.REQUEST, DeviceStateChangeRequest(
                    capability = config.mainLight.capability,
                    componentId = config.mainLight.componentId,
                    deviceId = config.mainLight.deviceId,
                    subscriptionId = config.mainLight.subscriptionId,
                    value = DeviceStateChangeResponse.SwitchState.getSwitchState(booleanValue).value
                )
            )

            logger.info("Requested to change main light state to $booleanValue")
        } else if (config.subLight != null && receivedParam == config.subLight.subLightParamName) {
            val booleanValue = when (info) {
                "T" -> true
                "F" -> false
                else -> return
            }
            send(
                Endpoint.REQUEST, DeviceStateChangeRequest(
                    capability = config.subLight.capability,
                    componentId = config.subLight.componentId,
                    deviceId = config.subLight.deviceId,
                    subscriptionId = config.subLight.subscriptionId,
                    value = DeviceStateChangeResponse.SwitchState.getSwitchState(booleanValue).value
                )
            )

            logger.info("Requested to change sub light state to $booleanValue")
        }
    }
}