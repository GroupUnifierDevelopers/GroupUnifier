package ru.itmo.sofwaredesign.groupunifier.common.registration

import ru.itmo.sofwaredesign.groupunifier.common.domain.Chat
import ru.itmo.sofwaredesign.groupunifier.common.domain.Messenger
import ru.itmo.sofwaredesign.groupunifier.common.registration.RegistrationAnswer.*
import ru.itmo.sofwaredesign.groupunifier.common.repository.ChatRepository

class ChatRegistrar(
    private val chatRepository: ChatRepository,
) {
    fun registerChat(messenger: Messenger, chatId: String): RegistrationAnswer {
        val chat = chatRepository.getByMessengerAndId(messenger, chatId)
        return if (chat == null) {
            chatRepository.save(Chat(mutableMapOf(messenger to chatId)))
            REGISTRATION_SUCCESS
        } else {
            ALREADY_REGISTERED
        }
    }

    fun unregisterChat(messenger: Messenger, chatId: String): RegistrationAnswer {
        val chat = chatRepository.getByMessengerAndId(messenger, chatId)
        return if (chat == null) {
            NOT_REGISTERED
        } else {
            if (chat.implementations.size == 1) {
                chatRepository.delete(chat)
            } else {
                chat.implementations.remove(messenger)
                chatRepository.save(chat)
            }
            UNREGISTRATION_SUCCESS
        }
    }
}