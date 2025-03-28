package it.unibo.alchemist.model.cognitive.impact.individual

/**
 * An enum representing the different periods of life.
 */
enum class Age : Characteristic {
    /**
     * Young person, before [CHILD_THRESHOLD].
     */
    CHILD,

    /**
     * Adult person, between [CHILD_THRESHOLD] and [ADULT_THRESHOLD].
     */
    ADULT,

    /**
     * Old person, past [ADULT_THRESHOLD].
     */
    ELDERLY,
    ;

    /**
     * Constants and factories.
     */
    companion object {
        /**
         * The threshold for being considered a child.
         */
        const val CHILD_THRESHOLD = 18

        /**
         * The threshold for being considered an elderly.
         */
        const val ADULT_THRESHOLD = 60

        private const val CHILD_KEYWORD = "child"
        private const val ADULT_KEYWORD = "adult"
        private const val ELDERLY_KEYWORD = "elderly"

        /**
         * Returns the corresponding age in this enum given the age in years.
         *
         * @param age
         *          the age in years.
         */
        fun fromYears(age: Int): Age = when {
            age < CHILD_THRESHOLD -> CHILD
            age < ADULT_THRESHOLD -> ADULT
            else -> ELDERLY
        }

        /**
         * Returns the corresponding age in this enum given a string resembling it.
         *
         * @param age
         *          the age as a string.
         */
        fun fromString(age: String): Age = when {
            age.equals(CHILD_KEYWORD, ignoreCase = true) -> CHILD
            age.equals(ADULT_KEYWORD, ignoreCase = true) -> ADULT
            age.equals(ELDERLY_KEYWORD, ignoreCase = true) -> ELDERLY
            else -> throw IllegalArgumentException("'$age' is not a valid age")
        }

        /**
         * Returns the corresponding age in this enum given a string resembling it.
         *
         * @param age
         *          the age as a string.
         */
        fun fromAny(age: Any): Age = when (age) {
            is String -> fromString(age)
            is Number -> fromYears(age.toInt())
            else -> throw IllegalArgumentException("$age:${age::class.simpleName} is not a valid age")
        }
    }
}
