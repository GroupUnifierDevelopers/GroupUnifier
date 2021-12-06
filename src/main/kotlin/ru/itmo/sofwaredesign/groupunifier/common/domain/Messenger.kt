package ru.itmo.sofwaredesign.groupunifier.common.domain

interface Messenger {
    val botLogin: String

    fun sendMessage(chatId: String, message: Message)
}