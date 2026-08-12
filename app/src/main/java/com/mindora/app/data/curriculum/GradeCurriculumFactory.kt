package com.mindora.app.data.curriculum

import com.mindora.app.data.content.EducationalVideos
import com.mindora.app.data.models.*

object GradeCurriculumFactory {

    private const val LESSONS_PER_TOPIC = 12

    private data class TopicSpec(
        val slug: String,
        val title: String,
        val blurb: String,
        val videoKey: String
    )

    private data class GradeSpec(
        val band: String,
        val prefix: String,
        val difficulty: String,
        val xpReward: Int,
        val energyCost: Int,
        val topics: List<TopicSpec>
    )

    private val gradeSpecs: Map<String, GradeSpec> = mapOf(
        "K-2" to GradeSpec(
            band = "K-2", prefix = "k2", difficulty = "easy", xpReward = 40, energyCost = 1,
            topics = listOf(
                TopicSpec("counting", "Counting Adventures", "count forward and backward", "counting"),
                TopicSpec("place_value", "Place Value Basics", "ones and tens", "place_value"),
                TopicSpec("addition", "Adding Numbers", "combine quantities", "addition"),
                TopicSpec("subtraction", "Taking Away", "subtract small numbers", "subtraction"),
                TopicSpec("shapes", "Shape Explorers", "identify 2D shapes", "geometry"),
                TopicSpec("numbers", "Number Neighbors", "compare and order numbers", "numbers"),
                TopicSpec("skip_counting", "Skip Counting", "count by 2s, 5s, and 10s", "counting"),
                TopicSpec("adding_tens", "Adding Tens", "add multiples of ten", "addition"),
                TopicSpec("subtract_within", "Subtract Within 20", "subtraction strategies", "subtraction"),
                TopicSpec("comparing", "Greater or Less", "compare two numbers", "numbers")
            )
        ),
        "3-5" to GradeSpec(
            band = "3-5", prefix = "g35", difficulty = "easy", xpReward = 55, energyCost = 2,
            topics = listOf(
                TopicSpec("multiplication", "Multiplication Missions", "equal groups and arrays", "multiplication"),
                TopicSpec("division", "Division Discoveries", "share equally", "division"),
                TopicSpec("fractions", "Fraction Foundations", "parts of a whole", "fractions"),
                TopicSpec("decimals", "Decimal Digits", "tenths and hundredths", "decimals"),
                TopicSpec("geometry", "Angles & Area", "perimeter and angles", "geometry"),
                TopicSpec("place_value", "Big Numbers", "thousands and beyond", "place_value"),
                TopicSpec("mult_facts", "Fact Fluency", "master multiplication facts", "multiplication"),
                TopicSpec("long_division", "Division Strategies", "divide with remainders", "division"),
                TopicSpec("fraction_equiv", "Equivalent Fractions", "same value, different look", "fractions"),
                TopicSpec("decimal_ops", "Decimal Operations", "add and subtract decimals", "decimals")
            )
        ),
        "6-8" to GradeSpec(
            band = "6-8", prefix = "g68", difficulty = "medium", xpReward = 70, energyCost = 2,
            topics = listOf(
                TopicSpec("ratios", "Ratio Reasoning", "compare quantities", "ratios"),
                TopicSpec("integers", "Integer Galaxy", "positive and negative numbers", "integers"),
                TopicSpec("prealgebra", "Pre-Algebra Prep", "variables and expressions", "prealgebra"),
                TopicSpec("fractions", "Fraction Operations", "multiply and divide fractions", "fractions"),
                TopicSpec("geometry", "Geometry in Space", "area, volume, and coordinates", "geometry"),
                TopicSpec("stats", "Data & Statistics", "mean, median, and graphs", "stats"),
                TopicSpec("proportions", "Proportional Thinking", "solve proportions", "ratios"),
                TopicSpec("equations", "Equation Solving", "one-step equations", "prealgebra"),
                TopicSpec("expressions", "Algebraic Expressions", "simplify and evaluate", "prealgebra"),
                TopicSpec("probability", "Probability Basics", "likelihood and chance", "stats")
            )
        ),
        "9-12" to GradeSpec(
            band = "9-12", prefix = "g912", difficulty = "medium", xpReward = 85, energyCost = 3,
            topics = listOf(
                TopicSpec("algebra", "Linear Algebra", "slope and equations", "algebra"),
                TopicSpec("geometry", "Proofs & Theorems", "triangles and circles", "geometry"),
                TopicSpec("quadratics", "Quadratic Quest", "parabolas and factoring", "quadratics"),
                TopicSpec("functions", "Function Fundamentals", "domain, range, and graphs", "functions"),
                TopicSpec("stats", "Statistics Studio", "distributions and inference intro", "stats"),
                TopicSpec("systems", "Systems of Equations", "solve pairs of equations", "algebra"),
                TopicSpec("polynomials", "Polynomial Power", "factor and expand", "quadratics"),
                TopicSpec("trig_geom", "Trigonometry Intro", "sine, cosine in triangles", "geometry"),
                TopicSpec("exponentials", "Exponential Growth", "exponents and logs preview", "functions"),
                TopicSpec("data_analysis", "Data Analysis", "scatter plots and correlation", "stats")
            )
        ),
        "College" to GradeSpec(
            band = "College", prefix = "col", difficulty = "hard", xpReward = 100, energyCost = 3,
            topics = listOf(
                TopicSpec("algebra", "College Algebra", "functions and equations", "algebra"),
                TopicSpec("advanced", "Calculus Foundations", "limits and derivatives", "advanced"),
                TopicSpec("stats", "College Statistics", "probability and inference", "stats"),
                TopicSpec("functions", "Advanced Functions", "composition and inverses", "functions"),
                TopicSpec("geometry", "Analytic Geometry", "vectors and conics", "geometry"),
                TopicSpec("calculus", "Differential Calculus", "rates of change", "advanced"),
                TopicSpec("linear_algebra", "Linear Systems", "matrices and vectors", "algebra"),
                TopicSpec("trigonometry", "Trigonometric Functions", "identities and graphs", "advanced"),
                TopicSpec("probability", "Probability Theory", "conditional probability", "stats"),
                TopicSpec("sequences", "Sequences & Series", "convergence and sums", "functions")
            )
        ),
        "Adult" to GradeSpec(
            band = "Adult", prefix = "adult", difficulty = "hard", xpReward = 100, energyCost = 3,
            topics = listOf(
                TopicSpec("algebra", "Practical Algebra", "real-world equations", "algebra"),
                TopicSpec("advanced", "Applied Advanced Math", "growth models and optimization", "advanced"),
                TopicSpec("stats", "Everyday Statistics", "interpret data confidently", "stats"),
                TopicSpec("functions", "Functions at Work", "model real situations", "functions"),
                TopicSpec("geometry", "Measurement & Design", "area, volume, scale", "geometry"),
                TopicSpec("finance", "Financial Literacy", "interest, budgets, percentages", "stats"),
                TopicSpec("problem_solving", "Workplace Math", "multi-step problem solving", "algebra"),
                TopicSpec("data_literacy", "Reading Charts", "graphs, tables, and trends", "stats"),
                TopicSpec("ratios_applied", "Ratios in Daily Life", "recipes, maps, and rates", "ratios"),
                TopicSpec("decimals_percent", "Decimals & Percents", "discounts and taxes", "decimals")
            )
        )
    )

