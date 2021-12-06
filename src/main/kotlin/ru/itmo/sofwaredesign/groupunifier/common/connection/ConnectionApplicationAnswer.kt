package ru.itmo.sofwaredesign.groupunifier.common.connection

enum class ConnectionApplicationAnswer {
    DESTINATION_CHAT_NOT_REGISTERED,
    SOURCE_CHAT_NOT_REGISTERED,
    MERGE_CONFLICT,
    SUCCESS_APPLICATION,
    APPLICATION_ALREADY_EXISTS,
    SAME_CHAT,
}