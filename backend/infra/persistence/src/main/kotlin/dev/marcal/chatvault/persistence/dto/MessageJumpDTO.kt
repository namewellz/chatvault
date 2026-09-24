package dev.marcal.chatvault.persistence.dto

import java.time.LocalDateTime

data class MessageJumpDTO(
    val id: Long,
    val content: String,
    val author: String,
    val authorType: String?,
    val createdAt: LocalDateTime,
    val attachmentName: String?,
    val attachmentPath: String?
)