    fun normalizeGrade(grade: String?): String {
        if (grade.isNullOrBlank()) return "K-2"
        val g = grade.trim().lowercase()
        return when {
            g in setOf("k", "k-2", "k2", "kindergarten", "grade k", "pre-k", "prek", "0", "1", "2",
                "grade 1", "grade 2", "first", "second", "1st", "2nd") -> "K-2"
            g in setOf("3", "4", "5", "3-5", "grade 3", "grade 4", "grade 5", "third", "fourth", "fifth") -> "3-5"
            g in setOf("6", "7", "8", "6-8", "grade 6", "grade 7", "grade 8", "middle school",
                "middle", "sixth", "seventh", "eighth") -> "6-8"
            g in setOf("9", "10", "11", "12", "9-12", "high school", "high", "grade 9", "grade 10",
                "grade 11", "grade 12", "freshman", "sophomore", "junior", "senior") -> "9-12"
            g.contains("college") || g.contains("university") || g.contains("undergrad") -> "College"
            g.contains("adult") || g.contains("ged") || g.contains("professional") -> "Adult"
            g.contains("k") && g.contains("2") -> "K-2"
            else -> "K-2"
        }
    }

    fun getSupportedGrades(): List<String> = gradeSpecs.keys.toList()

    fun topicsForGrade(grade: String): List<Topic> =
        buildCatalogForGrade(grade).topics

