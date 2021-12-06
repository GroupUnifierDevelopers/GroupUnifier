package ru.itmo.sofwaredesign.groupunifier.common.domain

data class ConnectionRequest(
    val sourceMessenger: Messenger,
    val sourceChatId: String,
    val destinationMessenger: Messenger,
    val destinationChatId: String
)