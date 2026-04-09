package com.ipl.analysis.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        if (databaseUrl == null || (!databaseUrl.startsWith("postgres://") && !databaseUrl.startsWith("postgresql://"))) {
            // Fallback to normal behavior if missing or not a Render URL
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(databaseUrl);
            return new HikariDataSource(config);
        }

        URI dbUri = new URI(databaseUrl);
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String portStr = dbUri.getPort() == -1 ? "" : ":" + dbUri.getPort();
        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + portStr + dbUri.getPath();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5); 
        return new HikariDataSource(config);
    }
}
