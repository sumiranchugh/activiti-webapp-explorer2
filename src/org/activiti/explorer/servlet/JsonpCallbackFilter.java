package org.activiti.explorer.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.activiti.explorer.servlet.GenericResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonpCallbackFilter implements Filter {
   private static Logger log = LoggerFactory.getLogger(JsonpCallbackFilter.class);

   public void init(FilterConfig fConfig) throws ServletException {
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      HttpServletResponse httpResponse = (HttpServletResponse)response;
      Map parms = httpRequest.getParameterMap();
      if(parms.containsKey("callback")) {
         if(log.isDebugEnabled()) {
            log.debug("Wrapping response with JSONP callback \'" + ((String[])parms.get("callback"))[0] + "\'");
         }

         ServletOutputStream out = httpResponse.getOutputStream();
         GenericResponseWrapper wrapper = new GenericResponseWrapper(httpResponse);
         chain.doFilter(request, wrapper);
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         outputStream.write((new String(((String[])parms.get("callback"))[0] + "(")).getBytes());
         outputStream.write(wrapper.getData());
         outputStream.write((new String(");")).getBytes());
         byte[] jsonpResponse = outputStream.toByteArray();
         wrapper.setContentType("text/javascript;charset=UTF-8");
         wrapper.setContentLength(jsonpResponse.length);
         out.write(jsonpResponse);
         out.close();
      } else {
         chain.doFilter(request, response);
      }

   }

   public void destroy() {
   }
}
