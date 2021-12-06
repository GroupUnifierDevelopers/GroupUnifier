package ru.itmo.sofwaredesign.groupunifier.common.repository

import ru.itmo.sofwaredesign.groupunifier.common.domain.Chat
import ru.itmo.sofwaredesign.groupunifier.common.domain.Messenger

interface ChatRepository {
    fun getByMessengerAndId(messenger: Messenger, chatId: String): Chat?

    fun save(chat: Chat)

    fun delete(chat: Chat)

    fun merge(chat1: Chat, chat2: Chat): Boolean
}