package com.proactivediary.data.db

import org.junit.Assert.*
import org.junit.Test

class MigrationTest {

    // ─── MIGRATION_11_12 structure ───

    @Test
    fun `MIGRATION_11_12 has correct start and end versions`() {
        val migration = AppDatabase.MIGRATION_11_12
        assertEquals(11, migration.startVersion)
        assertEquals(12, migration.endVersion)
    }

    // ─── MIGRATIONS array completeness ───

    @Test
    fun `MIGRATIONS array contains all 11 migrations in order`() {
        val migrations = AppDatabase.MIGRATIONS
        assertEquals(11, migrations.size)
        migrations.forEachIndexed { index, migration ->
            assertEquals(index + 1, migration.startVersion)
            assertEquals(index + 2, migration.endVersion)
        }
    }

    @Test
    fun `MIGRATIONS array has no gaps`() {
        val migrations = AppDatabase.MIGRATIONS
        for (i in 0 until migrations.size - 1) {
            assertEquals(
                "Migration ${migrations[i].endVersion} should lead to ${migrations[i + 1].startVersion}",
                migrations[i].endVersion,
                migrations[i + 1].startVersion
            )
        }
    }

    @Test
    fun `MIGRATIONS starts at version 1 and ends at 12`() {
        val migrations = AppDatabase.MIGRATIONS
        assertEquals(1, migrations.first().startVersion)
        assertEquals(12, migrations.last().endVersion)
    }

    @Test
    fun `MIGRATION_11_12 is the last migration in the array`() {
        val migrations = AppDatabase.MIGRATIONS
        assertSame(AppDatabase.MIGRATION_11_12, migrations.last())
    }
}
