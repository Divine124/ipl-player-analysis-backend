CREATE TABLE matches (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(50) UNIQUE NOT NULL,
    match_date TIMESTAMP NOT NULL,
    team1 VARCHAR(50) NOT NULL,
    team2 VARCHAR(50) NOT NULL,
    venue VARCHAR(100),
    status VARCHAR(20),
    result VARCHAR(100)
);
