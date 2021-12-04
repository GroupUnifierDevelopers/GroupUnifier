package ru.itmo.sofwaredesign.groupunifier.common.domain

data class User(
    val login: String,
    val messenger: Messenger
) {
    val isGroupUnifierBot: Boolean
        get() {
            return messenger.botLogin == login
        }
}