package com.ibm.ws.xd.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.File;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MopServlet
 */
public class MopServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String message;
	private long heapSizeAfter = 0;

    /**
     * Default constructor. 
     */
    public MopServlet() {
        // TODO Auto-generated constructor stub
    }
    

    public void init() throws ServletException
    {
        // Do required initialization
        message = "Hello World";
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	      // Set response content type
	      response.setContentType("text/html");

	      // Actual logic goes here.
	      PrintWriter out = response.getWriter();
	      long heapSizeBefore = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	      System.out.println("APP running");
	      out.println("<h1>" + message + "</h1>");
	      try
	      {
	      out.println("<h1>" + "leak memory started " + "</h1>");
	      StringBuilder stringB = new StringBuilder(); //for the 2mb one
	      String paddingString = "abcdefghijklmnopqrs";

	      while (true){
	      	 
	       
	       stringB.append(paddingString);
	       heapSizeAfter=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	      }
	      
	      }
	      catch (OutOfMemoryError E) {
	    	    // release some (all) of the above objects
	   
         	  String fileSeparator = System.getProperty("/");
              long size = (heapSizeAfter - heapSizeBefore);
	    	  long maxHeapSize= Runtime.getRuntime().maxMemory();
	    	  out.println("<h1>" + "heapSizeBefore size is  " + heapSizeBefore +"</h1>");
	    	  out.println("<h1>" + "heapSizeAfter size is  " + heapSizeAfter+"</h1>");
	    	  out.println("<h1>" + "Max heap size is  " + maxHeapSize +"</h1>");
	    	  out.println("<h1>" + "increase in size is  " + size +"</h1>");
	    	  float per= size%maxHeapSize;
	    	  float percentage=per*100;
	    	  out.println("<h1>" + "increase in size in more than " + percentage +"percentage</h1>");
	    	  out.println("<h1>" + "leak memory end " + E.getMessage()+"</h1>");
	    	  try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   	}
	      
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

