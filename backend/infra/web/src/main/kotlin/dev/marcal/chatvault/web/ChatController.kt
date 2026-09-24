package dev.marcal.chatvault.web

import dev.marcal.chatvault.in_out_boundary.input.AttachmentCriteriaInput
import dev.marcal.chatvault.in_out_boundary.output.AttachmentImportOutput
import dev.marcal.chatvault.in_out_boundary.output.AttachmentInfoOutput
import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.service.*
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/chats")
class ChatController(
        private val chatLister: ChatLister,
        private val messageFinderByChatId: MessageFinderByChatId,
        private val attachmentFinder: AttachmentFinder,
        private val chatNameUpdater: ChatNameUpdater,
        private val attachmentInfoFinderByChatId: AttachmentInfoFinderByChatId,
        private val profileImageManager: ProfileImageManager,
        private val chatDeleter: ChatDeleter,
        private val attachmentZipImporter: AttachmentZipImporter
) {

    @GetMapping
    fun listChats(): List<ChatLastMessageOutput> {
        return chatLister.execute()
    }

    @GetMapping("{chatId}")
    fun findChatMessages(
            @PathVariable("chatId") chatId: Long,
            @RequestParam("query", required = false) query: String? = null,
            @SortDefault(
                    sort = ["createdAt", "id"],
                    direction = Sort.Direction.DESC
            ) pageable: Pageable
    ): Page<MessageOutput> {
        return messageFinderByChatId.execute(
                chatId = chatId,
                query  = query,
                pageable = pageable
        )
    }

    @DeleteMapping("{chatId}")
    fun deleteChatAndAssets(
            @PathVariable("chatId") chatId: Long,
    ) {
        return chatDeleter.execute(chatId)

    }

    @PatchMapping("{chatId}/chatName/{chatName}")
    fun update(
            @PathVariable chatId: Long,
            @PathVariable chatName: String,
    ): ResponseEntity<String> {
        chatNameUpdater.execute(chatId, chatName)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("{chatId}/messages/{messageId}/attachment")
    fun downloadAttachment(
            @PathVariable("chatId") chatId: Long,
            @PathVariable("messageId") messageId: Long,
            @RequestParam("format", required = false) format: String? = null
    ): ResponseEntity<Resource> {
        val criteria = AttachmentCriteriaInput(
                chatId = chatId,
                messageId = messageId
        )

        val resource = if (format == "mp3") {
            attachmentFinder.executeAsPlayable(criteria)
        } else {
            attachmentFinder.execute(criteria)
        }

        val cacheControl = CacheControl.maxAge(1, TimeUnit.DAYS)
        val disposition = if (format == "mp3") "inline" else "attachment"

        return ResponseEntity.ok()
                .contentType(resource.getMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "$disposition; filename=\"${resource.filename}\"")
                .cacheControl(cacheControl)
                .body(resource)
    }

    @GetMapping("{chatId}/attachments")
    fun downloadAttachment(
            @PathVariable("chatId") chatId: Long
    ): ResponseEntity<Sequence<AttachmentInfoOutput>> {
        val cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(attachmentInfoFinderByChatId.execute(chatId))
    }

    @GetMapping("{chatId}/attachments/export")
    fun exportAttachmentsCsv(
            @PathVariable("chatId") chatId: Long
    ): ResponseEntity<String> {
        val rows = attachmentInfoFinderByChatId.execute(chatId).toList()

        val csv = buildString {
            appendLine("name,type,messageId")
            rows.forEach { row ->
                appendLine("${escapeCsv(row.name)},${attachmentTypeOf(row.name)},${row.id}")
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attachments-$chatId.csv\"")
                .contentType(MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv)
    }

    @PostMapping("{chatId}/attachments/import")
    fun importAttachments(
            @PathVariable("chatId") chatId: Long,
            @RequestParam("file") file: MultipartFile,
            @RequestParam("overwrite", required = false, defaultValue = "false") overwrite: Boolean
    ): ResponseEntity<AttachmentImportOutput> {
        val result = attachmentZipImporter.execute(chatId, file.inputStream, overwrite)
        return ResponseEntity.ok(result)
    }

    @PostMapping("{chatId}/profile-image")
    fun putProfileImage(
            @PathVariable("chatId") chatId: Long,
            @RequestParam("profile-image") file: MultipartFile
    ): ResponseEntity<Any> {
        profileImageManager.updateImage(file.inputStream, chatId)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("{chatId}/profile-image")
    fun getProfileImage(
            @PathVariable("chatId") chatId: Long
    ): ResponseEntity<Any> {
        val image = profileImageManager.getImage(chatId)

        val cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)

        return ResponseEntity.ok()
                .contentType(image.getMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${image.filename}\"")
                .cacheControl(cacheControl)
                .body(image)

    }
}

fun Resource.getMediaType(): MediaType {
    val knownAudioTypes = mapOf(
        "opus" to MediaType("audio", "ogg"),
        "oga" to MediaType("audio", "ogg"),
        "ogg" to MediaType("audio", "ogg")
    )
    val extension = this.filename?.substringAfterLast('.', "")?.lowercase()
    return knownAudioTypes[extension]
            ?: MediaTypeFactory.getMediaTypes(this.filename).firstOrNull()
            ?: MediaType.APPLICATION_OCTET_STREAM
}

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
private val VIDEO_EXTENSIONS = setOf("mp4", "avi", "mov")
private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "opus", "ogg", "oga", "m4a", "aac", "amr")

fun attachmentTypeOf(name: String): String {
    val extension = name.substringAfterLast('.', "").lowercase()
    return when (extension) {
        in IMAGE_EXTENSIONS -> "IMAGE"
        in VIDEO_EXTENSIONS -> "VIDEO"
        in AUDIO_EXTENSIONS -> "AUDIO"
        "pdf" -> "PDF"
        else -> "UNKNOWN"
    }
}

fun escapeCsv(value: String): String {
    return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
        "\"" + value.replace("\"", "\"\"") + "\""
    } else {
        value
    }
}