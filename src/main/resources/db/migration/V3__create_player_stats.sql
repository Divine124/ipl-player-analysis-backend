CREATE TABLE player_stats (
    id SERIAL PRIMARY KEY,
    player_id INTEGER REFERENCES players(id) ON DELETE CASCADE,
    matches_played INTEGER DEFAULT 0,
    runs INTEGER DEFAULT 0,
    wickets INTEGER DEFAULT 0,
    batting_average DECIMAL(5,2),
    strike_rate DECIMAL(6,2),
    bowling_average DECIMAL(5,2),
    economy DECIMAL(4,2),
    highest_score INTEGER,
    best_bowling_figures VARCHAR(20),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
