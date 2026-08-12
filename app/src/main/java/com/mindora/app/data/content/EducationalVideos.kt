package com.mindora.app.data.content

/**
 * Curated real educational math videos (Khan Academy on YouTube).
 */
object EducationalVideos {
    data class Video(val id: String, val title: String) {
        val watchUrl: String get() = "https://www.youtube.com/watch?v=$id"
        val embedUrl: String
            get() = "https://www.youtube.com/embed/$id?playsinline=1&rel=0&modestbranding=1"
    }

    // Verified Khan Academy (and closely related) education videos
    val placeValue = Video("wx2gI8iwMCA", "Introduction to place value — Khan Academy")
    val placeValueAdding = Video("7_QPAdHILzw", "Place value when adding ones — Khan Academy")
    val multiDigitAdd = Video("DqaHhReVpZI", "Adding multi-digit numbers — Khan Academy")
    val fractionsIntro = Video("52ZlXsFJULI", "Adding and subtracting fractions — Khan Academy")
    val algebraFractions = Video("w7NhLkQynS8", "Algebraic expressions with fractions — Khan Academy")
    val multiplyDecimals = Video("JEHejQphIYc", "Multiplying decimals — Khan Academy")
    val divideDecimals = Video("Z_NHrwK6ALE", "Dividing a decimal by a whole number — Khan Academy")
    val decimalsByPowers = Video("6fLNcGSa_L4", "Decimals ×÷ 10, 100, 1000 — Khan Academy")

    // Additional well-known Khan Academy math videos
    val library = listOf(
        placeValue, placeValueAdding, multiDigitAdd, fractionsIntro, algebraFractions,
        multiplyDecimals, divideDecimals, decimalsByPowers,
        Video("HpmHMnTLbsM", "Introduction to ratios — Khan Academy"),
        Video("teUsiSQ25Ik", "Negative numbers introduction — Khan Academy"),
        Video("ClYdw4d4OmA", "Order of operations — Khan Academy"),
        Video("vDqOoI-4Z6M", "What is a variable? — Khan Academy"),
        Video("9Ek61w1LxTw", "Solving linear equations — Khan Academy"),
        Video("R948Tsyq4vA", "Slope of a line — Khan Academy"),
        Video("i7j0oBArOVk", "Quadratic equations — Khan Academy"),
        Video("AA6RfgP-AHU", "Pythagorean theorem — Khan Academy"),
        Video("uhxtUt_-GyM", "Mean, median, and mode — Khan Academy"),
        Video("uzkc_q0cjR0", "Basic probability — Khan Academy"),
        Video("PUBNsHVmKIc", "Basic trigonometry — Khan Academy"),
        Video("ANyVpMS3u8o", "Derivative as slope of tangent — Khan Academy")
    )

    fun forTopicKey(key: String): List<Video> = when (key) {
        "numbers", "counting", "place_value" ->
            listOf(placeValue, placeValueAdding, multiDigitAdd)
        "addition", "subtraction" ->
            listOf(multiDigitAdd, placeValueAdding, placeValue)
        "multiplication", "division" ->
            listOf(multiplyDecimals, divideDecimals, decimalsByPowers)
        "fractions" ->
            listOf(fractionsIntro, algebraFractions, multiplyDecimals)
        "decimals", "percent" ->
            listOf(multiplyDecimals, divideDecimals, decimalsByPowers, fractionsIntro)
        "ratios", "proportions" ->
            listOf(Video("HpmHMnTLbsM", "Introduction to ratios — Khan Academy"), fractionsIntro, decimalsByPowers)
        "prealgebra", "integers" ->
            listOf(
                Video("teUsiSQ25Ik", "Negative numbers introduction — Khan Academy"),
                Video("ClYdw4d4OmA", "Order of operations — Khan Academy"),
                Video("vDqOoI-4Z6M", "What is a variable? — Khan Academy")
            )
        "algebra" ->
            listOf(
                Video("vDqOoI-4Z6M", "What is a variable? — Khan Academy"),
                Video("9Ek61w1LxTw", "Solving linear equations — Khan Academy"),
                Video("R948Tsyq4vA", "Slope of a line — Khan Academy"),
                algebraFractions
            )
        "geometry" ->
            listOf(
                Video("AA6RfgP-AHU", "Pythagorean theorem — Khan Academy"),
                Video("PUBNsHVmKIc", "Basic trigonometry — Khan Academy"),
                multiDigitAdd
            )
        "quadratics" ->
            listOf(Video("i7j0oBArOVk", "Quadratic equations — Khan Academy"), algebraFractions)
        "advanced" ->
            listOf(
                Video("PUBNsHVmKIc", "Basic trigonometry — Khan Academy"),
                Video("ANyVpMS3u8o", "Derivative as slope of tangent — Khan Academy"),
                Video("i7j0oBArOVk", "Quadratic equations — Khan Academy")
            )
        "stats" ->
            listOf(
                Video("uhxtUt_-GyM", "Mean, median, and mode — Khan Academy"),
                Video("uzkc_q0cjR0", "Basic probability — Khan Academy"),
                Video("HpmHMnTLbsM", "Introduction to ratios — Khan Academy")
            )
        else -> library.take(6)
    }

    fun pick(key: String, index: Int): Video {
        val list = forTopicKey(key).ifEmpty { library }
        return list[index.mod(list.size)]
    }

    fun extractYoutubeId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.startsWith("youtube:", ignoreCase = true)) {
            return trimmed.substringAfter(":").trim().ifBlank { null }
        }
        val patterns = listOf(
            Regex("""[?&]v=([A-Za-z0-9_-]{6,})"""),
            Regex("""youtu\.be/([A-Za-z0-9_-]{6,})"""),
            Regex("""youtube\.com/embed/([A-Za-z0-9_-]{6,})""")
        )
        for (p in patterns) {
            p.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }
        }
        return null
    }
}
