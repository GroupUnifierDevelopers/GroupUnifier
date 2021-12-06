package ru.itmo.sofwaredesign.groupunifier.common.connection

import ru.itmo.sofwaredesign.groupunifier.common.domain.Chat
import ru.itmo.sofwaredesign.groupunifier.common.domain.ConnectionRequest
import ru.itmo.sofwaredesign.groupunifier.common.repository.ChatRepository
import ru.itmo.sofwaredesign.groupunifier.common.repository.ConnectionRequestRepository

class ChatConnector(
    private val chatRepository: ChatRepository,
    private val connectionRequestRepository: ConnectionRequestRepository
) {

    fun applyConnection(connectionRequest: ConnectionRequest): ConnectionApplicationAnswer {
        val (sourceChat, destinationChat) = getChats(connectionRequest)
        if (sourceChat == null) return ConnectionApplicationAnswer.SOURCE_CHAT_NOT_REGISTERED
        if (destinationChat == null) return ConnectionApplicationAnswer.DESTINATION_CHAT_NOT_REGISTERED
        sourceChat.implementations.keys.intersect(destinationChat.implementations.keys)
            .forEach { messenger ->
                return if (sourceChat.implementations[messenger] == destinationChat.implementations[messenger]) {
                    ConnectionApplicationAnswer.SAME_CHAT
                } else {
                    ConnectionApplicationAnswer.MERGE_CONFLICT
                }
            }
        if (connectionRequestRepository.exists(connectionRequest))
            return ConnectionApplicationAnswer.APPLICATION_ALREADY_EXISTS
        connectionRequestRepository.save(connectionRequest)
        return ConnectionApplicationAnswer.SUCCESS_APPLICATION
    }

    fun confirmConnection(connectionRequest: ConnectionRequest): ConnectionConfirmationAnswer {
        if (connectionRequestRepository.exists(connectionRequest)) {
            connectionRequestRepository.delete(connectionRequest)
            val (sourceChat, destinationChat) = getChats(connectionRequest)
            if (sourceChat == null) return ConnectionConfirmationAnswer.SOURCE_CHAT_NOT_REGISTERED
            if (destinationChat == null) return ConnectionConfirmationAnswer.DESTINATION_CHAT_NOT_REGISTERED
            return if (chatRepository.merge(sourceChat, destinationChat)) {
                ConnectionConfirmationAnswer.SUCCESS_CONFIRMATION
            } else {
                ConnectionConfirmationAnswer.MERGE_CONFLICT
            }
        } else {
            return ConnectionConfirmationAnswer.APPLICATION_NOT_EXISTS
        }
    }

    private fun getChats(connectionRequest: ConnectionRequest): Pair<Chat?, Chat?> {
        return chatRepository.getByMessengerAndId(
            connectionRequest.sourceMessenger,
            connectionRequest.sourceChatId
        ) to chatRepository.getByMessengerAndId(
            connectionRequest.destinationMessenger,
            connectionRequest.destinationChatId
        )
    }
}