package ru.itmo.sofwaredesign.groupunifier.common

import ru.itmo.sofwaredesign.groupunifier.common.domain.Message
import ru.itmo.sofwaredesign.groupunifier.common.domain.Messenger
import ru.itmo.sofwaredesign.groupunifier.common.repository.ChatRepository

const val RETRIES_COUNT = 5

class Forwarder(
    private val chatRepository: ChatRepository
) {
    fun forwardMessage(message: Message) {
        try {
            if (message.fromUser.isGroupUnifierBot) {
                return
            }
            val chat = chatRepository.getChatByMessengerAndId(
                message.fromMessenger,
                message.fromChatId
            )
                ?: // bot added to chat, but chat is not registered in bot. Message must be ignored
                return
            chat.implementations
                .filterKeys { it != message.fromMessenger }
                .forEach { (messenger, chatId) -> sendMessage(messenger, chatId, message) }
        } catch (e: RuntimeException) {
            // TODO logging
        }
    }

    private fun sendMessage(messenger: Messenger, chatId: String, message: Message) {
        for (attempt in 1..RETRIES_COUNT) {
            try {
                messenger.sendMessage(chatId, message)
                return
            } catch (e: RuntimeException) {
                if (attempt == RETRIES_COUNT) {
                    throw e
                }
            }
        }
    }
}