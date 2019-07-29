package org.folio.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class PersistanceConfig {

  @Bean("jdbcTemplate")
  public JdbcTemplate createJdbcTemplateBean() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl("jdbc:postgresql://localhost:6000/postgres");
    dataSource.setUsername("username");
    dataSource.setPassword("password");
    return new JdbcTemplate(dataSource);
  }
}
