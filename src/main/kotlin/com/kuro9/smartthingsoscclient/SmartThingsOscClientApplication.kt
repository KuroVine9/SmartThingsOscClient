package com.kuro9.smartthingsoscclient

import com.kuro9.smartthingsoscclient.config.AppConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [AppConfig::class])
class SmartThingsOscClientApplication

fun main(args: Array<String>) {
    runApplication<SmartThingsOscClientApplication>(*args)
    val logger = LoggerFactory.getLogger("Main")

    do {
        logger.info("To terminate client, type 'y' and press Enter.")
        val ch = readln()
    } while (!ch.startsWith("y", true))
}
