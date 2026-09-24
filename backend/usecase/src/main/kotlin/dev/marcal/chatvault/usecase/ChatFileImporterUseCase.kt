package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.bucket_service.BucketService
import dev.marcal.chatvault.in_out_boundary.input.FileTypeInputEnum
import dev.marcal.chatvault.in_out_boundary.input.NewChatInput
import dev.marcal.chatvault.in_out_boundary.input.NewMessagePayloadInput
import dev.marcal.chatvault.in_out_boundary.output.exceptions.ChatImporterException
import dev.marcal.chatvault.model.BucketFile
import dev.marcal.chatvault.model.ChatNamePatternMatcher
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.ChatCreator
import dev.marcal.chatvault.service.ChatFileImporter
import dev.marcal.chatvault.service.ChatMessageParser
import dev.marcal.chatvault.service.MessageCreator
import dev.marcal.chatvault.usecase.mapper.toNewMessageInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.util.zip.ZipInputStream

@Service
class ChatFileImporterUseCase(
    private val chatMessageParser: ChatMessageParser,
    private val messageCreator: MessageCreator,
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
    private val chatCreator: ChatCreator
) : ChatFileImporter {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    override fun execute(chatId: Long, inputStream: InputStream, fileType: FileTypeInputEnum) {
        when (fileType) {
            FileTypeInputEnum.ZIP -> {
                iterateOverZip(chatId, inputStream)
            }

            FileTypeInputEnum.TEXT -> {
                createMessages(inputStream = inputStream, chatId = chatId)
            }

            else -> throw IllegalStateException("file type $fileType not supported")
        }


    }

    override fun execute(chatName: String?, inputStream: InputStream, fileType: FileTypeInputEnum) {
        val chatId =
            chatName?.let { chatRepository.findChatBucketInfoByChatName(it)?.chatId } ?: createTodoChat(chatName)
        execute(chatId, inputStream, fileType)
    }

    private fun createTodoChat(chatName: String?): Long {
        val tempChatName = chatName?.takeIf { it.isNotEmpty() } ?: "todo imported at ${LocalDateTime.now()}"
        chatCreator.executeIfNotExists(NewChatInput(name = tempChatName))
        return requireNotNull(chatRepository.findChatBucketInfoByChatName(tempChatName)?.chatId) { "temp chat creation fails: chatName: $tempChatName" }
    }

    private fun iterateOverZip(chatId: Long, inputStream: InputStream) {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId = chatId)
                ?: throw ChatImporterException("Chat id $chatId was not found. File import failed.")
        val bucket = chatBucketInfo.bucket.withPath("/")

        val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))

        logger.info("zip import started: chatId=$chatId")
        var entryCount = 0
        var mediaCount = 0

        var entry = zipInputStream.nextEntry
        while (entry != null) {
            entryCount++
            val fileName = entry.name
            logger.info("[$entryCount] entry: $fileName")

            if (ChatNamePatternMatcher.matches(fileName)) {
                // Chat text files are small — load into memory so we can save and parse in one pass
                val byteArray = readBytes(zipInputStream)
                logger.info("[$entryCount] parsing chat file: $fileName (${byteArray.size / 1024} KB)")
                bucketService.save(BucketFile(bytes = byteArray, fileName = fileName, address = bucket))
                execute(chatId = chatId, inputStream = ByteArrayInputStream(byteArray), fileType = FileTypeInputEnum.TEXT)
            } else {
                // Media files can be large — stream directly to disk without buffering in memory
                mediaCount++
                bucketService.save(BucketFile(stream = zipInputStream, fileName = fileName, address = bucket))
                logger.info("[$entryCount] saved media: $fileName")
            }

            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        logger.info("zip import finished: chatId=$chatId entries=$entryCount media=$mediaCount")
    }

    private fun readBytes(zipInputStream: ZipInputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var len: Int
        while (zipInputStream.read(buffer).also { len = it } > 0) {
            out.write(buffer, 0, len)
        }
        return out.toByteArray()
    }

    private fun createMessages(inputStream: InputStream, chatId: Long) {
        val messages = chatMessageParser.parseAndTransform(inputStream) { messageOutput ->
            messageOutput.toNewMessageInput(chatId = chatId)
        }

        val count = chatRepository.countChatMessages(chatId)

        messageCreator.execute(
            NewMessagePayloadInput(
                chatId = chatId,
                eventSource = false,
                messages = messages
            )
        )

        val countUpdated = chatRepository.countChatMessages(chatId)

        val newMessages = countUpdated - count

        logger.info("imported $newMessages of ${messages.size} messages  to chatId=$chatId")
    }
}