// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004, 2005
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.wsmm.services;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.net.*;
import java.util.Date;

/**
 * @version 	1.0
 * @author
 */
public class simpleSleep extends HttpServlet {
	
	/**
	* @see jakarta.servlet.http.HttpServlet#void (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		Date inTime = new Date();
		try {
		    Thread.sleep(100);
		} catch (InterruptedException ex) {
		    
		}
	    Date outTime = new Date();
	    long dt = outTime.getTime() - inTime.getTime();
		resp.setContentType("text/plain");
		ServletOutputStream out = resp.getOutputStream();
		out.println("Started " + inTime + ", stopped " + outTime
		        + ", slept for " + dt + " ms.");
		out.close();
	}

	/**
	* @see jakarta.servlet.http.HttpServlet#void (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		doGet(req,resp);
	}

}
