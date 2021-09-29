package ru.itmo.sofwaredesign.groupunifier.common

import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import ru.itmo.sofwaredesign.groupunifier.common.domain.Chat
import ru.itmo.sofwaredesign.groupunifier.common.domain.Message
import ru.itmo.sofwaredesign.groupunifier.common.domain.Messenger
import ru.itmo.sofwaredesign.groupunifier.common.domain.User
import ru.itmo.sofwaredesign.groupunifier.common.repository.ChatRepository

class ForwarderTest {

    @Test
    fun forwardTest() {
        val fromMockMessenger: Messenger = mock()
        val message = Message(
            "test message",
            User("testUser", fromMockMessenger),
            "from chat id",
            fromMockMessenger
        )
        val toMockMessenger: Messenger = mock()
        val chat = Chat()
        chat.implementations[toMockMessenger] = "to chat id"
        val mockChatRepository: ChatRepository = mock()
        whenever(
            mockChatRepository
                .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        )
            .thenReturn(chat)

        val forwarder = Forwarder(mockChatRepository)
        forwarder.forwardMessage(message)

        verify(mockChatRepository)
            .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        verify(toMockMessenger)
            .sendMessage("to chat id", message)
    }

    @Test
    fun noChatTest() {
        val fromMockMessenger: Messenger = mock()
        val message = Message(
            "test message",
            User("testUser", fromMockMessenger),
            "from chat id",
            fromMockMessenger
        )
        val toMockMessenger: Messenger = mock()
        val chat = Chat()
        chat.implementations[toMockMessenger] = "to chat id"
        val mockChatRepository: ChatRepository = mock()
        whenever(
            mockChatRepository
                .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        )
            .thenReturn(null)

        val forwarder = Forwarder(mockChatRepository)
        forwarder.forwardMessage(message)

        verify(mockChatRepository)
            .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        verify(toMockMessenger, times(0))
            .sendMessage("to chat id", message)
    }

    @Test
    fun fromBotMessageTest() {
        val fromMockMessenger: Messenger = mock()
        whenever(fromMockMessenger.botLogin)
            .thenReturn("botLogin")
        val message = Message(
            "test message",
            User("botLogin", fromMockMessenger),
            "from chat id",
            fromMockMessenger
        )
        val toMockMessenger: Messenger = mock()
        val chat = Chat()
        chat.implementations[toMockMessenger] = "to chat id"
        val mockChatRepository: ChatRepository = mock()
        whenever(
            mockChatRepository
                .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        )
            .thenReturn(null)

        val forwarder = Forwarder(mockChatRepository)
        forwarder.forwardMessage(message)

        verify(mockChatRepository, times(0))
            .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        verify(toMockMessenger, times(0))
            .sendMessage("to chat id", message)
    }

    @Test
    fun retrySuccessTest() {
        val fromMockMessenger: Messenger = mock()
        val message = Message(
            "test message",
            User("testUser", fromMockMessenger),
            "from chat id",
            fromMockMessenger
        )
        val toMockMessenger: Messenger = mock()
        val chat = Chat()
        chat.implementations[toMockMessenger] = "to chat id"
        val mockChatRepository: ChatRepository = mock()
        whenever(
            mockChatRepository
                .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        )
            .thenReturn(chat)
        whenever(toMockMessenger.sendMessage(any(), any()))
            .thenThrow(RuntimeException::class.java)
            .thenThrow(RuntimeException::class.java)
            .thenThrow(RuntimeException::class.java)
            .then {}

        val forwarder = Forwarder(mockChatRepository)
        forwarder.forwardMessage(message)

        verify(mockChatRepository)
            .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        verify(toMockMessenger, times(4))
            .sendMessage("to chat id", message)
    }

    @Test
    fun retryFailTest() {
        val fromMockMessenger: Messenger = mock()
        val message = Message(
            "test message",
            User("testUser", fromMockMessenger),
            "from chat id",
            fromMockMessenger
        )
        val toMockMessenger: Messenger = mock()
        val chat = Chat()
        chat.implementations[toMockMessenger] = "to chat id"
        val mockChatRepository: ChatRepository = mock()
        whenever(
            mockChatRepository
                .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        )
            .thenReturn(chat)
        whenever(toMockMessenger.sendMessage(any(), any()))
            .thenThrow(RuntimeException::class.java)

        val forwarder = Forwarder(mockChatRepository)
        forwarder.forwardMessage(message)

        verify(mockChatRepository)
            .getChatByMessengerAndId(fromMockMessenger, "from chat id")
        verify(toMockMessenger, times(5))
            .sendMessage("to chat id", message)
    }
}