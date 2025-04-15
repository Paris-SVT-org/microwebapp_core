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
import java.util.Date;

/**
 * @version 	1.0
 * @author
 */
public class memBoundPerSession extends HttpServlet {

	private final MicroServices microServices = new MicroServices();
	
	private String user = null;
	private boolean login = false, logout = false;
	private HttpSession hsess = null;
	
    	
	/*public void service(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		login = "true".equalsIgnoreCase(req.getParameter("login"));
		logout = "true".equalsIgnoreCase(req.getParameter("logout"));
		//user = req.getParameter("user");
        
				
		if (login)
		{
			hsess = req.getSession(login);   //Set up a http session
			}
		
		if(logout)
		{
			hsess.invalidate();        //http session invalidate
		}   
	    
		
	}*/
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
			
		int chunkSize=2;
		int m=2;
				
		login = "true".equalsIgnoreCase(req.getParameter("login"));
		logout = "true".equalsIgnoreCase(req.getParameter("logout"));

		/*MicroFailures micFail = new MicroFailures(req);
		if (micFail.failRequest(req, resp)) {
			return;
		}*/

		// initialize Writer
		resp.setContentType("text/html");
		ServletOutputStream out = resp.getOutputStream();
		out.println("<HTML><HEAD><TITLE>mem Bound Per Session</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1>mem Bound Per Session Statistics</H1>");
		out.println("<H2>Parameters</H2>");
				
		
		if (login)
		{
			if(req.isRequestedSessionIdValid()){
				out.println("<P> Session with ID: "+req.getSession().getId()+" has already been created.");
			} 
			else
			{
				hsess = req.getSession(login);   //Set up a http session
				out.println("<P> login :"+"true");
				out.println("<P> Session ID: "+hsess.getId());
				out.println("<P> Is requested session id from cookie? "+req.isRequestedSessionIdFromCookie());
				out.println("<P> Is requested session id from URL? "+req.isRequestedSessionIdFromURL());		
				
				out.println("<H2>Set Up a New Session</H2>");
				out.println("<P>Session created at: "+ new Date(hsess.getCreationTime()));
	              
				//	Get the parameter from the request. Only the dialog initial req has the rights to 
				//  invoke MicroService
				
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

				//micFail.printFailureParameters(out);
				
				//out.println("<P> The return code of this session: "+ hsess);

				// invoke Service
				try{
					int[][] block = microServices.memAlloc(chunkSize, m);
					hsess.setAttribute("Memory", block);   //attach the allocated memory to http session
					out.println("<P> Attached "+chunkSize*m*4+" bytes to this http session");
					//out.println("<P> The return code of this session: "+ hsess);
					
					/*micFail.leakRequest(); 
					micFail.deadlockRequest();*/
				} catch(Exception e){
					out.println("<H2> An Error occured </H2>");
					out.println("<P> "+e.getMessage());
					out.println("<P> Session Invalidate");
					hsess.invalidate();
				}
			}
			
			
		}		
		if(logout)
		{
			hsess = req.getSession(false);
			//out.println("<P> The return code of this session: "+hsess);
		    if (hsess != null){
		    	out.println("<P> logout :"+"true");
		    	out.println("<P> About to Invalidate session with ID: "+hsess.getId());
		    	//int[][] block=(int[][]) hsess.getAttribute("Memory");
		    	//out.println("<P> The attribute of Memory is: "+block);
				hsess.invalidate();        //http session invalidate
				hsess = req.getSession(false);
				out.println("<P> The return code of this session after invalidating: "+hsess);
				out.println("<H2>Session Invalidate</H2>");				
		    } else
		    {
		    	out.println("<P> Session has already been invalidated");
		    }
		
			/*if(req.isRequestedSessionIdValid()){
				hsess = req.getSession();
				out.println("<P> About to Invalidate session with ID: "+hsess.getId());
				hsess.invalidate();        //http session invalidate
				
				out.println("<P> Is requested session ID valida? "+req.isRequestedSessionIdValid());
				out.println("<P> logout :"+"true");
				out.println("<H2>Session Invalidate</H2>");
				
			} else
			{
				out.println("<P> Session has already been invalidated");
			}*/
			
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
