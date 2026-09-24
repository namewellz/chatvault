package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.bucket_service.BucketService
import dev.marcal.chatvault.in_out_boundary.input.AttachmentCriteriaInput
import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentNotFoundException
import dev.marcal.chatvault.model.BucketFile
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.AttachmentFinder
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class AttachmentFinderUseCase(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository
) : AttachmentFinder {
    override fun execute(criteriaInput: AttachmentCriteriaInput): Resource {
        val bucketFile = findBucketFile(criteriaInput)
        return bucketService.loadFileAsResource(bucketFile)
    }

    override fun executeAsPlayable(criteriaInput: AttachmentCriteriaInput): Resource {
        val bucketFile = findBucketFile(criteriaInput)

        val extension = bucketFile.fileName.substringAfterLast('.', "").lowercase()
        return if (extension in AUDIO_TRANSCODE_EXTENSIONS) {
            bucketService.transcodeAudioToMp3(bucketFile)
        } else {
            bucketService.loadFileAsResource(bucketFile)
        }
    }

    private fun findBucketFile(criteriaInput: AttachmentCriteriaInput): BucketFile {
        val message =
            chatRepository.findMessageBy(chatId = criteriaInput.chatId, messageId = criteriaInput.messageId)
                ?: throw AttachmentNotFoundException()

        return message.content.attachment?.toBucketFile()
            ?: throw AttachmentNotFoundException("the message exists but there are no attachments linked to it")
    }

    companion object {
        private val AUDIO_TRANSCODE_EXTENSIONS = setOf("opus", "ogg", "oga")
    }
}