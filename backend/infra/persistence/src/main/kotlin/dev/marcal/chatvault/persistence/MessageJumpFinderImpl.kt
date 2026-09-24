package dev.marcal.chatvault.persistence

import dev.marcal.chatvault.in_out_boundary.output.MessageDateResponse
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.persistence.dto.MessageJumpDTO
import dev.marcal.chatvault.persistence.repository.MessageCrudRepository
import dev.marcal.chatvault.service.MessageJumpFinder
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MessageJumpFinderImpl(
    private val messageRepository: MessageCrudRepository
) : MessageJumpFinder {

    override fun findMessageNearDate(chatId: Long, date: LocalDate, pageSize: Int): MessageDateResponse {
        val startDateTime = date.atStartOfDay()
        val pageable = PageRequest.of(0, pageSize)

        val messagesPage = messageRepository.findMessagesFromDate(chatId, startDateTime, pageable)
        val totalOnDate = messageRepository.countMessagesByDate(chatId, date)

        return MessageDateResponse(
            date = date,
            messages = messagesPage.content.map { it.toOutput() },
            totalOnDate = totalOnDate,
            hasMore = messagesPage.hasNext()
        )
    }

    override fun getDistinctMessageDates(chatId: Long): List<LocalDate> {
        return messageRepository.findDistinctDatesByChatId(chatId)
    }

    private fun MessageJumpDTO.toOutput() = MessageOutput(
        id = id,
        author = author,
        authorType = authorType ?: "",
        content = content,
        attachmentName = attachmentName,
        createdAt = createdAt
    )
}