    fun lessonCountForGrade(grade: String): Int =
        buildCatalogForGrade(grade).lessons.size

    fun buildCatalog(): MathCatalog {
        val topics = mutableListOf<Topic>()
        val lessons = mutableListOf<Lesson>()
        gradeSpecs.keys.forEach { band ->
            val catalog = buildCatalogForGrade(band)
            topics += catalog.topics
            lessons += catalog.lessons
        }
        return MathCatalog(
            subject = SubjectInfo(
                id = "math",
                name = "Mathematics",
                description = "Complete K-12 and beyond math curriculum across all grade bands."
            ),
            topics = topics,
            lessons = lessons
        )
    }

    fun buildCatalogForGrade(grade: String): MathCatalog {
        val spec = gradeSpecs[normalizeGrade(grade)] ?: gradeSpecs.getValue("K-2")
        val topics = mutableListOf<Topic>()
        val lessons = mutableListOf<Lesson>()
        spec.topics.forEachIndexed { topicIndex, topicSpec ->
            val topicId = "${spec.prefix}_${topicSpec.slug}"
            val lessonIds = (1..LESSONS_PER_TOPIC).map { li ->
                "${topicId}_l${li.toString().padStart(2, '0')}"
            }
            topics += Topic(
                id = topicId,
                subjectId = "math",
                title = topicSpec.title,
                description = "[Grade ${spec.band}] ${topicSpec.blurb}",
                difficulty = spec.difficulty,
                order = topicIndex + 1,
                xpReward = spec.xpReward,
                energyCost = spec.energyCost,
                constellationX = constellationCoord(topicIndex, spec.topics.size, axis = 0),
                constellationY = constellationCoord(topicIndex, spec.topics.size, axis = 1),
                lessonIds = lessonIds
            )
            lessonIds.forEachIndexed { lessonIndex, lessonId ->
                lessons += buildLesson(
                    lessonId = lessonId,
                    topicId = topicId,
                    topicSpec = topicSpec,
                    gradeSpec = spec,
                    lessonIndex = lessonIndex + 1,
                    topicIndex = topicIndex
                )
            }
        }
        return MathCatalog(
            subject = SubjectInfo(
                id = "math",
                name = "Mathematics",
                description = "Math curriculum for grade band ${spec.band}."
            ),
            topics = topics,
            lessons = lessons
        )
    }

    private fun constellationCoord(index: Int, total: Int, axis: Int): Float {
        val cols = 5
        val row = index / cols
        val col = index % cols
        val rows = (total + cols - 1) / cols
        val x = 0.1f + (col.toFloat() / (cols - 1).coerceAtLeast(1)) * 0.8f
        val y = 0.1f + (row.toFloat() / (rows - 1).coerceAtLeast(1)) * 0.8f
        return if (axis == 0) x else y
    }

    private fun buildLesson(
        lessonId: String,
        topicId: String,
        topicSpec: TopicSpec,
        gradeSpec: GradeSpec,
        lessonIndex: Int,
        topicIndex: Int
    ): Lesson {
        val video = EducationalVideos.pick(topicSpec.videoKey, lessonIndex + topicIndex)
        val seed = lessonIndex + topicIndex * 17 + gradeSpec.prefix.hashCode()
        val title = "${topicSpec.title} — Part $lessonIndex"
        val contentText = contentFor(topicSpec.videoKey, gradeSpec.band, lessonIndex, seed)
        val examples = examplesFor(topicSpec.videoKey, lessonIndex, seed)
        val practiceQs = questionsFor(topicSpec.videoKey, lessonId, lessonIndex, seed, count = 2, prefix = "p")
        val quizQs = questionsFor(topicSpec.videoKey, lessonId, lessonIndex, seed + 99, count = 2, prefix = "q")

        return Lesson(
            id = lessonId,
            topicId = topicId,
            title = title,
            description = "[Grade ${gradeSpec.band}] Lesson $lessonIndex: ${topicSpec.blurb}",
            videoUrl = video.watchUrl,
            videoTitle = video.title,
            stages = listOf(
                LessonStage(
                    id = "${lessonId}_content",
                    type = StageType.CONTENT,
                    title = "Learn",
                    content = contentText
                ),
                LessonStage(
                    id = "${lessonId}_video",
                    type = StageType.VIDEO,
                    title = "Watch",
                    content = "Watch this video to deepen your understanding of ${topicSpec.title.lowercase()}.",
                    videoUrl = video.watchUrl
                ),
                LessonStage(
                    id = "${lessonId}_example",
                    type = StageType.EXAMPLE,
                    title = "Worked Examples",
                    content = "Study these step-by-step solutions.",
                    examples = examples
                ),
                LessonStage(
                    id = "${lessonId}_practice",
                    type = StageType.PRACTICE,
                    title = "Practice",
                    content = "Try these problems on your own.",
                    questions = practiceQs
                ),
                LessonStage(
                    id = "${lessonId}_quiz",
                    type = StageType.QUIZ,
                    title = "Quiz",
                    content = "Check your mastery with this short quiz.",
                    questions = quizQs
                )
            )
        )
    }

