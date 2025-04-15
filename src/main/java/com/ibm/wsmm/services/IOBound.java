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
public class IOBound extends HttpServlet {

	private final MicroServices microServices = new MicroServices();
	
	/**
	* @see jakarta.servlet.http.HttpServlet#void (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
			
		int noOfBytes=1000;
		
		MicroFailures micFail = new MicroFailures(req);
		if (micFail.failRequest(req, resp)) {
			return;
		}

		// initialize Writer
		resp.setContentType("text/html");
		ServletOutputStream out = resp.getOutputStream();
		out.println("<HTML><HEAD><TITLE>IO Bound</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1>IO Bound Statistics</H1>");
		out.println("<H2>Parameters</H2>");

		// Read Parameters
		try { 
			noOfBytes=Integer.parseInt((String)req.getParameter("noOfBytes"));
			out.println("<P> noOfBytes 	   :"+String.valueOf(noOfBytes)+" bytes");
		} catch(Exception e){
			out.println("<P> noOfBytes 	   :"+String.valueOf(noOfBytes)+" bytes (Default)");
		}

		micFail.printFailureParameters(out);

		// invoke Service
		try{
			microServices.IOBound(noOfBytes);
			micFail.leakRequest(); 
			micFail.deadlockRequest();
		} catch(Exception e){
			out.println("<H2> An Error occured</H2>");
			out.println("<P> "+e.getMessage());
		}

		// close Writer
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
