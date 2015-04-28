package com.glassdoor.planout4j.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.glassdoor.planout4j.NamespaceFactory;
import com.glassdoor.planout4j.RefreshableNamespaceFactory;

/**
 * This is the face of Planout4j in the context of Spring.
 * Import this into your Spring config and you're ready to use it in your application.
 */
@Configuration
@EnableScheduling
public class Planout4jAppContext {

   @Bean
   public NamespaceFactory namespaceFactory() {
      return new RefreshableNamespaceFactory();
   }

}
