package dev.marcal.chatvault.in_out_boundary.output

import java.time.LocalDate

data class MessageDateResponse(
    val date: LocalDate,
    val messages: List<MessageOutput>,
    val totalOnDate: Long,
    val hasMore: Boolean
)
