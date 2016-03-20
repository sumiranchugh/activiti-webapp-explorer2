package org.activiti.explorer.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration.Dynamic;
import org.activiti.explorer.conf.ApplicationConfiguration;
import org.activiti.explorer.servlet.DispatcherServletConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

public class WebConfigurer implements ServletContextListener {
   private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);
   public AnnotationConfigWebApplicationContext context;

   public void setContext(AnnotationConfigWebApplicationContext context) {
      this.context = context;
   }

   public void contextInitialized(ServletContextEvent sce) {
      ServletContext servletContext = sce.getServletContext();
      this.log.debug("Configuring Spring root application context");
      AnnotationConfigWebApplicationContext rootContext = null;
      if(this.context == null) {
         rootContext = new AnnotationConfigWebApplicationContext();
         rootContext.register(new Class[]{ApplicationConfiguration.class});
         rootContext.refresh();
      } else {
         rootContext = this.context;
      }

      servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, rootContext);
      this.initSpring(servletContext, rootContext);
      this.log.debug("Web application fully configured");
   }

   private Dynamic initSpring(ServletContext servletContext, AnnotationConfigWebApplicationContext rootContext) {
      this.log.debug("Configuring Spring Web application context");
      AnnotationConfigWebApplicationContext dispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
      dispatcherServletConfiguration.setParent(rootContext);
      dispatcherServletConfiguration.register(new Class[]{DispatcherServletConfiguration.class});
      this.log.debug("Registering Spring MVC Servlet");
      Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", new DispatcherServlet(dispatcherServletConfiguration));
      dispatcherServlet.addMapping(new String[]{"/service/*"});
      dispatcherServlet.setLoadOnStartup(1);
      dispatcherServlet.setAsyncSupported(true);
      return dispatcherServlet;
   }

   public void contextDestroyed(ServletContextEvent sce) {
      this.log.info("Destroying Web application");
      WebApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
      AnnotationConfigWebApplicationContext gwac = (AnnotationConfigWebApplicationContext)ac;
      gwac.close();
      this.log.debug("Web application destroyed");
   }
}
