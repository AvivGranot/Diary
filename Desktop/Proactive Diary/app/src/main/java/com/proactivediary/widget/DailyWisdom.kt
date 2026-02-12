package com.proactivediary.widget

import java.time.LocalDate

/**
 * Curated collection of quotes from uncommon and world-famous historical figures
 * across cultures, disciplines, and eras. Not the usual motivational poster fare —
 * these are words that make you stop and think.
 */
object DailyWisdom {

    data class WisdomQuote(
        val text: String,
        val author: String,
        val context: String = "" // Brief descriptor: "Mathematician", "Poet", etc.
    )

    private val quotes = listOf(
        // ── Scientists & Mathematicians ─────────────────────────────────
        WisdomQuote(
            "The most beautiful thing we can experience is the mysterious.",
            "Albert Einstein",
            "Physicist"
        ),
        WisdomQuote(
            "Nothing in life is to be feared, it is only to be understood.",
            "Marie Curie",
            "Physicist & Chemist"
        ),
        WisdomQuote(
            "The good thing about science is that it\u2019s true whether or not you believe in it.",
            "Neil deGrasse Tyson",
            "Astrophysicist"
        ),
        WisdomQuote(
            "I have no special talents. I am only passionately curious.",
            "Albert Einstein",
            "Physicist"
        ),
        WisdomQuote(
            "The enchanting charms of this sublime science reveal themselves only to those who have the courage to go deeply into it.",
            "Carl Friedrich Gauss",
            "Mathematician"
        ),
        WisdomQuote(
            "Imagination is more important than knowledge. Knowledge is limited. Imagination encircles the world.",
            "Albert Einstein",
            "Physicist"
        ),

        // ── Philosophers & Thinkers ────────────────────────────────────
        WisdomQuote(
            "The unexamined life is not worth living.",
            "Socrates",
            "Philosopher"
        ),
        WisdomQuote(
            "He who has a why to live can bear almost any how.",
            "Friedrich Nietzsche",
            "Philosopher"
        ),
        WisdomQuote(
            "We suffer more often in imagination than in reality.",
            "Seneca",
            "Stoic Philosopher"
        ),
        WisdomQuote(
            "No man ever steps in the same river twice, for it\u2019s not the same river and he\u2019s not the same man.",
            "Heraclitus",
            "Philosopher"
        ),
        WisdomQuote(
            "The only true wisdom is in knowing you know nothing.",
            "Socrates",
            "Philosopher"
        ),
        WisdomQuote(
            "Happiness is not something ready made. It comes from your own actions.",
            "Dalai Lama",
            "Spiritual Leader"
        ),
        WisdomQuote(
            "To know what you know and what you do not know, that is true knowledge.",
            "Confucius",
            "Philosopher"
        ),
        WisdomQuote(
            "The mind is everything. What you think you become.",
            "Buddha",
            "Spiritual Teacher"
        ),
        WisdomQuote(
            "An eye for an eye only ends up making the whole world blind.",
            "Mahatma Gandhi",
            "Activist & Leader"
        ),

        // ── Writers & Poets ────────────────────────────────────────────
        WisdomQuote(
            "One must still have chaos in oneself to be able to give birth to a dancing star.",
            "Friedrich Nietzsche",
            "Philosopher"
        ),
        WisdomQuote(
            "I am not what happened to me. I am what I choose to become.",
            "Carl Jung",
            "Psychiatrist"
        ),
        WisdomQuote(
            "There is no greater agony than bearing an untold story inside you.",
            "Maya Angelou",
            "Poet & Memoirist"
        ),
        WisdomQuote(
            "We write to taste life twice, in the moment and in retrospect.",
            "Ana\u00EFs Nin",
            "Diarist & Essayist"
        ),
        WisdomQuote(
            "The wound is the place where the Light enters you.",
            "Rumi",
            "13th Century Poet"
        ),
        WisdomQuote(
            "If you do not tell the truth about yourself you cannot tell it about other people.",
            "Virginia Woolf",
            "Novelist"
        ),
        WisdomQuote(
            "Not all those who wander are lost.",
            "J.R.R. Tolkien",
            "Author"
        ),
        WisdomQuote(
            "In the middle of difficulty lies opportunity.",
            "Albert Einstein",
            "Physicist"
        ),
        WisdomQuote(
            "I have spread my dreams under your feet; tread softly because you tread on my dreams.",
            "W.B. Yeats",
            "Poet"
        ),
        WisdomQuote(
            "The only way out is through.",
            "Robert Frost",
            "Poet"
        ),
        WisdomQuote(
            "How wild it was, to let it be.",
            "Cheryl Strayed",
            "Author"
        ),
        WisdomQuote(
            "You do not write your life with words. You write it with actions.",
            "Patrick Ness",
            "Author"
        ),

        // ── Artists & Creators ─────────────────────────────────────────
        WisdomQuote(
            "I dream my painting, and I paint my dream.",
            "Vincent van Gogh",
            "Painter"
        ),
        WisdomQuote(
            "At the end of the day, we can endure much more than we think we can.",
            "Frida Kahlo",
            "Artist"
        ),
        WisdomQuote(
            "Every child is an artist. The problem is how to remain an artist once we grow up.",
            "Pablo Picasso",
            "Artist"
        ),
        WisdomQuote(
            "Creativity takes courage.",
            "Henri Matisse",
            "Artist"
        ),
        WisdomQuote(
            "The chief enemy of creativity is good sense.",
            "Pablo Picasso",
            "Artist"
        ),
        WisdomQuote(
            "Art is not what you see, but what you make others see.",
            "Edgar Degas",
            "Painter"
        ),

        // ── Leaders & Activists ────────────────────────────────────────
        WisdomQuote(
            "In a gentle way, you can shake the world.",
            "Mahatma Gandhi",
            "Activist & Leader"
        ),
        WisdomQuote(
            "The time is always right to do what is right.",
            "Martin Luther King Jr.",
            "Civil Rights Leader"
        ),
        WisdomQuote(
            "I learned a long time ago the wisest thing I can do is be on my own side.",
            "Maya Angelou",
            "Poet & Memoirist"
        ),
        WisdomQuote(
            "One child, one teacher, one book, one pen can change the world.",
            "Malala Yousafzai",
            "Activist"
        ),
        WisdomQuote(
            "The most common way people give up their power is by thinking they don\u2019t have any.",
            "Alice Walker",
            "Author & Activist"
        ),
        WisdomQuote(
            "You may not control all the events that happen to you, but you can decide not to be reduced by them.",
            "Maya Angelou",
            "Poet & Memoirist"
        ),
        WisdomQuote(
            "It is during our darkest moments that we must focus to see the light.",
            "Aristotle",
            "Philosopher"
        ),
        WisdomQuote(
            "What lies behind us and what lies before us are tiny matters compared to what lies within us.",
            "Ralph Waldo Emerson",
            "Essayist"
        ),

        // ── Uncommon Voices ────────────────────────────────────────────
        WisdomQuote(
            "Those who do not move, do not notice their chains.",
            "Rosa Luxemburg",
            "Political Theorist"
        ),
        WisdomQuote(
            "The world is a book and those who do not travel read only one page.",
            "Ibn Battuta",
            "14th Century Explorer"
        ),
        WisdomQuote(
            "I am no bird; and no net ensnares me: I am a free human being with an independent will.",
            "Charlotte Bront\u00EB",
            "Novelist"
        ),
        WisdomQuote(
            "All we have to decide is what to do with the time that is given us.",
            "J.R.R. Tolkien",
            "Author"
        ),
        WisdomQuote(
            "The soul becomes dyed with the color of its thoughts.",
            "Marcus Aurelius",
            "Roman Emperor & Philosopher"
        ),
        WisdomQuote(
            "If you are always trying to be normal, you will never know how amazing you can be.",
            "Maya Angelou",
            "Poet & Memoirist"
        ),
        WisdomQuote(
            "A society grows great when old men plant trees in whose shade they shall never sit.",
            "Greek Proverb",
            "Ancient Wisdom"
        ),
        WisdomQuote(
            "The art of being wise is the art of knowing what to overlook.",
            "William James",
            "Psychologist & Philosopher"
        ),
        WisdomQuote(
            "To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment.",
            "Ralph Waldo Emerson",
            "Essayist"
        ),
        WisdomQuote(
            "Knowing yourself is the beginning of all wisdom.",
            "Aristotle",
            "Philosopher"
        ),

        // ── Innovators & Visionaries ───────────────────────────────────
        WisdomQuote(
            "The people who are crazy enough to think they can change the world are the ones who do.",
            "Steve Jobs",
            "Innovator"
        ),
        WisdomQuote(
            "I have not failed. I\u2019ve just found 10,000 ways that won\u2019t work.",
            "Thomas Edison",
            "Inventor"
        ),
        WisdomQuote(
            "If I have seen further, it is by standing on the shoulders of giants.",
            "Isaac Newton",
            "Physicist & Mathematician"
        ),
        WisdomQuote(
            "That brain of mine is something more than merely mortal; as time will show.",
            "Ada Lovelace",
            "Mathematician & First Programmer"
        ),
        WisdomQuote(
            "The future belongs to those who believe in the beauty of their dreams.",
            "Eleanor Roosevelt",
            "Diplomat & Activist"
        ),
        WisdomQuote(
            "Life is either a daring adventure or nothing at all.",
            "Helen Keller",
            "Author & Activist"
        ),
        WisdomQuote(
            "I am among those who think that science has great beauty.",
            "Marie Curie",
            "Physicist & Chemist"
        ),

        // ── Eastern Wisdom ─────────────────────────────────────────────
        WisdomQuote(
            "When I let go of what I am, I become what I might be.",
            "Lao Tzu",
            "Philosopher"
        ),
        WisdomQuote(
            "The journey of a thousand miles begins with one step.",
            "Lao Tzu",
            "Philosopher"
        ),
        WisdomQuote(
            "Do not dwell in the past, do not dream of the future, concentrate the mind on the present moment.",
            "Buddha",
            "Spiritual Teacher"
        ),
        WisdomQuote(
            "Be the change that you wish to see in the world.",
            "Mahatma Gandhi",
            "Activist & Leader"
        ),
        WisdomQuote(
            "The bamboo that bends is stronger than the oak that resists.",
            "Japanese Proverb",
            "Eastern Wisdom"
        ),
        WisdomQuote(
            "Fall seven times, stand up eight.",
            "Japanese Proverb",
            "Eastern Wisdom"
        ),

        // ── Musicians & Performers ─────────────────────────────────────
        WisdomQuote(
            "Where words fail, music speaks.",
            "Hans Christian Andersen",
            "Author"
        ),
        WisdomQuote(
            "Life is what happens when you\u2019re busy making other plans.",
            "John Lennon",
            "Musician"
        ),
        WisdomQuote(
            "Everything has beauty, but not everyone sees it.",
            "Confucius",
            "Philosopher"
        ),

        // ── Modern Thinkers ───────────────────────────────────────────
        WisdomQuote(
            "Vulnerability is not winning or losing; it\u2019s having the courage to show up and be seen.",
            "Bren\u00E9 Brown",
            "Researcher"
        ),
        WisdomQuote(
            "Between stimulus and response there is a space. In that space is our power to choose our response.",
            "Viktor Frankl",
            "Psychiatrist & Holocaust Survivor"
        ),
        WisdomQuote(
            "Almost everything will work again if you unplug it for a few minutes, including you.",
            "Anne Lamott",
            "Author"
        ),
        WisdomQuote(
            "You can\u2019t go back and change the beginning, but you can start where you are and change the ending.",
            "C.S. Lewis",
            "Author"
        ),
        WisdomQuote(
            "The privilege of a lifetime is to become who you truly are.",
            "Carl Jung",
            "Psychiatrist"
        ),

        // ── Diverse Voices ─────────────────────────────────────────────
        WisdomQuote(
            "I have learned over the years that when one\u2019s mind is made up, this diminishes fear.",
            "Rosa Parks",
            "Civil Rights Activist"
        ),
        WisdomQuote(
            "Turn your wounds into wisdom.",
            "Oprah Winfrey",
            "Media Leader"
        ),
        WisdomQuote(
            "The only impossible journey is the one you never begin.",
            "Tony Robbins",
            "Author"
        ),
        WisdomQuote(
            "What you get by achieving your goals is not as important as what you become by achieving your goals.",
            "Zig Ziglar",
            "Author"
        ),
        WisdomQuote(
            "You have power over your mind \u2014 not outside events. Realize this, and you will find strength.",
            "Marcus Aurelius",
            "Roman Emperor & Philosopher"
        ),
        WisdomQuote(
            "The only person you are destined to become is the person you decide to be.",
            "Ralph Waldo Emerson",
            "Essayist"
        ),
        WisdomQuote(
            "One day or day one. You decide.",
            "Paulo Coelho",
            "Author"
        ),
        WisdomQuote(
            "What we achieve inwardly will change outer reality.",
            "Plutarch",
            "Greek Historian"
        ),

        // ── Scholars & Historians ──────────────────────────────────────
        WisdomQuote(
            "Those who cannot remember the past are condemned to repeat it.",
            "George Santayana",
            "Philosopher"
        ),
        WisdomQuote(
            "History is not the past. It is the present. We carry our history with us. We are our history.",
            "James Baldwin",
            "Author & Activist"
        ),
        WisdomQuote(
            "The measure of intelligence is the ability to change.",
            "Albert Einstein",
            "Physicist"
        ),
        WisdomQuote(
            "Education is the most powerful weapon which you can use to change the world.",
            "Nelson Mandela",
            "Leader & Activist"
        ),
        WisdomQuote(
            "True knowledge exists in knowing that you know nothing.",
            "Socrates",
            "Philosopher"
        ),

        // ── Nature & Observation ───────────────────────────────────────
        WisdomQuote(
            "In every walk with nature one receives far more than he seeks.",
            "John Muir",
            "Naturalist"
        ),
        WisdomQuote(
            "Look deep into nature, and then you will understand everything better.",
            "Albert Einstein",
            "Physicist"
        ),
        WisdomQuote(
            "The clearest way into the Universe is through a forest wilderness.",
            "John Muir",
            "Naturalist"
        ),
        WisdomQuote(
            "Adopt the pace of nature: her secret is patience.",
            "Ralph Waldo Emerson",
            "Essayist"
        )
    )

    /**
     * Returns today's wisdom quote. Deterministic per day — same quote all day,
     * different quote tomorrow.
     */
    fun todayQuote(): WisdomQuote {
        val dayOfYear = LocalDate.now().dayOfYear
        val yearOffset = LocalDate.now().year % 7 // Shift cycle across years
        return quotes[(dayOfYear + yearOffset) % quotes.size]
    }

    /**
     * Returns N unique quotes for a given date range (for digest cards, etc.)
     */
    fun quotesForRange(startDay: Int, count: Int): List<WisdomQuote> {
        return (0 until count).map { i ->
            quotes[(startDay + i) % quotes.size]
        }
    }

    val totalQuotes: Int get() = quotes.size
}
