// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.wsmm.services;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.net.*;

/**
 * @version 	1.0
 * @author
 */
public class CPUBound extends HttpServlet {

	private final MicroServices microServices = new MicroServices();
	
	/**
	* @see jakarta.servlet.http.HttpServlet#void (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		MicroFailures micFail = new MicroFailures(req);
		if (micFail.failRequest(req, resp)) {
			return;
		}

		// initialize Writer
		resp.setContentType("text/html");
		ServletOutputStream out = resp.getOutputStream();
		out.println("<HTML><HEAD><TITLE>CPU Bound</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1>CPU Bound Statistics</H1>");
		out.println("<H2>Parameters</H2>");

		// get the parameter from the request
		int count = 0;
		try { 
			count=Integer.parseInt((String)req.getParameter("count"));
			out.println("<P> count   :"+String.valueOf(count));
		} catch(Exception e){
			out.println("<P> count   :"+String.valueOf(count)+" (Default)");
		}
		
		micFail.printFailureParameters(out);

		// Invoke Service
		try{
			microServices.CPUBound(count);
			micFail.leakRequest();
			micFail.deadlockRequest();		
		} catch (Exception e){
			out.println("<H2>An Error occured</H2>");
			out.println("<P> "+e.getMessage());
		}
		
		// Close OutputStream
		out.println("</BODY></HTML>");
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
