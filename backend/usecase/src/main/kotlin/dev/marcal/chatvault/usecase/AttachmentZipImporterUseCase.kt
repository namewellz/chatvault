package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.bucket_service.BucketService
import dev.marcal.chatvault.in_out_boundary.output.AttachmentImportOutput
import dev.marcal.chatvault.in_out_boundary.output.exceptions.ChatImporterException
import dev.marcal.chatvault.model.BucketFile
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.AttachmentZipImporter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

@Service
class AttachmentZipImporterUseCase(
    private val chatRepository: ChatRepository,
    private val bucketService: BucketService
) : AttachmentZipImporter {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute(chatId: Long, inputStream: InputStream, overwrite: Boolean): AttachmentImportOutput {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId)
                ?: throw ChatImporterException("Chat id $chatId was not found. Attachment import failed.")
        val bucket = chatBucketInfo.bucket.withPath("/")

        val existingNames = chatRepository.findAttachmentMessageIdsByChatId(chatId)
            .map { it.name }
            .toHashSet()

        var imported = 0
        var skipped = 0
        var ignored = 0

        val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val fileName = sanitizeFileName(entry.name)

            if (entry.isDirectory || fileName == null) {
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
                continue
            }

            when {
                fileName !in existingNames -> {
                    ignored++
                    logger.info("attachment '$fileName' has no matching message in chat $chatId, ignored")
                }

                bucketService.exists(BucketFile(fileName = fileName, address = bucket)) && !overwrite -> {
                    skipped++
                    logger.info("attachment '$fileName' already exists, skipped")
                }

                else -> {
                    bucketService.save(BucketFile(stream = zipInputStream, fileName = fileName, address = bucket))
                    imported++
                    logger.info("attachment '$fileName' imported")
                }
            }

            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()

        logger.info("attachment import finished: chatId=$chatId imported=$imported skipped=$skipped ignored=$ignored")
        return AttachmentImportOutput(imported = imported, skipped = skipped, ignored = ignored)
    }

    private fun sanitizeFileName(entryName: String): String? {
        val base = entryName.replace('\\', '/').substringAfterLast('/')
        return base.takeIf { it.isNotBlank() }
    }
}
