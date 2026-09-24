package dev.marcal.chatvault.web

import dev.marcal.chatvault.in_out_boundary.input.NewMessageInput
import dev.marcal.chatvault.in_out_boundary.output.MessageDateResponse
import dev.marcal.chatvault.service.MessageCreator
import dev.marcal.chatvault.service.MessageJumpFinder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageCreator: MessageCreator,
    private val messageJumpFinder: MessageJumpFinder
) {

    @PostMapping
    fun newMessage(@RequestBody input: NewMessageInput): ResponseEntity<Unit> {
        messageCreator.execute(input)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{chatId}/jump-to-date")
    fun jumpToDate(
        @PathVariable chatId: Long,
        @RequestParam date: LocalDate,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<MessageDateResponse> {
        val result = messageJumpFinder.findMessageNearDate(chatId, date, pageSize)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{chatId}/available-dates")
    fun availableDates(
        @PathVariable chatId: Long
    ): ResponseEntity<List<LocalDate>> {
        val dates = messageJumpFinder.getDistinctMessageDates(chatId)
        return ResponseEntity.ok(dates)
    }
}
