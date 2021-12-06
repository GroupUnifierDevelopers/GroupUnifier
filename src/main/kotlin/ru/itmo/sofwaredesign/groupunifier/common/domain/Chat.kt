package ru.itmo.sofwaredesign.groupunifier.common.domain

data class Chat(
    val implementations: MutableMap<Messenger, String> = mutableMapOf()
)
