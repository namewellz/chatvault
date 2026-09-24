package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.persistence.dto.AttachmentInfoDTO
import dev.marcal.chatvault.persistence.dto.MessageJumpDTO
import dev.marcal.chatvault.persistence.entity.MessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface MessageCrudRepository : JpaRepository<MessageEntity, Long> {

    fun countByChatId(chatId: Long): Long

    @Query("""
        SELECT m FROM MessageEntity m 
        WHERE 0=0 
            AND m.chatId = :chatId
            AND (:query = '' 
                  OR ((LOWER(m.author) LIKE LOWER(CONCAT('%', :query, '%')) 
                  OR LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')))))
    """)
    fun findAllByChatIdIs(
        @Param("query") query: String,
        @Param("chatId") chatId: Long,
        pageable: Pageable
    ): Page<MessageEntity>


    fun findMessageEntityByIdAndChatId(id: Long, chatId: Long): MessageEntity?

    fun findTopByChatIdOrderByIdDesc(chatId: Long): MessageEntity?

    @Query("SELECT new dev.marcal.chatvault.persistence.dto.AttachmentInfoDTO(m.id, m.attachmentName) FROM MessageEntity m WHERE m.chatId = :chatId AND m.attachmentPath IS NOT NULL")
    fun findMessageIdByChatIdAndAttachmentExists(chatId: Long): List<AttachmentInfoDTO>

    fun deleteAllByChatId(chatId: Long)

    // Buscar mensagens a partir de uma data
    @Query("""
        SELECT new dev.marcal.chatvault.persistence.dto.MessageJumpDTO(
            m.id, 
            m.content, 
            m.author,
            m.authorType,
            m.createdAt,
            m.attachmentName,
            m.attachmentPath
        )
        FROM MessageEntity m 
        WHERE m.chatId = :chatId 
        AND m.createdAt >= :startDateTime
        ORDER BY m.createdAt ASC
    """)
    fun findMessagesFromDate(
        @Param("chatId") chatId: Long,
        @Param("startDateTime") startDateTime: LocalDateTime,
        pageable: Pageable
    ): Page<MessageJumpDTO>

    // Buscar todas as datas únicas de um chat
    @Query("""
        SELECT DISTINCT CAST(m.createdAt AS date)
        FROM MessageEntity m 
        WHERE m.chatId = :chatId
        ORDER BY CAST(m.createdAt AS date) ASC
    """)
    fun findDistinctDatesByChatId(
        @Param("chatId") chatId: Long
    ): List<LocalDate>

    // Contar mensagens em uma data específica
    @Query("""
        SELECT COUNT(m)
        FROM MessageEntity m 
        WHERE m.chatId = :chatId 
        AND CAST(m.createdAt AS date) = :date
    """)
    fun countMessagesByDate(
        @Param("chatId") chatId: Long,
        @Param("date") date: LocalDate
    ): Long
}