    private fun contentFor(key: String, band: String, lessonIndex: Int, seed: Int): String {
        val focus = when (key) {
            "counting", "numbers" -> "counting and number sense"
            "place_value" -> "place value and digit positions"
            "addition" -> "addition strategies"
            "subtraction" -> "subtraction strategies"
            "geometry" -> "geometric reasoning"
            "multiplication" -> "multiplication"
            "division" -> "division"
            "fractions" -> "fractions"
            "decimals" -> "decimals"
            "ratios" -> "ratios and rates"
            "integers" -> "integers"
            "prealgebra" -> "pre-algebra"
            "stats" -> "statistics"
            "algebra" -> "algebra"
            "quadratics" -> "quadratic functions"
            "functions" -> "functions"
            "advanced" -> "advanced mathematics"
            else -> "key math concepts"
        }
        val n = (seed % 9) + lessonIndex
        return "In this lesson ($lessonIndex) for grade band $band, you will explore $focus. " +
            "Pay attention to how numbers change when the starting value is $n. " +
            "Use patterns you notice to solve similar problems faster. " +
            "By the end, you should confidently apply these ideas to new examples."
    }

    private fun examplesFor(key: String, lessonIndex: Int, seed: Int): List<Example> {
        val ex1 = exampleFor(key, lessonIndex, seed)
        val ex2 = if (lessonIndex % 3 == 0) exampleFor(key, lessonIndex + 5, seed + 7) else null
        return listOfNotNull(ex1, ex2)
    }

