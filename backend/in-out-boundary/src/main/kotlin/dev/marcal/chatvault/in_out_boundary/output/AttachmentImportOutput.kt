package dev.marcal.chatvault.in_out_boundary.output

data class AttachmentImportOutput(
    val imported: Int,
    val skipped: Int,
    val ignored: Int
)
