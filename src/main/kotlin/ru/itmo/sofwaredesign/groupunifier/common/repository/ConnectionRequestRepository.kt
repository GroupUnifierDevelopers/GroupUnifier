package ru.itmo.sofwaredesign.groupunifier.common.repository

import ru.itmo.sofwaredesign.groupunifier.common.domain.ConnectionRequest

interface ConnectionRequestRepository {
    fun save(connectionRequest: ConnectionRequest): Boolean

    fun delete(connectionRequest: ConnectionRequest)

    fun exists(connectionRequest: ConnectionRequest): Boolean
}