    private fun exampleFor(key: String, lessonIndex: Int, seed: Int): Example {
        val a = 2 + (seed % 12) + lessonIndex
        val b = 1 + ((seed / 3) % 10) + (lessonIndex % 4)
        return when (key) {
            "counting", "numbers" -> {
                val start = a % 5
                val step = 2 + lessonIndex % 3
                val end = start + step * b
                Example(
                    problem = "Count by $step starting at $start for $b steps. What is the last number?",
                    solution = "$end",
                    steps = listOf(
                        "Start at $start.",
                        "Add $step each time, $b times.",
                        "The last number is $end."
                    )
                )
            }
            "place_value" -> {
                val tens = a % 9 + 1
                val ones = b % 10
                val value = tens * 10 + ones
                Example(
                    problem = "What number has $tens tens and $ones ones?",
                    solution = "$value",
                    steps = listOf(
                        "$tens tens = ${tens * 10}",
                        "Add $ones ones.",
                        "The number is $value."
                    )
                )
            }
            "addition" -> {
                val sum = a + b
                Example(
                    problem = "Calculate $a + $b.",
                    solution = "$sum",
                    steps = listOf("Line up the numbers.", "Add: $a + $b = $sum.", "Check by counting on.")
                )
            }
            "subtraction" -> {
                val big = a + b
                Example(
                    problem = "Calculate $big − $b.",
                    solution = "$a",
                    steps = listOf("Start with $big.", "Take away $b.", "Result: $a.")
                )
            }
            "multiplication" -> {
                val x = (a % 9) + 2
                val y = (b % 8) + 2
                val prod = x * y
                Example(
                    problem = "Find $x × $y.",
                    solution = "$prod",
                    steps = listOf("$x groups of $y.", "Multiply: $x × $y = $prod.")
                )
            }
            "division" -> {
                val y = (b % 7) + 2
                val x = (a % 8) + 2
                val prod = x * y
                Example(
                    problem = "Share $prod into $y equal groups. How many in each group?",
                    solution = "$x",
                    steps = listOf("Total: $prod.", "Divide by $y groups.", "Each group gets $x.")
                )
            }
            "fractions" -> {
                val num = (a % 5) + 1
                val den = num + (b % 4) + 2
                Example(
                    problem = "Simplify the idea: what fraction of $den is $num?",
                    solution = "$num/$den",
                    steps = listOf("Numerator = $num.", "Denominator = $den.", "Fraction: $num/$den.")
                )
            }
            "decimals" -> {
                val whole = a % 20
                val tenths = b % 9
                val value = whole + tenths / 10.0
                Example(
                    problem = "Write $whole.${tenths} as a decimal sum of place values.",
                    solution = String.format("%.1f", value),
                    steps = listOf("Ones: $whole.", "Tenths: 0.$tenths.", "Decimal: ${String.format("%.1f", value)}.")
                )
            }
            "ratios" -> {
                val r1 = (a % 6) + 2
                val r2 = (b % 6) + 2
                Example(
                    problem = "A ratio is $r1:$r2. If the first quantity is ${r1 * 3}, what is the second?",
                    solution = "${r2 * 3}",
                    steps = listOf("Scale factor = 3.", "Multiply $r2 by 3.", "Answer: ${r2 * 3}.")
                )
            }
            "integers" -> {
                val pos = a % 10 + 1
                val neg = -(b % 8 + 1)
                val sum = pos + neg
                Example(
                    problem = "Compute $pos + ($neg).",
                    solution = "$sum",
                    steps = listOf("Start at $pos on the number line.", "Move ${kotlin.math.abs(neg)} left.", "Land on $sum.")
                )
            }
            "prealgebra", "algebra" -> {
                val coef = (a % 5) + 2
                val constant = b % 10 + 1
                val xVal = (seed % 7) + 2
                val rhs = coef * xVal + constant
                Example(
                    problem = "Solve for x: ${coef}x + $constant = $rhs",
                    solution = "$xVal",
                    steps = listOf("Subtract $constant: ${coef}x = ${rhs - constant}.", "Divide by $coef.", "x = $xVal.")
                )
            }
            "geometry" -> {
                val side = (a % 8) + 3
                val area = side * side
                Example(
                    problem = "Find the area of a square with side length $side.",
                    solution = "$area",
                    steps = listOf("Area = side × side.", "$side × $side = $area.", "Area = $area square units.")
                )
            }
            "stats" -> {
                val v1 = a % 10 + 5
                val v2 = b % 10 + 5
                val mean = (v1 + v2) / 2.0
                Example(
                    problem = "Find the mean of $v1 and $v2.",
                    solution = if (mean == mean.toLong().toDouble()) "${mean.toInt()}" else String.format("%.1f", mean),
                    steps = listOf("Add: ${v1 + v2}.", "Divide by 2.", "Mean = ${if (mean == mean.toLong().toDouble()) mean.toInt() else String.format("%.1f", mean)}.")
                )
            }
            "quadratics" -> {
                val root = (a % 5) + 2
                Example(
                    problem = "If (x − $root)(x + $root) = 0, what are the solutions?",
                    solution = "$root, -$root",
                    steps = listOf("Set each factor to zero.", "x = $root or x = −$root.")
                )
            }
            "functions" -> {
                val input = (a % 6) + 1
                val output = input * 2 + (b % 3)
                Example(
                    problem = "If f(x) = 2x + ${b % 3}, find f($input).",
                    solution = "$output",
                    steps = listOf("Substitute x = $input.", "f($input) = 2($input) + ${b % 3}.", "f($input) = $output.")
                )
            }
            "advanced" -> {
                val base = (a % 4) + 2
                val exp = (b % 3) + 2
                var result = 1
                repeat(exp) { result *= base }
                Example(
                    problem = "Evaluate $base^$exp.",
                    solution = "$result",
                    steps = listOf("Multiply $base by itself $exp times.", "Result = $result.")
                )
            }
            else -> Example(
                problem = "Compute $a + $b.",
                solution = "${a + b}",
                steps = listOf("Add the values.", "${a + b}.")
            )
        }
    }

    private fun questionsFor(
        key: String,
        lessonId: String,
        lessonIndex: Int,
        seed: Int,
        count: Int,
        prefix: String
    ): List<Question> = (1..count).map { qi ->
        val s = seed + qi * 13 + lessonIndex
        questionFor(key, "${lessonId}_${prefix}$qi", s, qi, useMc = qi % 2 == 0)
    }

