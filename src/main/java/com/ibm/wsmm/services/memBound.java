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
public class memBound extends HttpServlet {

	private final MicroServices microServices = new MicroServices();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
			
		int chunkSize=2;
		int m=2;
		int nInner=2;
		int sleepMs=2;
		int nOuter=2;

		MicroFailures micFail = new MicroFailures(req);
		if (micFail.failRequest(req, resp)) {
			return;
		}

		// initialize Writer
		resp.setContentType("text/html");
		ServletOutputStream out = resp.getOutputStream();
		out.println("<HTML><HEAD><TITLE>mem Bound</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1>mem Bound Statistics</H1>");
		out.println("<H2>Parameters</H2>");

		// get the parameter from the request
		try { 
			chunkSize=	Integer.parseInt((String)req.getParameter("chunkSize"));
			out.println("<P> chunkSize :"+String.valueOf(chunkSize)+" integers");
		} catch(Exception e){
			out.println("<P> chunkSize :"+String.valueOf(chunkSize)+" integers (Default)");
		}
		try { 
			m= Integer.parseInt((String)req.getParameter("m"));
			out.println("<P> m         :"+String.valueOf(m)+" blocks");
		} catch(Exception e){
			out.println("<P> m         :"+String.valueOf(m)+" blocks (Default)");
		}
		try { 
			nInner=		Integer.parseInt((String)req.getParameter("nInner"));
			out.println("<P> nInner    :"+String.valueOf(nInner)+" counts");
		} catch(Exception e){
			out.println("<P> nInner    :"+String.valueOf(nInner)+" counts (Default)");
		}
		try { 
			sleepMs=	Integer.parseInt((String)req.getParameter("sleepMs"));
			out.println("<P> sleepMs   :"+String.valueOf(sleepMs)+" ms");
		} catch(Exception e){
			out.println("<P> sleepMs   :"+String.valueOf(sleepMs)+" ms (Default)");
		}
		try { 
			nOuter=		Integer.parseInt((String)req.getParameter("nOuter"));
			out.println("<P> nOuter    :"+String.valueOf(nOuter)+" counts");
		} catch(Exception e){
			out.println("<P> nOuter    :"+String.valueOf(nOuter)+" counts (Default)");
		}

		micFail.printFailureParameters(out);

		// invoke Service
		try{
			out.println("<H2>Sum</H2>");
			out.println("<P> "+microServices.memBound(chunkSize,m,nInner,sleepMs,nOuter));
			micFail.leakRequest(); 
			micFail.deadlockRequest();
		} catch(Exception e){
			out.println("<H2> An Error occured </H2>");
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
