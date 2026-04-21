package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class Task07JdbcTransactionConfig {

    @Bean("task07TransactionManager")
    public PlatformTransactionManager task07TransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
