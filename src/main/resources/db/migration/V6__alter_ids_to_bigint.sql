ALTER TABLE player_stats DROP CONSTRAINT player_stats_player_id_fkey;
ALTER TABLE analysis_reports DROP CONSTRAINT analysis_reports_player_id_fkey;

ALTER TABLE users ALTER COLUMN id TYPE BIGINT;
ALTER TABLE players ALTER COLUMN id TYPE BIGINT;
ALTER TABLE matches ALTER COLUMN id TYPE BIGINT;
ALTER TABLE player_stats ALTER COLUMN id TYPE BIGINT;
ALTER TABLE player_stats ALTER COLUMN player_id TYPE BIGINT;
ALTER TABLE analysis_reports ALTER COLUMN id TYPE BIGINT;
ALTER TABLE analysis_reports ALTER COLUMN player_id TYPE BIGINT;

ALTER TABLE player_stats ADD CONSTRAINT player_stats_player_id_fkey FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE;
ALTER TABLE analysis_reports ADD CONSTRAINT analysis_reports_player_id_fkey FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE;
