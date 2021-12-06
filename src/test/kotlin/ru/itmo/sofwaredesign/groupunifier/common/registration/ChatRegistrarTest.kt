package ru.itmo.sofwaredesign.groupunifier.common.registration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import ru.itmo.sofwaredesign.groupunifier.common.domain.Chat
import ru.itmo.sofwaredesign.groupunifier.common.domain.Messenger
import ru.itmo.sofwaredesign.groupunifier.common.registration.RegistrationAnswer.*
import ru.itmo.sofwaredesign.groupunifier.common.repository.ChatRepository

internal class ChatRegistrarTest {
    private val chatRepository: ChatRepository = mock()
    private val chatRegistrar = ChatRegistrar(chatRepository)

    private val messenger: Messenger = mock()
    private val chatId = "123"

    @AfterEach
    fun after() {
        verify(chatRepository).getByMessengerAndId(messenger, chatId)
        verifyNoMoreInteractions(chatRepository)
    }

    @Test
    fun `success registration`() {
        whenever(chatRepository.getByMessengerAndId(any(), any())).thenReturn(null)

        assertEquals(REGISTRATION_SUCCESS, chatRegistrar.registerChat(messenger, chatId))

        verify(chatRepository).save(Chat(mutableMapOf(messenger to chatId)))
    }

    @Test
    fun `registration of already registered chat`() {
        whenever(chatRepository.getByMessengerAndId(any(), any())).thenReturn(Chat())

        assertEquals(ALREADY_REGISTERED, chatRegistrar.registerChat(messenger, chatId))
    }

    @Test
    fun `success unregistration when there are no more chats`() {
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(Chat(mutableMapOf(messenger to chatId)))

        assertEquals(UNREGISTRATION_SUCCESS, chatRegistrar.unregisterChat(messenger, chatId))

        verify(chatRepository).delete(Chat(mutableMapOf(messenger to chatId)))
    }

    @Test
    fun `success unregistration when another chat exists`() {
        val anotherMessenger: Messenger = mock()
        val anotherChatId = "456"
        whenever(chatRepository.getByMessengerAndId(any(), any()))
            .thenReturn(Chat(mutableMapOf(messenger to chatId, anotherMessenger to anotherChatId)))

        assertEquals(UNREGISTRATION_SUCCESS, chatRegistrar.unregisterChat(messenger, chatId))

        verify(chatRepository).save(Chat(mutableMapOf(anotherMessenger to anotherChatId)))
    }

    @Test
    fun `unregistration of not registered chat`() {
        whenever(chatRepository.getByMessengerAndId(any(), any())).thenReturn(null)

        assertEquals(NOT_REGISTERED, chatRegistrar.unregisterChat(messenger, chatId))
    }
}