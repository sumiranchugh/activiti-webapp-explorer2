package org.activiti.explorer.conf;

import java.util.ArrayList;
import javax.sql.DataSource;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.explorer.form.MonthFormType;
import org.activiti.explorer.form.ProcessDefinitionFormType;
import org.activiti.explorer.form.UserFormType;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ActivitiEngineConfiguration {
   private final Logger log = LoggerFactory.getLogger(ActivitiEngineConfiguration.class);
   @Autowired
   protected Environment environment;

   @Bean
   public DataSource dataSource() {
      SimpleDriverDataSource ds = new SimpleDriverDataSource();

      try {
         Class e = Class.forName(this.environment.getProperty("jdbc.driver", "org.h2.Driver"));
         ds.setDriverClass(e);
      } catch (Exception var3) {
         this.log.error("Error loading driver class", var3);
      }

      ds.setUrl(this.environment.getProperty("jdbc.url", "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000"));
      ds.setUsername(this.environment.getProperty("jdbc.username", "sa"));
      ds.setPassword(this.environment.getProperty("jdbc.password", ""));
      return ds;
   }

   @Bean(
      name = {"transactionManager"}
   )
   public PlatformTransactionManager annotationDrivenTransactionManager() {
      DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
      transactionManager.setDataSource(this.dataSource());
      return transactionManager;
   }

   @Bean(
      name = {"processEngineFactoryBean"}
   )
   public ProcessEngineFactoryBean processEngineFactoryBean() {
      ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
      factoryBean.setProcessEngineConfiguration(this.processEngineConfiguration());
      return factoryBean;
   }

   @Bean(
      name = {"processEngine"}
   )
   public ProcessEngine processEngine() {
      try {
         return this.processEngineFactoryBean().getObject();
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }
   }

   @Bean(
      name = {"processEngineConfiguration"}
   )
   public ProcessEngineConfigurationImpl processEngineConfiguration() {
      SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
      processEngineConfiguration.setDataSource(this.dataSource());
      processEngineConfiguration.setDatabaseSchemaUpdate(this.environment.getProperty("engine.schema.update", "true"));
      processEngineConfiguration.setTransactionManager(this.annotationDrivenTransactionManager());
      processEngineConfiguration.setJobExecutorActivate(Boolean.valueOf(this.environment.getProperty("engine.activate.jobexecutor", "false")).booleanValue());
      processEngineConfiguration.setAsyncExecutorEnabled(Boolean.valueOf(this.environment.getProperty("engine.asyncexecutor.enabled", "true")).booleanValue());
      processEngineConfiguration.setAsyncExecutorActivate(Boolean.valueOf(this.environment.getProperty("engine.asyncexecutor.activate", "true")).booleanValue());
      processEngineConfiguration.setHistory(this.environment.getProperty("engine.history.level", "full"));
      String mailEnabled = this.environment.getProperty("engine.email.enabled");
      if("true".equals(mailEnabled)) {
         processEngineConfiguration.setMailServerHost(this.environment.getProperty("engine.email.host"));
         int formTypes = 1025;
         String emailPortProperty = this.environment.getProperty("engine.email.port");
         if(StringUtils.isNotEmpty(emailPortProperty)) {
            formTypes = Integer.valueOf(emailPortProperty).intValue();
         }

         processEngineConfiguration.setMailServerPort(formTypes);
         String emailUsernameProperty = this.environment.getProperty("engine.email.username");
         if(StringUtils.isNotEmpty(emailUsernameProperty)) {
            processEngineConfiguration.setMailServerUsername(emailUsernameProperty);
         }

         String emailPasswordProperty = this.environment.getProperty("engine.email.password");
         if(StringUtils.isNotEmpty(emailPasswordProperty)) {
            processEngineConfiguration.setMailServerPassword(emailPasswordProperty);
         }
      }

      ArrayList formTypes1 = new ArrayList();
      formTypes1.add(new UserFormType());
      formTypes1.add(new ProcessDefinitionFormType());
      formTypes1.add(new MonthFormType());
      processEngineConfiguration.setCustomFormTypes(formTypes1);
      return processEngineConfiguration;
   }

   @Bean
   public RepositoryService repositoryService() {
      return this.processEngine().getRepositoryService();
   }

   @Bean
   public RuntimeService runtimeService() {
      return this.processEngine().getRuntimeService();
   }

   @Bean
   public TaskService taskService() {
      return this.processEngine().getTaskService();
   }

   @Bean
   public HistoryService historyService() {
      return this.processEngine().getHistoryService();
   }

   @Bean
   public FormService formService() {
      return this.processEngine().getFormService();
   }

   @Bean
   public IdentityService identityService() {
      return this.processEngine().getIdentityService();
   }

   @Bean
   public ManagementService managementService() {
      return this.processEngine().getManagementService();
   }
}
