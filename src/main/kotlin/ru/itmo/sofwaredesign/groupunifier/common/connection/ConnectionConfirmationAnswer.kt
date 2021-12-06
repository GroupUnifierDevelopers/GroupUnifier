package ru.itmo.sofwaredesign.groupunifier.common.connection

enum class ConnectionConfirmationAnswer {
    DESTINATION_CHAT_NOT_REGISTERED,
    SOURCE_CHAT_NOT_REGISTERED,
    APPLICATION_NOT_EXISTS,
    SUCCESS_CONFIRMATION,
    MERGE_CONFLICT
}