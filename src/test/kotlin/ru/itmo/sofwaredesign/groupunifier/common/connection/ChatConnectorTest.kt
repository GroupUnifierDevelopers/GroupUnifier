package ru.itmo.sofwaredesign.groupunifier.common.connection

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import ru.itmo.sofwaredesign.groupunifier.common.domain.Chat
import ru.itmo.sofwaredesign.groupunifier.common.domain.ConnectionRequest
import ru.itmo.sofwaredesign.groupunifier.common.domain.Messenger
import ru.itmo.sofwaredesign.groupunifier.common.repository.ChatRepository
import ru.itmo.sofwaredesign.groupunifier.common.repository.ConnectionRequestRepository

internal class ChatConnectorTest {
    private val chatRepository: ChatRepository = mock()
    private val connectionRequestRepository: ConnectionRequestRepository = mock()
    private val chatConnector = ChatConnector(chatRepository, connectionRequestRepository)

    private val connectionRequest = ConnectionRequest(mock(), "123", mock(), "456")


    @AfterEach
    fun after() {
        verifyNoMoreInteractions(chatRepository)
        verifyNoMoreInteractions(connectionRequestRepository)
    }

    @Test
    fun `source chat not registered in application request`() {
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(null)

        assertEquals(
            ConnectionApplicationAnswer.SOURCE_CHAT_NOT_REGISTERED,
            chatConnector.applyConnection(connectionRequest)
        )

        verifyChatGetting()
    }

    @Test
    fun `destination chat not registered in application request`() {
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(Chat())
            .thenReturn(null)

        assertEquals(
            ConnectionApplicationAnswer.DESTINATION_CHAT_NOT_REGISTERED,
            chatConnector.applyConnection(connectionRequest)
        )

        verifyChatGetting()
    }

    @Test
    fun `merge conflict in application request`() {
        val thirdMessenger: Messenger = mock()

        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(Chat(mutableMapOf(thirdMessenger to "1")))
            .thenReturn(Chat(mutableMapOf(thirdMessenger to "2")))

        assertEquals(
            ConnectionApplicationAnswer.MERGE_CONFLICT,
            chatConnector.applyConnection(connectionRequest)
        )

        verifyChatGetting()
    }

    @Test
    fun `same chats in application request`() {
        val thirdMessenger: Messenger = mock()

        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(Chat(mutableMapOf(thirdMessenger to "1")))
            .thenReturn(Chat(mutableMapOf(thirdMessenger to "1")))

        assertEquals(
            ConnectionApplicationAnswer.SAME_CHAT,
            chatConnector.applyConnection(connectionRequest)
        )

        verifyChatGetting()
    }

    @Test
    fun `repeated application`() {
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(Chat())
        whenever(connectionRequestRepository.exists(any()))
            .thenReturn(true)

        assertEquals(
            ConnectionApplicationAnswer.APPLICATION_ALREADY_EXISTS,
            chatConnector.applyConnection(connectionRequest)
        )

        verify(connectionRequestRepository).exists(connectionRequest)
        verifyChatGetting()
    }

    @Test
    fun `success application`() {
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(Chat())
        whenever(connectionRequestRepository.exists(any()))
            .thenReturn(false)

        assertEquals(
            ConnectionApplicationAnswer.SUCCESS_APPLICATION,
            chatConnector.applyConnection(connectionRequest)
        )

        verify(connectionRequestRepository).exists(connectionRequest)
        verify(connectionRequestRepository).save(connectionRequest)
        verifyChatGetting()
    }

    @Test
    fun `confirm not existed application`() {
        whenever(connectionRequestRepository.exists(any()))
            .thenReturn(false)

        assertEquals(
            ConnectionConfirmationAnswer.APPLICATION_NOT_EXISTS,
            chatConnector.confirmConnection(connectionRequest)
        )

        verify(connectionRequestRepository).exists(connectionRequest)
    }

    private fun verifyChatGetting() {
        verify(chatRepository).getByMessengerAndId(
            connectionRequest.sourceMessenger,
            connectionRequest.sourceChatId
        )
        verify(chatRepository).getByMessengerAndId(
            connectionRequest.destinationMessenger,
            connectionRequest.destinationChatId
        )
    }

    @Test
    fun `destination chat not exists in confirmation`() {
        whenever(connectionRequestRepository.exists(connectionRequest))
            .thenReturn(true)
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(Chat())
            .thenReturn(null)

        assertEquals(
            ConnectionConfirmationAnswer.DESTINATION_CHAT_NOT_REGISTERED,
            chatConnector.confirmConnection(connectionRequest)
        )

        verify(connectionRequestRepository).exists(connectionRequest)
        verify(connectionRequestRepository).delete(connectionRequest)
        verifyChatGetting()
    }

    @Test
    fun `source chat not exists in confirmation`() {
        whenever(connectionRequestRepository.exists(connectionRequest))
            .thenReturn(true)
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(null)

        assertEquals(
            ConnectionConfirmationAnswer.SOURCE_CHAT_NOT_REGISTERED,
            chatConnector.confirmConnection(connectionRequest)
        )
        verify(connectionRequestRepository).exists(connectionRequest)
        verify(connectionRequestRepository).delete(connectionRequest)
        verifyChatGetting()
    }

    @Test
    fun `merge conflict in confirmation`() {
        val sourceChat = Chat(
            mutableMapOf(
                connectionRequest.sourceMessenger to connectionRequest.sourceChatId
            )
        )
        val destinationChat = Chat(
            mutableMapOf(
                connectionRequest.destinationMessenger to connectionRequest.destinationChatId
            )
        )

        whenever(connectionRequestRepository.exists(connectionRequest))
            .thenReturn(true)
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(sourceChat)
            .thenReturn(destinationChat)
        whenever(chatRepository.merge(any(), any()))
            .thenReturn(false)

        assertEquals(
            ConnectionConfirmationAnswer.MERGE_CONFLICT,
            chatConnector.confirmConnection(connectionRequest)
        )

        verify(connectionRequestRepository).exists(connectionRequest)
        verify(connectionRequestRepository).delete(connectionRequest)
        verify(chatRepository).merge(sourceChat, destinationChat)
        verifyChatGetting()
    }

    @Test
    fun `success confirmation`() {
        val sourceChat = Chat(
            mutableMapOf(
                connectionRequest.sourceMessenger to connectionRequest.sourceChatId
            )
        )
        val destinationChat = Chat(
            mutableMapOf(
                connectionRequest.destinationMessenger to connectionRequest.destinationChatId
            )
        )

        whenever(connectionRequestRepository.exists(connectionRequest))
            .thenReturn(true)
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(sourceChat)
            .thenReturn(destinationChat)
        whenever(chatRepository.merge(any(), any()))
            .thenReturn(true)

        assertEquals(
            ConnectionConfirmationAnswer.SUCCESS_CONFIRMATION,
            chatConnector.confirmConnection(connectionRequest)
        )

        verify(connectionRequestRepository).exists(connectionRequest)
        verify(connectionRequestRepository).delete(connectionRequest)
        verify(chatRepository).merge(sourceChat, destinationChat)
        verifyChatGetting()
    }
}