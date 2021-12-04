package ru.itmo.sofwaredesign.groupunifier.common

import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import ru.itmo.sofwaredesign.groupunifier.common.domain.Chat
import ru.itmo.sofwaredesign.groupunifier.common.domain.Message
import ru.itmo.sofwaredesign.groupunifier.common.domain.Messenger
import ru.itmo.sofwaredesign.groupunifier.common.domain.User
import ru.itmo.sofwaredesign.groupunifier.common.repository.ChatRepository

private const val TEST_MESSAGE = "test massage"
private const val TEST_USER = "testUser"
private const val FROM_TEST_CHAT_ID = "from test chat id"
private const val TO_TEST_CHAT_ID = "to test chat id"
private const val TEST_BOT_LOGIN = "testBotLogin"

internal class ForwarderTest {

    @Test
    fun `forward message`() = getTestData().run {
        whenever(
            mockChatRepository
                .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        )
            .thenReturn(chat)

        forwarder.forwardMessage(message)

        verify(mockChatRepository)
            .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        verify(toMockMessenger)
            .sendMessage(TO_TEST_CHAT_ID, message)
    }

    @Test
    fun `bot added to chat, but chat is not registered`() = getTestData().run {
        whenever(
            mockChatRepository
                .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        )
            .thenReturn(null)

        forwarder.forwardMessage(message)

        verify(mockChatRepository)
            .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        verify(toMockMessenger, times(0))
            .sendMessage(TO_TEST_CHAT_ID, message)
    }

    @Test
    fun `Do not forward message from bot`() = getTestData(
        userLogin = TEST_BOT_LOGIN
    ).run {
        whenever(
            mockChatRepository
                .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        )
            .thenReturn(null)

        forwarder.forwardMessage(message)

        verify(mockChatRepository, times(0))
            .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        verify(toMockMessenger, times(0))
            .sendMessage(TO_TEST_CHAT_ID, message)
    }

    @Test
    fun `rutry until success`() = getTestData().run {
        whenever(
            mockChatRepository
                .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        )
            .thenReturn(chat)
        whenever(toMockMessenger.sendMessage(any(), any()))
            .thenThrow(RuntimeException::class.java)
            .thenThrow(RuntimeException::class.java)
            .thenThrow(RuntimeException::class.java)
            .then {}

        forwarder.forwardMessage(message)

        verify(mockChatRepository)
            .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        verify(toMockMessenger, times(4))
            .sendMessage(TO_TEST_CHAT_ID, message)
    }

    @Test
    fun `retry a limited number of times`() = getTestData().run {
        whenever(
            mockChatRepository
                .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        )
            .thenReturn(chat)
        whenever(toMockMessenger.sendMessage(any(), any()))
            .thenThrow(RuntimeException::class.java)

        forwarder.forwardMessage(message)

        verify(mockChatRepository)
            .getByMessengerAndId(fromMockMessenger, FROM_TEST_CHAT_ID)
        verify(toMockMessenger, times(5))
            .sendMessage(TO_TEST_CHAT_ID, message)
    }

    private fun getTestData(
        userLogin: String = TEST_USER
    ): TestData {
        val fromMockMessenger: Messenger = mock()
        whenever(fromMockMessenger.botLogin)
            .thenReturn(TEST_BOT_LOGIN)
        val message = Message(
            TEST_MESSAGE,
            User(userLogin, fromMockMessenger),
            FROM_TEST_CHAT_ID,
            fromMockMessenger
        )
        val toMockMessenger: Messenger = mock()
        val chat = Chat()
        chat.implementations[toMockMessenger] = TO_TEST_CHAT_ID
        val mockChatRepository: ChatRepository = mock()
        val forwarder = Forwarder(mockChatRepository)
        return TestData(
            fromMockMessenger,
            message,
            toMockMessenger,
            chat,
            mockChatRepository,
            forwarder
        )
    }

    private data class TestData constructor(
        val fromMockMessenger: Messenger,
        val message: Message,
        val toMockMessenger: Messenger,
        val chat: Chat,
        val mockChatRepository: ChatRepository,
        val forwarder: Forwarder
    )
}