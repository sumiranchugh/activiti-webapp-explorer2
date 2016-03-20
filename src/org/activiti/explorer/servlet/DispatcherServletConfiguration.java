package org.activiti.explorer.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@ComponentScan({"org.activiti.rest.editor", "org.activiti.rest.diagram"})
@EnableAsync
public class DispatcherServletConfiguration extends WebMvcConfigurationSupport {
   private final Logger log = LoggerFactory.getLogger(DispatcherServletConfiguration.class);
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private Environment environment;

   @Bean
   public SessionLocaleResolver localeResolver() {
      return new SessionLocaleResolver();
   }

   @Bean
   public LocaleChangeInterceptor localeChangeInterceptor() {
      this.log.debug("Configuring localeChangeInterceptor");
      LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
      localeChangeInterceptor.setParamName("language");
      return localeChangeInterceptor;
   }

   @Bean
   public RequestMappingHandlerMapping requestMappingHandlerMapping() {
      this.log.debug("Creating requestMappingHandlerMapping");
      RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
      requestMappingHandlerMapping.setUseSuffixPatternMatch(false);
      Object[] interceptors = new Object[]{this.localeChangeInterceptor()};
      requestMappingHandlerMapping.setInterceptors(interceptors);
      return requestMappingHandlerMapping;
   }

   public void configureMessageConverters(List converters) {
      this.addDefaultHttpMessageConverters(converters);
      Iterator i$ = converters.iterator();

      while(i$.hasNext()) {
         HttpMessageConverter converter = (HttpMessageConverter)i$.next();
         if(converter instanceof MappingJackson2HttpMessageConverter) {
            MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter)converter;
            jackson2HttpMessageConverter.setObjectMapper(this.objectMapper);
            break;
         }
      }

   }

   protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
      configurer.favorPathExtension(false);
   }
}
