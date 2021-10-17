package ru.itmo.sofwaredesign.groupunifier.common.domain

interface Messenger {
    val botLogin: String

    fun receiveMessage(body: Any): Message

    fun sendMessage(chatId: String, message: Message)
}