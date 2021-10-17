package ru.itmo.sofwaredesign.groupunifier.common.domain

data class Message(
    val text: String,
    val fromUser: User,
    val fromChatId: String,
    val fromMessenger: Messenger
)
