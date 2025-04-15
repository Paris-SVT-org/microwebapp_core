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
public class sleepBound extends HttpServlet {

	private final MicroServices microServices = new MicroServices();
	
	/**
	* @see jakarta.servlet.http.HttpServlet#void (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
			

		long inTime = System.currentTimeMillis();
		boolean deterministic = true;
		boolean precise =  false;
		int ms = 1000;

		MicroFailures micFail = new MicroFailures(req);
		if (micFail.failRequest(req, resp)) {
			return;
		}

		// initialize Writer
		resp.setContentType("text/html");
		ServletOutputStream out = resp.getOutputStream();
		out.println("<HTML><HEAD><TITLE>MicroService</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1>MicroService Statistics</H1>");
		out.println("<H2>Parameters</H2>");

		// get the parameter from the request
		try { 
			deterministic = ((String)req.getParameter("deterministic")).equals("true");
			out.println("<P> deterministic :"+(deterministic?"Yes":"No"));
		} catch(Exception e){
			out.println("<P> deterministic :"+(deterministic?"Yes (Default)":"No (Default)"));
		}
		try { 
			precise = ((String)req.getParameter("precise")).equals("true");
			out.println("<P> precise       :"+(precise?"Yes":"No"));
		} catch(Exception e){
			out.println("<P> precise       :"+(precise?"Yes (Default)":"No (Default)"));
		}
		try { 
			ms=Integer.parseInt((String)req.getParameter("ms"));
			out.println("<P> ms 	       :"+String.valueOf(ms)+" ms");
		} catch(Exception e){
			out.println("<P> ms 	       :"+String.valueOf(ms)+" ms (Default)");
		}

		micFail.printFailureParameters(out);

		// invoke Service
		try{
			out.println("<H2>Base Service Time</H2>");
			out.println("<P> "+microServices.sleepBound(ms,deterministic,precise)+" ms");
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
