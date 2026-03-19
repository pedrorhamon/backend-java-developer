-- Add indexes for improved query performance
-- Index for episode queries grouped by show and season
CREATE INDEX IF NOT EXISTS idx_episode_fk_show ON episode(fk_show);

-- Composite index for season-based episode queries
CREATE INDEX IF NOT EXISTS idx_episode_show_season ON episode(fk_show, season);

-- Index for case-insensitive name filtering on shows
CREATE INDEX IF NOT EXISTS idx_show_name_lower ON show(LOWER(name));
