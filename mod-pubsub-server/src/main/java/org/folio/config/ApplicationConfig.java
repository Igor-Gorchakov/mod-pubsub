package org.folio.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
  "org.folio.rest",
  "org.folio.dao",
  "org.folio.service"})
public class ApplicationConfig {
}
