package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.output.AttachmentImportOutput
import java.io.InputStream

interface AttachmentZipImporter {

    fun execute(chatId: Long, inputStream: InputStream, overwrite: Boolean): AttachmentImportOutput

}
