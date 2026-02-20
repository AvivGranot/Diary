package com.proactivediary.data.repository

import com.proactivediary.data.db.dao.TemplateDao
import com.proactivediary.data.db.entities.TemplateEntity
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val templateDao: TemplateDao
) {
    private val gson = Gson()

    fun getAllTemplates(): Flow<List<TemplateEntity>> = templateDao.getAllTemplates()

    fun getByCategory(category: String): Flow<List<TemplateEntity>> = templateDao.getByCategory(category)

    suspend fun getById(id: String): TemplateEntity? = templateDao.getById(id)

    suspend fun ensureBuiltInTemplates() {
        // Use IGNORE strategy — inserts new templates without overwriting existing ones
        templateDao.insertAll(builtInTemplates())
    }

    private fun builtInTemplates(): List<TemplateEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // ── ORIGINAL 10 ──────────────────────────────────────
            TemplateEntity(
                id = "tpl_gratitude",
                title = "Gratitude",
                description = "Reflect on what you're thankful for",
                prompts = gson.toJson(listOf(
                    "3 things I'm grateful for today...",
                    "Why these matter to me...",
                    "One person I appreciate and why..."
                )),
                category = "gratitude",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_morning_pages",
                title = "Morning Pages",
                description = "Stream of consciousness writing",
                prompts = gson.toJson(listOf(
                    "Write whatever comes to mind. Don't stop for 10 minutes."
                )),
                category = "creative",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_evening_reflection",
                title = "Evening Reflection",
                description = "Review and process your day",
                prompts = gson.toJson(listOf(
                    "Best moment today...",
                    "What I learned...",
                    "Tomorrow I want to..."
                )),
                category = "reflection",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_weekly_review",
                title = "Weekly Review",
                description = "Look back at your week",
                prompts = gson.toJson(listOf(
                    "Wins this week...",
                    "Challenges I faced...",
                    "Goals for next week..."
                )),
                category = "reflection",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_thought_record",
                title = "Thought Record",
                description = "Examine and reframe your thoughts",
                prompts = gson.toJson(listOf(
                    "Situation: What happened?",
                    "Automatic thought: What went through my mind?",
                    "Evidence for and against this thought...",
                    "Balanced thought: A more helpful perspective..."
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_five_minute",
                title = "5-Minute Journal",
                description = "Quick daily check-in",
                prompts = gson.toJson(listOf(
                    "I am grateful for...",
                    "What would make today great?",
                    "Daily affirmation: I am..."
                )),
                category = "gratitude",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_travel",
                title = "Travel Journal",
                description = "Capture your adventures",
                prompts = gson.toJson(listOf(
                    "Where am I?",
                    "What did I see and do today?",
                    "Best moment of the day...",
                    "Food I tried..."
                )),
                category = "creative",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_letter_to_self",
                title = "Letter to Self",
                description = "Write to your future self",
                prompts = gson.toJson(listOf(
                    "Dear future me,\n\nRight now I'm feeling...\n\nWhat I want you to remember..."
                )),
                category = "growth",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_mood_checkin",
                title = "Feelings Check-In",
                description = "Understand how you're feeling",
                prompts = gson.toJson(listOf(
                    "How am I feeling right now? (1-10)",
                    "What's driving this feeling?",
                    "What would help right now?"
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_creative_writing",
                title = "Creative Writing",
                description = "Let your imagination flow",
                prompts = gson.toJson(listOf(
                    "Write a scene: You open a door and find...",
                    "Describe the scene using all five senses...",
                    "What happens next?"
                )),
                category = "creative",
                isBuiltIn = true,
                createdAt = now
            ),

            // ── NEW PREMIUM TEMPLATES ────────────────────────────

            // LIFE TRANSITIONS
            TemplateEntity(
                id = "tpl_starting_therapy",
                title = "Starting Therapy",
                description = "Process your therapy sessions",
                prompts = gson.toJson(listOf(
                    "What came up in my session today?",
                    "Something my therapist said that stayed with me...",
                    "One thing I want to explore further...",
                    "How I feel right now, after reflecting on this..."
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_job_transition",
                title = "Job Transition",
                description = "Navigate career changes with clarity",
                prompts = gson.toJson(listOf(
                    "Where I am in this transition...",
                    "What excites me about what's ahead...",
                    "What scares me, honestly...",
                    "One concrete step I can take this week..."
                )),
                category = "growth",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_grief",
                title = "Grief Processing",
                description = "A gentle space for loss and remembrance",
                prompts = gson.toJson(listOf(
                    "Today I'm remembering...",
                    "A memory that makes me smile...",
                    "What I wish I could say...",
                    "Something I'm carrying right now..."
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_new_parent",
                title = "New Parent",
                description = "Document early parenthood honestly",
                prompts = gson.toJson(listOf(
                    "A small moment with my child today...",
                    "How I'm really doing...",
                    "Something nobody warned me about...",
                    "What I want to remember from this stage..."
                )),
                category = "reflection",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_moving",
                title = "New City, New Chapter",
                description = "Processing a big move",
                prompts = gson.toJson(listOf(
                    "What I noticed about this place today...",
                    "Something I miss from before...",
                    "Someone or something new I encountered...",
                    "How this place is changing me..."
                )),
                category = "growth",
                isBuiltIn = true,
                createdAt = now
            ),

            // RELATIONSHIPS
            TemplateEntity(
                id = "tpl_relationship_checkin",
                title = "Relationship Check-In",
                description = "Honest reflection on a relationship",
                prompts = gson.toJson(listOf(
                    "How are we doing, really?",
                    "Something I appreciate about this person...",
                    "Something I haven't said out loud yet...",
                    "What I need right now in this relationship..."
                )),
                category = "reflection",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_difficult_conversation",
                title = "Before a Hard Conversation",
                description = "Prepare for something you need to say",
                prompts = gson.toJson(listOf(
                    "What I need to communicate...",
                    "How I'm feeling about having this conversation...",
                    "What I hope the other person understands...",
                    "The outcome I'd like, realistically..."
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_forgiveness",
                title = "Letting Go",
                description = "Work through resentment or regret",
                prompts = gson.toJson(listOf(
                    "What I'm holding onto...",
                    "How it's affecting me day to day...",
                    "What would it feel like to release this?",
                    "One small step toward letting go..."
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),

            // GROWTH & CAREER
            TemplateEntity(
                id = "tpl_monthly_review",
                title = "Monthly Review",
                description = "Zoom out on the past 30 days",
                prompts = gson.toJson(listOf(
                    "Biggest win this month...",
                    "Biggest surprise or unexpected lesson...",
                    "Something I dropped or deprioritized...",
                    "What I want next month to feel like..."
                )),
                category = "reflection",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_imposter",
                title = "Imposter Syndrome",
                description = "When you feel like a fraud",
                prompts = gson.toJson(listOf(
                    "The situation triggering this feeling...",
                    "Evidence that I actually belong here...",
                    "Who I'd reassure if they told me this about themselves...",
                    "What I'd say to them\u2014and to myself..."
                )),
                category = "growth",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_decision",
                title = "Big Decision",
                description = "Think through a crossroads clearly",
                prompts = gson.toJson(listOf(
                    "The decision I'm facing...",
                    "Option A: what it looks like, pros, cons...",
                    "Option B: what it looks like, pros, cons...",
                    "What my gut is telling me...",
                    "What I'll wish I chose in 5 years..."
                )),
                category = "growth",
                isBuiltIn = true,
                createdAt = now
            ),

            // WELLNESS & MENTAL HEALTH
            TemplateEntity(
                id = "tpl_anxiety_release",
                title = "Anxiety Release",
                description = "Empty your worried mind onto the page",
                prompts = gson.toJson(listOf(
                    "Everything I'm anxious about right now, unfiltered...",
                    "Which of these are in my control?",
                    "One thing I can do about it in the next hour...",
                    "A truth I need to remind myself of..."
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_body_scan",
                title = "Body Scan Journal",
                description = "Listen to what your body is telling you",
                prompts = gson.toJson(listOf(
                    "Where am I holding tension right now?",
                    "What does my energy level feel like today?",
                    "How did I sleep?",
                    "One kind thing I can do for my body today..."
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_self_compassion",
                title = "Self-Compassion Letter",
                description = "Write to yourself with kindness",
                prompts = gson.toJson(listOf(
                    "What I'm struggling with right now...",
                    "How I'd comfort a close friend going through this...",
                    "The kindest, most honest thing I can say to myself..."
                )),
                category = "wellness",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_sleep_journal",
                title = "Before Bed",
                description = "Quiet your mind before sleep",
                prompts = gson.toJson(listOf(
                    "One thing I'm releasing from today...",
                    "Something that went well, even if small...",
                    "What I'm looking forward to tomorrow...",
                    "Letting go of the rest. Goodnight."
                )),
                category = "reflection",
                isBuiltIn = true,
                createdAt = now
            ),

            // CREATIVE & EXPRESSIVE
            TemplateEntity(
                id = "tpl_unsent_letter",
                title = "The Unsent Letter",
                description = "Say what you never said",
                prompts = gson.toJson(listOf(
                    "Dear ___,\n\nI never told you this, but...",
                    "What I wish had gone differently...",
                    "What I hope for you now..."
                )),
                category = "creative",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_dream_journal",
                title = "Dream Journal",
                description = "Capture dreams before they fade",
                prompts = gson.toJson(listOf(
                    "What I remember from my dream...",
                    "The strongest image or feeling...",
                    "Does this connect to anything in my waking life?"
                )),
                category = "creative",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_snapshot",
                title = "Snapshot",
                description = "Freeze this exact moment in time",
                prompts = gson.toJson(listOf(
                    "Right now I'm sitting...",
                    "I can hear...",
                    "The light is...",
                    "I'm thinking about...",
                    "If someone could see me right now, they'd see..."
                )),
                category = "creative",
                isBuiltIn = true,
                createdAt = now
            ),

            // GRATITUDE (EXPANDED)
            TemplateEntity(
                id = "tpl_gratitude_deep",
                title = "Deep Gratitude",
                description = "Go beyond the surface",
                prompts = gson.toJson(listOf(
                    "Something I usually take for granted...",
                    "A person who shaped who I am, and how...",
                    "A hardship I'm now grateful for, and why...",
                    "Something beautiful I noticed today..."
                )),
                category = "gratitude",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_wins",
                title = "Wins & Celebrations",
                description = "Acknowledge what's going right",
                prompts = gson.toJson(listOf(
                    "Something I accomplished recently, no matter how small...",
                    "A moment I'm proud of this week...",
                    "A compliment I received or a kind thing someone did...",
                    "What this tells me about myself..."
                )),
                category = "gratitude",
                isBuiltIn = true,
                createdAt = now
            ),

            // IDENTITY & VALUES
            TemplateEntity(
                id = "tpl_values",
                title = "Values Check-In",
                description = "Are you living aligned?",
                prompts = gson.toJson(listOf(
                    "My top 3 values right now...",
                    "A moment this week where I lived one of these values...",
                    "A moment where I didn't, and what happened...",
                    "One adjustment I want to make..."
                )),
                category = "growth",
                isBuiltIn = true,
                createdAt = now
            ),
            TemplateEntity(
                id = "tpl_seasons",
                title = "Season of Life",
                description = "Name the season you're in",
                prompts = gson.toJson(listOf(
                    "The season of life I'm in right now...",
                    "What this season is teaching me...",
                    "What I want to let go of from the last season...",
                    "What I want to carry into the next..."
                )),
                category = "growth",
                isBuiltIn = true,
                createdAt = now
            )
        )
    }
}
