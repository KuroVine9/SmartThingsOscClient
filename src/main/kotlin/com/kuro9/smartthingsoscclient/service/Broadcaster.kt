package com.kuro9.smartthingsoscclient.service

abstract class Broadcaster<T> {
    private val subs = mutableSetOf<Subscriber<T>>()
    fun attach(subscriber: Subscriber<T>) = subs.add(subscriber)
    fun detach(subscriber: Subscriber<T>) = subs.remove(subscriber)
    fun broadcast(message: T) = subs.forEach { it.onMessage(message) }

}