    private fun questionFor(key: String, id: String, seed: Int, qi: Int, useMc: Boolean): Question {
        val a = 3 + (seed % 15)
        val b = 2 + ((seed / 5) % 12)
        return when (key) {
            "counting", "numbers" -> {
                val step = 2 + seed % 4
                val count = b % 6 + 3
                val ans = step * count
                numericQ(id, "Count by $step, $count times starting from 0. What number do you reach?", "$ans", seed)
            }
            "place_value" -> {
                val tens = a % 9 + 1
                val ones = b % 10
                numericQ(id, "How many ones are in ${tens * 10 + ones}?", "$ones", seed)
            }
            "addition" -> numericQ(id, "What is $a + $b?", "${a + b}", seed)
            "subtraction" -> {
                val big = a + b + qi
                numericQ(id, "What is $big − $b?", "${big - b}", seed)
            }
            "multiplication" -> {
                val x = (a % 9) + 2
                val y = (b % 8) + 2
                numericQ(id, "What is $x × $y?", "${x * y}", seed)
            }
            "division" -> {
                val y = (b % 7) + 2
                val x = (a % 8) + 2
                numericQ(id, "What is ${x * y} ÷ $y?", "$x", seed)
            }
            "fractions" -> {
                val num = (a % 4) + 1
                val den = num + (b % 3) + 2
                if (useMc) mcQ(id, "Which fraction represents $num parts of $den?", listOf("$num/$den", "$b/$a", "1/$den", "$num/${den + 1}"), "$num/$den", seed)
                else numericQ(id, "Numerator of $num/$den?", "$num", seed)
            }
            "decimals" -> {
                val tenths = b % 9
                val valStr = "$a.$tenths"
                numericQ(id, "How many tenths in $valStr?", "$tenths", seed)
            }
            "ratios" -> {
                val r1 = (a % 5) + 2
                val r2 = (b % 5) + 2
                numericQ(id, "Ratio $r1:$r2 — if first is ${r1 * 4}, second is?", "${r2 * 4}", seed)
            }
            "integers" -> {
                val pos = a % 8 + 2
                val neg = -(b % 6 + 2)
                numericQ(id, "$pos + ($neg) = ?", "${pos + neg}", seed)
            }
            "prealgebra", "algebra" -> {
                val c = b % 10 + 1
                val rhs = 2 * a + c
                numericQ(id, "Solve: 2x + $c = $rhs. x = ?", "$a", seed)
            }
            "geometry" -> {
                val side = (a % 7) + 4
                numericQ(id, "Perimeter of square with side $side?", "${side * 4}", seed)
            }
            "stats" -> {
                val v1 = a % 10 + 10
                val v2 = b % 10 + 10
                val mean = (v1 + v2) / 2
                numericQ(id, "Mean of $v1 and $v2?", "$mean", seed)
            }
            "quadratics" -> {
                val r = (a % 5) + 1
                numericQ(id, "x² = ${r * r}. Positive x?", "$r", seed)
            }
            "functions" -> {
                val x = (a % 6) + 1
                val m = (b % 3) + 2
                numericQ(id, "f(x) = ${m}x. f($x) = ?", "${m * x}", seed)
            }
            "advanced" -> {
                val base = (a % 3) + 2
                val exp = 2
                numericQ(id, "$base² = ?", "${base * base}", seed)
            }
            else -> numericQ(id, "What is $a + $b?", "${a + b}", seed)
        }
    }

    private fun numericQ(id: String, prompt: String, answer: String, seed: Int): Question =
        Question(
            id = id,
            type = QuestionType.NUMERIC,
            prompt = prompt,
            correctAnswer = answer,
            explanation = "The correct answer is $answer.",
            hint = hintFor(seed),
            difficulty = 1 + seed % 3
        )

    private fun mcQ(id: String, prompt: String, options: List<String>, answer: String, seed: Int): Question {
        val shuffled = options.distinct().let { opts ->
            val idx = seed % opts.size
            opts.drop(idx) + opts.take(idx)
        }
        return Question(
            id = id,
            type = QuestionType.MULTIPLE_CHOICE,
            prompt = prompt,
            options = shuffled,
            correctAnswer = answer,
            explanation = "$answer is correct.",
            hint = hintFor(seed),
            difficulty = 1 + seed % 3
        )
    }

    private fun hintFor(seed: Int): String = when (seed % 4) {
        0 -> "Draw a picture or use objects."
        1 -> "Break the problem into smaller steps."
        2 -> "Check your work by working backward."
        else -> "Look for a pattern in earlier examples."
    }
}
