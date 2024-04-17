package com.kuro9.smartthingsoscclient.service;

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service;

@Service
class MasterConnectionService(
    private val wsService: WebSocketService,
    private val oscService: OscService,
) {

    @PostConstruct
    fun start() {
        wsService.attach(oscService)
        oscService.attach(wsService)
    }
}
