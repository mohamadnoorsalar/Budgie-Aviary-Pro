package com.example.core.common

/**
 * Calculates offspring generation dynamically based on parent generation records
 * without hardcoding speculative genetic assumptions.
 *
 * Examples:
 * - Father: F1, Mother: F1 -> F2
 * - Father: F2, Mother: F1 -> F3 (maximum parental filial generation + 1)
 * - Father: P (Parental / Foundation), Mother: P -> F1
 * - Father: G1, Mother: G2 -> G3
 * - Parents without explicit generation (foundation stock) -> F1
 */
object GenerationCalculator {

    fun calculateOffspringGeneration(
        fatherGeneration: String?,
        motherGeneration: String?,
        hasKnownParents: Boolean = true
    ): String {
        val fGen = fatherGeneration?.trim()?.uppercase()
        val mGen = motherGeneration?.trim()?.uppercase()

        // Check for 'F' notation (F1, F2, F3...)
        val fNum = extractGenerationNumber(fGen, 'F')
        val mNum = extractGenerationNumber(mGen, 'F')

        if (fNum != null || mNum != null) {
            val maxGen = maxOf(fNum ?: 0, mNum ?: 0)
            return "F${maxGen + 1}"
        }

        // Check for 'G' notation (G1, G2, G3...)
        val gFNum = extractGenerationNumber(fGen, 'G')
        val gMNum = extractGenerationNumber(mGen, 'G')

        if (gFNum != null || gMNum != null) {
            val maxGen = maxOf(gFNum ?: 0, gMNum ?: 0)
            return "G${maxGen + 1}"
        }

        // Check for 'P' (Parental foundation)
        val isFParental = fGen?.startsWith("P") == true
        val isMParental = mGen?.startsWith("P") == true

        if (isFParental || isMParental) {
            return "F1"
        }

        // Default if parents are known birds in database
        return if (hasKnownParents) "F1" else "Unknown"
    }

    private fun extractGenerationNumber(genStr: String?, prefix: Char): Int? {
        if (genStr.isNullOrBlank()) return null
        if (!genStr.startsWith(prefix)) return null
        val numberPart = genStr.substring(1).filter { it.isDigit() }
        return numberPart.toIntOrNull()
    }
}
