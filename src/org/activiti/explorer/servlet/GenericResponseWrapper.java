package org.activiti.explorer.servlet;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.activiti.explorer.servlet.FilterServletOutputStream;

public class GenericResponseWrapper extends HttpServletResponseWrapper {
   private ByteArrayOutputStream output = new ByteArrayOutputStream();
   private int contentLength;
   private String contentType;

   public GenericResponseWrapper(HttpServletResponse response) {
      super(response);
   }

   public byte[] getData() {
      return this.output.toByteArray();
   }

   public ServletOutputStream getOutputStream() {
      return new FilterServletOutputStream(this.output);
   }

   public PrintWriter getWriter() {
      return new PrintWriter(this.getOutputStream(), true);
   }

   public void setContentLength(int length) {
      this.contentLength = length;
      super.setContentLength(length);
   }

   public int getContentLength() {
      return this.contentLength;
   }

   public void setContentType(String type) {
      this.contentType = type;
      super.setContentType(type);
   }

   public String getContentType() {
      return this.contentType;
   }
}
