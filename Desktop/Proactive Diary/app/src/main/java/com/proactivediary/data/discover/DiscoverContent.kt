package com.proactivediary.data.discover

import com.proactivediary.domain.model.DiscoverEntry
import java.time.LocalDate
import kotlin.random.Random

/**
 * Curated library of journal entries, letters, and reflections
 * from great philosophers, scientists, artists, and uncommon thinkers.
 *
 * No API needed — this ships with the app.
 */
object DiscoverContent {

    val categories = listOf("Philosophy", "Science", "Art", "Leadership", "Spirituality", "Literature")

    val allEntries: List<DiscoverEntry> = listOf(
        // ── Philosophy ──────────────────────────────────────────

        DiscoverEntry(
            id = "marcus_1",
            author = "Marcus Aurelius",
            authorYears = "121\u2013180 AD",
            title = "On beginning each day",
            excerpt = "When you arise in the morning, think of what a precious privilege it is to be alive\u2014to breathe, to think, to enjoy, to love.",
            source = "Meditations, Book V",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "marcus_2",
            author = "Marcus Aurelius",
            authorYears = "121\u2013180 AD",
            title = "On what we control",
            excerpt = "You have power over your mind\u2014not outside events. Realize this, and you will find strength.",
            source = "Meditations, Book VI",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "marcus_3",
            author = "Marcus Aurelius",
            authorYears = "121\u2013180 AD",
            title = "On impermanence",
            excerpt = "Think of the life you have lived until now as over and done. See what remains as a bonus, and live it according to nature.",
            source = "Meditations, Book VII",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "seneca_1",
            author = "Seneca",
            authorYears = "4 BC\u201365 AD",
            title = "On the shortness of life",
            excerpt = "It is not that we have a short time to live, but that we waste a great deal of it. Life is long enough, and a sufficiently generous amount has been given to us for the highest achievements if it were all well invested.",
            source = "On the Shortness of Life",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "seneca_2",
            author = "Seneca",
            authorYears = "4 BC\u201365 AD",
            title = "On difficulty",
            excerpt = "Difficulties strengthen the mind, as labor does the body.",
            source = "Letters to Lucilius, Letter 78",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "epictetus_1",
            author = "Epictetus",
            authorYears = "50\u2013135 AD",
            title = "On freedom",
            excerpt = "No man is free who is not master of himself. If you wish to be good, first believe that you are bad.",
            source = "Discourses, Book IV",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "epictetus_2",
            author = "Epictetus",
            authorYears = "50\u2013135 AD",
            title = "On what disturbs us",
            excerpt = "It is not things that disturb us, but our judgments about things. Remove the judgment, and you remove the disturbance.",
            source = "Enchiridion, Chapter 5",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "plato_1",
            author = "Plato",
            authorYears = "428\u2013348 BC",
            title = "On self-knowledge",
            excerpt = "The first and best victory is to conquer self. To be conquered by self is, of all things, the most shameful and vile.",
            source = "Laws, Book I",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "laotzu_1",
            author = "Lao Tzu",
            authorYears = "6th century BC",
            title = "On knowing oneself",
            excerpt = "Knowing others is intelligence; knowing yourself is true wisdom. Mastering others is strength; mastering yourself is true power.",
            source = "Tao Te Ching, Chapter 33",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "laotzu_2",
            author = "Lao Tzu",
            authorYears = "6th century BC",
            title = "On the journey",
            excerpt = "A journey of a thousand miles begins with a single step. The sage does not attempt anything very big, and thus achieves greatness.",
            source = "Tao Te Ching, Chapter 64",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "beauvoir_1",
            author = "Simone de Beauvoir",
            authorYears = "1908\u20131986",
            title = "On meaning",
            excerpt = "One is not born, but rather becomes. Life is occupied in both perpetuating itself and in surpassing itself; if all it does is maintain itself, then living is only not dying.",
            source = "The Ethics of Ambiguity",
            category = "Philosophy",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "kierkegaard_1",
            author = "S\u00f8ren Kierkegaard",
            authorYears = "1813\u20131855",
            title = "On living forward",
            excerpt = "Life can only be understood backwards; but it must be lived forwards.",
            source = "Journals, 1843",
            category = "Philosophy",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "nietzsche_1",
            author = "Friedrich Nietzsche",
            authorYears = "1844\u20131900",
            title = "On becoming",
            excerpt = "The individual has always had to struggle to keep from being overwhelmed by the tribe. But no price is too high to pay for the privilege of owning yourself.",
            source = "Thus Spoke Zarathustra",
            category = "Philosophy",
            era = "Modern"
        ),

        // ── Science ─────────────────────────────────────────────

        DiscoverEntry(
            id = "edison_1",
            author = "Thomas Edison",
            authorYears = "1847\u20131931",
            title = "Lab notebook, December 1879",
            excerpt = "I have not failed. I have just found ten thousand ways that do not work. Our greatest weakness lies in giving up. The most certain way to succeed is always to try just one more time.",
            source = "Personal laboratory notebooks",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "edison_2",
            author = "Thomas Edison",
            authorYears = "1847\u20131931",
            title = "On genius and work",
            excerpt = "Genius is one percent inspiration and ninety-nine percent perspiration. Accordingly, a genius is often merely a talented person who has done all of their homework.",
            source = "Interview, Harper\u2019s Monthly, 1932",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "curie_1",
            author = "Marie Curie",
            authorYears = "1867\u20131934",
            title = "On persistence in science",
            excerpt = "I was taught that the way of progress is neither swift nor easy. Nothing in life is to be feared, it is only to be understood. Now is the time to understand more, so that we may fear less.",
            source = "Personal writings",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "curie_2",
            author = "Marie Curie",
            authorYears = "1867\u20131934",
            title = "On curiosity",
            excerpt = "Be less curious about people and more curious about ideas. A scientist in the laboratory is not a mere technician: they are a child confronting natural phenomena that impress like fairy tales.",
            source = "Lecture at Vassar College, 1921",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "einstein_1",
            author = "Albert Einstein",
            authorYears = "1879\u20131955",
            title = "On imagination",
            excerpt = "Imagination is more important than knowledge. Knowledge is limited. Imagination encircles the world. The important thing is not to stop questioning. Curiosity has its own reason for existing.",
            source = "Interview, The Saturday Evening Post, 1929",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "einstein_2",
            author = "Albert Einstein",
            authorYears = "1879\u20131955",
            title = "Letter on mystery",
            excerpt = "The most beautiful experience we can have is the mysterious. It is the fundamental emotion that stands at the cradle of true art and true science.",
            source = "The World As I See It, 1931",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "tesla_1",
            author = "Nikola Tesla",
            authorYears = "1856\u20131943",
            title = "On the mind\u2019s power",
            excerpt = "The mind is sharper and keener in seclusion and uninterrupted solitude. Originality thrives in seclusion free of outside influences beating upon us to cripple the creative mind.",
            source = "My Inventions, 1919",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "tesla_2",
            author = "Nikola Tesla",
            authorYears = "1856\u20131943",
            title = "On the future",
            excerpt = "Let the future tell the truth, and evaluate each one according to his work and accomplishments. The present is theirs; the future, for which I have really worked, is mine.",
            source = "Personal correspondence",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "darwin_1",
            author = "Charles Darwin",
            authorYears = "1809\u20131882",
            title = "Voyage journal, Galapagos",
            excerpt = "There is grandeur in this view of life, with its several powers, having been originally breathed into a few forms or into one; and that from so simple a beginning, endless forms most beautiful have been evolved.",
            source = "On the Origin of Species, closing passage",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "feynman_1",
            author = "Richard Feynman",
            authorYears = "1918\u20131988",
            title = "On not knowing",
            excerpt = "I would rather have questions that can\u2019t be answered than answers that can\u2019t be questioned. The first principle is that you must not fool yourself, and you are the easiest person to fool.",
            source = "Surely You\u2019re Joking, Mr. Feynman!",
            category = "Science",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "sagan_1",
            author = "Carl Sagan",
            authorYears = "1934\u20131996",
            title = "On our place in the cosmos",
            excerpt = "Look again at that dot. That\u2019s here. That\u2019s home. That\u2019s us. Every saint and sinner in the history of our species lived there, on a mote of dust suspended in a sunbeam.",
            source = "Pale Blue Dot, 1994",
            category = "Science",
            era = "Contemporary"
        ),

        // ── Art ──────────────────────────────────────────────────

        DiscoverEntry(
            id = "davinci_1",
            author = "Leonardo da Vinci",
            authorYears = "1452\u20131519",
            title = "Notebook observation on water",
            excerpt = "In rivers, the water that you touch is the last of what has passed and the first of that which comes; so with present time. Learn to see. Realize that everything connects to everything else.",
            source = "Codex Leicester",
            category = "Art",
            era = "Renaissance"
        ),
        DiscoverEntry(
            id = "davinci_2",
            author = "Leonardo da Vinci",
            authorYears = "1452\u20131519",
            title = "On simplicity",
            excerpt = "Simplicity is the ultimate sophistication. The noblest pleasure is the joy of understanding.",
            source = "Personal notebooks",
            category = "Art",
            era = "Renaissance"
        ),
        DiscoverEntry(
            id = "kahlo_1",
            author = "Frida Kahlo",
            authorYears = "1907\u20131954",
            title = "On pain and creation",
            excerpt = "I used to think I was the strangest person in the world. But then I thought, there are so many people in the world, there must be someone just like me. I am out there waiting for me.",
            source = "Personal diary",
            category = "Art",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "kahlo_2",
            author = "Frida Kahlo",
            authorYears = "1907\u20131954",
            title = "On resilience",
            excerpt = "At the end of the day, we can endure much more than we think we can. Feet, what do I need them for if I have wings to fly?",
            source = "Personal diary",
            category = "Art",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "vangogh_1",
            author = "Vincent van Gogh",
            authorYears = "1853\u20131890",
            title = "Letter to Theo on purpose",
            excerpt = "If you hear a voice within you say \u2018you cannot paint,\u2019 then by all means paint, and that voice will be silenced. Great things are not done by impulse, but by a series of small things brought together.",
            source = "Letters to Theo, October 1884",
            category = "Art",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "vangogh_2",
            author = "Vincent van Gogh",
            authorYears = "1853\u20131890",
            title = "Letter on finding meaning",
            excerpt = "I would rather die of passion than of boredom. What is done in love is done well. For my part I know nothing with any certainty, but the sight of the stars makes me dream.",
            source = "Letters to Theo, 1888",
            category = "Art",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "michelangelo_1",
            author = "Michelangelo",
            authorYears = "1475\u20131564",
            title = "On the work within",
            excerpt = "Every block of stone has a statue inside it and it is the task of the sculptor to discover it. The greater danger for most of us lies not in setting our aim too high, but in setting it too low.",
            source = "Attributed writings",
            category = "Art",
            era = "Renaissance"
        ),
        DiscoverEntry(
            id = "okeefe_1",
            author = "Georgia O\u2019Keeffe",
            authorYears = "1887\u20131986",
            title = "On seeing clearly",
            excerpt = "I found I could say things with color and shapes that I couldn\u2019t say any other way\u2014things I had no words for. Nobody sees a flower really; it is so small. We haven\u2019t time, and to see takes time.",
            source = "Exhibition catalogue, 1939",
            category = "Art",
            era = "Modern"
        ),

        // ── Leadership ──────────────────────────────────────────

        DiscoverEntry(
            id = "frank_1",
            author = "Anne Frank",
            authorYears = "1929\u20131945",
            title = "On hope in darkness",
            excerpt = "How wonderful it is that nobody need wait a single moment before starting to improve the world. In spite of everything, I still believe that people are really good at heart.",
            source = "The Diary of a Young Girl, July 1944",
            category = "Leadership",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "frank_2",
            author = "Anne Frank",
            authorYears = "1929\u20131945",
            title = "On inner light",
            excerpt = "Look at how a single candle can both defy and define the darkness. Think of all the beauty still left around you and be happy.",
            source = "The Diary of a Young Girl, March 1944",
            category = "Leadership",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "douglass_1",
            author = "Frederick Douglass",
            authorYears = "1818\u20131895",
            title = "On literacy and freedom",
            excerpt = "Once you learn to read, you will be forever free. If there is no struggle, there is no progress. Power concedes nothing without a demand. It never did and it never will.",
            source = "Narrative of the Life of Frederick Douglass",
            category = "Leadership",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "mandela_1",
            author = "Nelson Mandela",
            authorYears = "1918\u20132013",
            title = "On courage",
            excerpt = "I learned that courage was not the absence of fear, but the triumph over it. The brave man is not he who does not feel afraid, but he who conquers that fear.",
            source = "Long Walk to Freedom",
            category = "Leadership",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "mandela_2",
            author = "Nelson Mandela",
            authorYears = "1918\u20132013",
            title = "On education",
            excerpt = "Education is the most powerful weapon which you can use to change the world. It is in your hands to make of our world a better one for all.",
            source = "Address to University of the Witwatersrand, 2003",
            category = "Leadership",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "gandhi_1",
            author = "Mahatma Gandhi",
            authorYears = "1869\u20131948",
            title = "On becoming the change",
            excerpt = "Be the change you wish to see in the world. In a gentle way, you can shake the world. The best way to find yourself is to lose yourself in the service of others.",
            source = "Collected writings",
            category = "Leadership",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "roosevelt_1",
            author = "Eleanor Roosevelt",
            authorYears = "1884\u20131962",
            title = "On courage and growth",
            excerpt = "You gain strength, courage, and confidence by every experience in which you really stop to look fear in the face. You must do the thing you think you cannot do.",
            source = "You Learn by Living, 1960",
            category = "Leadership",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "mlk_1",
            author = "Martin Luther King Jr.",
            authorYears = "1929\u20131968",
            title = "On taking the first step",
            excerpt = "Faith is taking the first step even when you don\u2019t see the whole staircase. The time is always right to do what is right.",
            source = "Sermons and speeches",
            category = "Leadership",
            era = "Contemporary"
        ),

        // ── Spirituality ────────────────────────────────────────

        DiscoverEntry(
            id = "rumi_1",
            author = "Rumi",
            authorYears = "1207\u20131273",
            title = "On the wound becoming light",
            excerpt = "The wound is the place where the Light enters you. Don\u2019t grieve. Anything you lose comes round in another form.",
            source = "Masnavi",
            category = "Spirituality",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "rumi_2",
            author = "Rumi",
            authorYears = "1207\u20131273",
            title = "On seeking",
            excerpt = "What you seek is seeking you. Let yourself be silently drawn by the strange pull of what you really love. It will not lead you astray.",
            source = "Collected poems",
            category = "Spirituality",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "rumi_3",
            author = "Rumi",
            authorYears = "1207\u20131273",
            title = "On the guest house",
            excerpt = "This being human is a guest house. Every morning a new arrival. A joy, a depression, a meanness\u2014some momentary awareness comes as an unexpected visitor. Welcome and entertain them all.",
            source = "The Guest House",
            category = "Spirituality",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "thich_1",
            author = "Thich Nhat Hanh",
            authorYears = "1926\u20132022",
            title = "On the present moment",
            excerpt = "The present moment is the only moment available to us, and it is the door to all moments. Life is available only in the present moment.",
            source = "The Miracle of Mindfulness",
            category = "Spirituality",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "thich_2",
            author = "Thich Nhat Hanh",
            authorYears = "1926\u20132022",
            title = "On walking",
            excerpt = "Walk as if you are kissing the Earth with your feet. Every step you take can be a prayer, a meditation, a poem.",
            source = "Peace Is Every Step",
            category = "Spirituality",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "buddha_1",
            author = "Siddhartha Gautama",
            authorYears = "563\u2013483 BC",
            title = "On the mind",
            excerpt = "The mind is everything. What you think, you become. Peace comes from within. Do not seek it without.",
            source = "Dhammapada",
            category = "Spirituality",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "frankl_1",
            author = "Viktor Frankl",
            authorYears = "1905\u20131997",
            title = "On finding meaning",
            excerpt = "When we are no longer able to change a situation, we are challenged to change ourselves. Between stimulus and response there is a space. In that space is our freedom and power to choose our response.",
            source = "Man\u2019s Search for Meaning",
            category = "Spirituality",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "frankl_2",
            author = "Viktor Frankl",
            authorYears = "1905\u20131997",
            title = "On purpose",
            excerpt = "Those who have a \u2018why\u2019 to live can bear with almost any \u2018how.\u2019 Life is never made unbearable by circumstances, but only by lack of meaning and purpose.",
            source = "Man\u2019s Search for Meaning",
            category = "Spirituality",
            era = "Contemporary"
        ),

        // ── Literature ──────────────────────────────────────────

        DiscoverEntry(
            id = "woolf_1",
            author = "Virginia Woolf",
            authorYears = "1882\u20131941",
            title = "On writing daily",
            excerpt = "The habit of writing for my eye only is good practice. It loosens the ligaments. The only way to do it is to do it.",
            source = "A Writer\u2019s Diary, 1920",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "woolf_2",
            author = "Virginia Woolf",
            authorYears = "1882\u20131941",
            title = "On seeing the world",
            excerpt = "You cannot find peace by avoiding life. One cannot think well, love well, sleep well, if one has not dined well.",
            source = "A Room of One\u2019s Own",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "plath_1",
            author = "Sylvia Plath",
            authorYears = "1932\u20131963",
            title = "On living fully",
            excerpt = "I can never read all the books I want; I can never be all the people I want and live all the lives I want. I want to live and feel all the shades and tones of living.",
            source = "The Unabridged Journals",
            category = "Literature",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "emerson_1",
            author = "Ralph Waldo Emerson",
            authorYears = "1803\u20131882",
            title = "On self-reliance",
            excerpt = "To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment. Trust thyself: every heart vibrates to that iron string.",
            source = "Self-Reliance, 1841",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "emerson_2",
            author = "Ralph Waldo Emerson",
            authorYears = "1803\u20131882",
            title = "On each new day",
            excerpt = "Write it on your heart that every day is the best day in the year. Finish each day and be done with it. You have done what you could.",
            source = "Journals, 1849",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "thoreau_1",
            author = "Henry David Thoreau",
            authorYears = "1817\u20131862",
            title = "On deliberate living",
            excerpt = "I went to the woods because I wished to live deliberately, to front only the essential facts of life, and see if I could not learn what it had to teach, and not, when I came to die, discover that I had not lived.",
            source = "Walden, 1854",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "thoreau_2",
            author = "Henry David Thoreau",
            authorYears = "1817\u20131862",
            title = "On simplicity",
            excerpt = "Our life is frittered away by detail. Simplify, simplify. As you simplify your life, the laws of the universe will be simpler.",
            source = "Walden, 1854",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "camus_1",
            author = "Albert Camus",
            authorYears = "1913\u20131960",
            title = "On invincible summer",
            excerpt = "In the midst of winter, I found there was, within me, an invincible summer. And that makes me happy. For it says that no matter how hard the world pushes against me, there\u2019s something stronger pushing right back.",
            source = "Return to Tipasa, 1954",
            category = "Literature",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "tolstoy_1",
            author = "Leo Tolstoy",
            authorYears = "1828\u20131910",
            title = "On happiness",
            excerpt = "If you want to be happy, be. The two most powerful warriors are patience and time. Everyone thinks of changing the world, but no one thinks of changing himself.",
            source = "Collected essays",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "angelou_1",
            author = "Maya Angelou",
            authorYears = "1928\u20132014",
            title = "On courage",
            excerpt = "There is no greater agony than bearing an untold story inside you. We delight in the beauty of the butterfly, but rarely admit the changes it has gone through to achieve that beauty.",
            source = "I Know Why the Caged Bird Sings",
            category = "Literature",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "kafka_1",
            author = "Franz Kafka",
            authorYears = "1883\u20131924",
            title = "On books that wound",
            excerpt = "A book must be the axe for the frozen sea within us. I think we ought to read only the kind of books that wound and stab us.",
            source = "Letter to Oskar Pollak, 1904",
            category = "Literature",
            era = "Modern"
        ),

        // ── Additional Philosophy ───────────────────────────────

        DiscoverEntry(
            id = "marcus_4",
            author = "Marcus Aurelius",
            authorYears = "121\u2013180 AD",
            title = "On obstacles",
            excerpt = "The impediment to action advances action. What stands in the way becomes the way.",
            source = "Meditations, Book V",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "seneca_3",
            author = "Seneca",
            authorYears = "4 BC\u201365 AD",
            title = "On anger",
            excerpt = "The greatest remedy for anger is delay. We suffer more often in imagination than in reality.",
            source = "Letters to Lucilius, Letter 13",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "confucius_1",
            author = "Confucius",
            authorYears = "551\u2013479 BC",
            title = "On perseverance",
            excerpt = "It does not matter how slowly you go, as long as you do not stop. The man who moves a mountain begins by carrying away small stones.",
            source = "Analects",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "confucius_2",
            author = "Confucius",
            authorYears = "551\u2013479 BC",
            title = "On learning",
            excerpt = "Real knowledge is to know the extent of one\u2019s ignorance. By three methods we may learn wisdom: first, by reflection, which is noblest; second, by imitation; and third, by experience, which is the bitterest.",
            source = "Analects",
            category = "Philosophy",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "schopenhauer_1",
            author = "Arthur Schopenhauer",
            authorYears = "1788\u20131860",
            title = "On solitude",
            excerpt = "A man can be himself only so long as he is alone; and if he does not love solitude, he will not love freedom; for it is only when he is alone that he is really free.",
            source = "Counsels and Maxims",
            category = "Philosophy",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "weil_1",
            author = "Simone Weil",
            authorYears = "1909\u20131943",
            title = "On attention",
            excerpt = "Attention is the rarest and purest form of generosity. The soul empties itself of all its own contents in order to receive the being it is looking at, just as that being is, in all its truth.",
            source = "Gravity and Grace",
            category = "Philosophy",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "montaigne_1",
            author = "Michel de Montaigne",
            authorYears = "1533\u20131592",
            title = "On self-knowledge",
            excerpt = "The greatest thing in the world is to know how to belong to oneself. We are, I know not how, double in ourselves, so that what we believe we disbelieve, and cannot rid ourselves of what we condemn.",
            source = "Essays, Book I",
            category = "Philosophy",
            era = "Renaissance"
        ),

        // ── Additional Science ──────────────────────────────────

        DiscoverEntry(
            id = "hawking_1",
            author = "Stephen Hawking",
            authorYears = "1942\u20132018",
            title = "On looking up",
            excerpt = "Remember to look up at the stars and not down at your feet. Try to make sense of what you see and wonder about what makes the universe exist. Be curious.",
            source = "Interview, ABC News, 2010",
            category = "Science",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "curie_3",
            author = "Marie Curie",
            authorYears = "1867\u20131934",
            title = "On dreaming",
            excerpt = "I am among those who think that science has great beauty. A scientist in the laboratory is not only a technician but also a child placed before natural phenomena which impress like a fairy tale.",
            source = "Pierre Curie, biographical note, 1923",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "lovelace_1",
            author = "Ada Lovelace",
            authorYears = "1815\u20131852",
            title = "On imagination and science",
            excerpt = "That brain of mine is something more than merely mortal; as time will show. Imagination is the discovering faculty, pre-eminently. It is that which penetrates into the unseen worlds around us, the worlds of science.",
            source = "Personal letters, 1841",
            category = "Science",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "goodall_1",
            author = "Jane Goodall",
            authorYears = "1934\u2013",
            title = "On hope and action",
            excerpt = "What you do makes a difference, and you have to decide what kind of difference you want to make. Every individual matters. Every individual has a role to play.",
            source = "Reason for Hope",
            category = "Science",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "galileo_1",
            author = "Galileo Galilei",
            authorYears = "1564\u20131642",
            title = "On doubt and inquiry",
            excerpt = "All truths are easy to understand once they are discovered; the point is to discover them. I do not feel obliged to believe that the same God who has endowed us with sense, reason, and intellect has intended us to forgo their use.",
            source = "Letter to the Grand Duchess Christina, 1615",
            category = "Science",
            era = "Renaissance"
        ),
        DiscoverEntry(
            id = "darwin_2",
            author = "Charles Darwin",
            authorYears = "1809\u20131882",
            title = "On small daily acts",
            excerpt = "A man who dares to waste one hour of time has not discovered the value of life. It is not the strongest of the species that survives, nor the most intelligent, but the one most responsive to change.",
            source = "Personal letters and notebooks",
            category = "Science",
            era = "Modern"
        ),

        // ── Additional Art ──────────────────────────────────────

        DiscoverEntry(
            id = "basquiat_1",
            author = "Jean-Michel Basquiat",
            authorYears = "1960\u20131988",
            title = "On originality",
            excerpt = "I don\u2019t think about art when I\u2019m working. I try to think about life. I don\u2019t listen to what art critics say. I don\u2019t know anybody who needs a critic to find out what art is.",
            source = "Interview, 1983",
            category = "Art",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "picasso_1",
            author = "Pablo Picasso",
            authorYears = "1881\u20131973",
            title = "On action",
            excerpt = "Action is the foundational key to all success. The meaning of life is to find your gift. The purpose of life is to give it away.",
            source = "Attributed conversations",
            category = "Art",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "rodin_1",
            author = "Auguste Rodin",
            authorYears = "1840\u20131917",
            title = "On patience in craft",
            excerpt = "Patience is also a form of action. Nothing is a waste of time if you use the experience wisely. The artist must create a spark before he can make a fire.",
            source = "Art: Conversations with Paul Gsell",
            category = "Art",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "hokusai_1",
            author = "Katsushika Hokusai",
            authorYears = "1760\u20131849",
            title = "On lifelong learning",
            excerpt = "From the age of six I had a mania for drawing. At seventy-three, I began to grasp the structures of nature. When I am eighty I shall have made still more progress. At ninety I shall penetrate the mystery of things.",
            source = "One Hundred Views of Mount Fuji, postscript",
            category = "Art",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "kollwitz_1",
            author = "K\u00e4the Kollwitz",
            authorYears = "1867\u20131945",
            title = "On creative duty",
            excerpt = "I do not want to die until I have faithfully made the most of my talent and cultivated the seed that was placed in me, until the last small twig has grown.",
            source = "Personal diary, 1920",
            category = "Art",
            era = "Modern"
        ),

        // ── Additional Leadership ───────────────────────────────

        DiscoverEntry(
            id = "churchill_1",
            author = "Winston Churchill",
            authorYears = "1874\u20131965",
            title = "On perseverance",
            excerpt = "Success is not final, failure is not fatal: it is the courage to continue that counts. If you\u2019re going through hell, keep going.",
            source = "Speeches and writings",
            category = "Leadership",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "keller_1",
            author = "Helen Keller",
            authorYears = "1880\u20131968",
            title = "On doors and windows",
            excerpt = "When one door of happiness closes, another opens; but often we look so long at the closed door that we do not see the one which has been opened for us. Life is either a daring adventure or nothing.",
            source = "We Bereaved, 1929",
            category = "Leadership",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "tubman_1",
            author = "Harriet Tubman",
            authorYears = "1822\u20131913",
            title = "On the first step",
            excerpt = "Every great dream begins with a dreamer. Always remember, you have within you the strength, the patience, and the passion to reach for the stars to change the world.",
            source = "Attributed sayings",
            category = "Leadership",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "teresa_1",
            author = "Mother Teresa",
            authorYears = "1910\u20131997",
            title = "On small things",
            excerpt = "Not all of us can do great things. But we can do small things with great love. If you judge people, you have no time to love them.",
            source = "Speeches and letters",
            category = "Leadership",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "parks_1",
            author = "Rosa Parks",
            authorYears = "1913\u20132005",
            title = "On quiet determination",
            excerpt = "I have learned over the years that when one\u2019s mind is made up, this diminishes fear; knowing what must be done does away with fear.",
            source = "Quiet Strength, 1994",
            category = "Leadership",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "malala_1",
            author = "Malala Yousafzai",
            authorYears = "1997\u2013",
            title = "On the power of voice",
            excerpt = "One child, one teacher, one book, one pen can change the world. When the whole world is silent, even one voice becomes powerful.",
            source = "Speech to United Nations, 2013",
            category = "Leadership",
            era = "Contemporary"
        ),

        // ── Additional Spirituality ─────────────────────────────

        DiscoverEntry(
            id = "rumi_4",
            author = "Rumi",
            authorYears = "1207\u20131273",
            title = "On starting",
            excerpt = "Start a huge, foolish project, like Noah. It makes absolutely no difference what people think of you.",
            source = "Collected poems",
            category = "Spirituality",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "hafiz_1",
            author = "Hafiz",
            authorYears = "1315\u20131390",
            title = "On fear and love",
            excerpt = "Fear is the cheapest room in the house. I would like to see you living in better conditions. Your heart and my heart are very, very old friends.",
            source = "The Gift: Poems by Hafiz",
            category = "Spirituality",
            era = "Ancient"
        ),
        DiscoverEntry(
            id = "gibran_1",
            author = "Kahlil Gibran",
            authorYears = "1883\u20131931",
            title = "On joy and sorrow",
            excerpt = "Your joy is your sorrow unmasked. The deeper that sorrow carves into your being, the more joy you can contain. When you are joyous, look deep into your heart and you shall find it is only that which has given you sorrow that is giving you joy.",
            source = "The Prophet, 1923",
            category = "Spirituality",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "gibran_2",
            author = "Kahlil Gibran",
            authorYears = "1883\u20131931",
            title = "On work and love",
            excerpt = "Work is love made visible. And if you cannot work with love but only with distaste, it is better that you should leave your work and sit at the gate of the temple and take alms of those who work with joy.",
            source = "The Prophet, 1923",
            category = "Spirituality",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "watts_1",
            author = "Alan Watts",
            authorYears = "1915\u20131973",
            title = "On the present",
            excerpt = "This is the real secret of life\u2014to be completely engaged with what you are doing in the here and now. And instead of calling it work, realize it is play.",
            source = "The Essence of Alan Watts",
            category = "Spirituality",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "tolle_1",
            author = "Eckhart Tolle",
            authorYears = "1948\u2013",
            title = "On acceptance",
            excerpt = "Whatever the present moment contains, accept it as if you had chosen it. Always work with it, not against it. Make it your friend and ally, not your enemy.",
            source = "The Power of Now, 1997",
            category = "Spirituality",
            era = "Contemporary"
        ),

        // ── Additional Literature ───────────────────────────────

        DiscoverEntry(
            id = "dostoevsky_1",
            author = "Fyodor Dostoevsky",
            authorYears = "1821\u20131881",
            title = "On suffering and growth",
            excerpt = "Pain and suffering are always inevitable for a large intelligence and a deep heart. The soul is healed by being with children, with nature, and with beauty.",
            source = "Crime and Punishment",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "rilke_1",
            author = "Rainer Maria Rilke",
            authorYears = "1875\u20131926",
            title = "On patience with the unsolved",
            excerpt = "Be patient toward all that is unsolved in your heart and try to love the questions themselves. Do not now seek the answers, which cannot be given you because you would not be able to live them.",
            source = "Letters to a Young Poet, 1903",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "rilke_2",
            author = "Rainer Maria Rilke",
            authorYears = "1875\u20131926",
            title = "On living the questions",
            excerpt = "Live the questions now. Perhaps then, someday far in the future, you will gradually, without even noticing it, live your way into the answer.",
            source = "Letters to a Young Poet, 1903",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "proust_1",
            author = "Marcel Proust",
            authorYears = "1871\u20131922",
            title = "On discovery",
            excerpt = "The real voyage of discovery consists not in seeking new landscapes, but in having new eyes.",
            source = "In Search of Lost Time",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "hemingway_1",
            author = "Ernest Hemingway",
            authorYears = "1899\u20131961",
            title = "On strength",
            excerpt = "The world breaks everyone, and afterward, many are strong at the broken places. There is nothing noble in being superior to your fellow man; true nobility is being superior to your former self.",
            source = "A Farewell to Arms",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "dickinson_1",
            author = "Emily Dickinson",
            authorYears = "1830\u20131886",
            title = "On dwelling in possibility",
            excerpt = "I dwell in Possibility, a fairer house than Prose. Forever is composed of Nows. We turn not older with years, but newer every day.",
            source = "Collected poems",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "wilde_1",
            author = "Oscar Wilde",
            authorYears = "1854\u20131900",
            title = "On being yourself",
            excerpt = "Be yourself; everyone else is already taken. To live is the rarest thing in the world. Most people exist, that is all.",
            source = "Collected aphorisms",
            category = "Literature",
            era = "Modern"
        ),
        DiscoverEntry(
            id = "morrison_1",
            author = "Toni Morrison",
            authorYears = "1931\u20132019",
            title = "On freedom and writing",
            excerpt = "If there\u2019s a book that you want to read, but it hasn\u2019t been written yet, then you must write it. You wanna fly, you got to give up the thing that weighs you down.",
            source = "Speeches and interviews",
            category = "Literature",
            era = "Contemporary"
        ),
        DiscoverEntry(
            id = "borges_1",
            author = "Jorge Luis Borges",
            authorYears = "1899\u20131986",
            title = "On time and reading",
            excerpt = "I have always imagined that Paradise will be a kind of library. Time is the substance I am made of. Time is a river which sweeps me along, but I am the river.",
            source = "Collected essays",
            category = "Literature",
            era = "Contemporary"
        )
    )

    /**
     * Returns a daily selection of entries, deterministic per day.
     * Same seed for the same day ensures consistent display.
     */
    fun getEntriesForToday(): List<DiscoverEntry> {
        val seed = LocalDate.now().dayOfYear.toLong() + LocalDate.now().year.toLong() * 365
        return allEntries.shuffled(Random(seed)).take(8)
    }

    /**
     * Returns all entries for a given category, or all if null.
     */
    fun getByCategory(category: String?): List<DiscoverEntry> {
        if (category == null) return allEntries
        return allEntries.filter { it.category == category }
    }

    /**
     * Returns a fresh random selection (for pull-to-refresh).
     */
    fun getRandomSelection(count: Int = 8): List<DiscoverEntry> {
        return allEntries.shuffled().take(count)
    }
}
