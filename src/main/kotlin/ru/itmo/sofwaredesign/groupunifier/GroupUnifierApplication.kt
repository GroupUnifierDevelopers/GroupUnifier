package ru.itmo.sofwaredesign.groupunifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GroupUnifierApplication

fun main(args: Array<String>) {
	runApplication<GroupUnifierApplication>(*args)
}
