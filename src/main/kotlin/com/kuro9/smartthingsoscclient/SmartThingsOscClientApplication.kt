package com.kuro9.smartthingsoscclient

import com.kuro9.smartthingsoscclient.config.AppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [AppConfig::class])
class SmartThingsOscClientApplication {
}

fun main(args: Array<String>) {
    runApplication<SmartThingsOscClientApplication>(*args)

    while (true) {
        println(1)
        Thread.sleep(100000)
    };
}
