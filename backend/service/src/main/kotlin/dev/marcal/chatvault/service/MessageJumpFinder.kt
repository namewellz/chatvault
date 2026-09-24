package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.output.MessageDateResponse
import java.time.LocalDate

interface MessageJumpFinder {
    fun findMessageNearDate(chatId: Long, date: LocalDate, pageSize: Int): MessageDateResponse
    fun getDistinctMessageDates(chatId: Long): List<LocalDate>
}
