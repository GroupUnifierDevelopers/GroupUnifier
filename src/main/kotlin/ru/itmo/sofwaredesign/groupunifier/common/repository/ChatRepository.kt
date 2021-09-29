package ru.itmo.sofwaredesign.groupunifier.common.repository

import ru.itmo.sofwaredesign.groupunifier.common.domain.Chat
import ru.itmo.sofwaredesign.groupunifier.common.domain.Messenger

interface ChatRepository {
    fun getChatByMessengerAndId(messenger: Messenger, chatId: String): Chat?
}