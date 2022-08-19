package com.example.pocbatch;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@EnableScheduling
@SpringBootApplication
public class PocBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(PocBatchApplication.class, args);
    }

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(final DataSourceProperties dataSourceProperties) {
        return schedulerFactoryBean -> {
            DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                    .url(dataSourceProperties.getUrl() + "?currentSchema=scheduler")
                    .build();
            schedulerFactoryBean.setDataSource(dataSource);
        };
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            Flyway.configure()
                    .locations("db/migration/quartz")
                    .dataSource(flyway.getConfiguration().getDataSource())
                    .schemas("scheduler")
                    .load()
                    .migrate();
        };
    }


}
