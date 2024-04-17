package com.kuro9.smartthingsoscclient.service

interface Subscriber<T> {
    fun onMessage(message: T)
}