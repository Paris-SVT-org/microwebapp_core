package com.ibm.ws.sessionTester;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import coreservlets.ServletUtilities;

/**
 * This servlet is part of a test application that is being used to test
 * DRS memory to memory replication functionality.
 * @version 	1.0
 * @author		Aditya Desai
 */
public class SessionTester extends HttpServlet {

    static boolean bindingListenerCalled = false;

	/**
	 * Instance Variables
	 */
	private HashMap objectMap;

	public void init() {
		objectMap = new HashMap();
	}

	/**
	 * Method doGet
	 * @see jakarta.servlet.http.HttpServlet#doGet (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		
			System.out.println("SessionTester.doGet called");
		
			resp.setContentType("text/html");
			PrintWriter out = resp.getWriter();
			String title = "Session Tracking Example";
			HttpSession session = req.getSession(true);
			String heading;
			String reqURI = null;
			String encReqURI = null;
            boolean checkBindingListenerStatus = false;

			boolean invalidate = false;
			String invalidateString = new String();

			int invalTimeout = 0;
			String invalTimeoutString = null;
						

			String serverName =(String) getServletContext().getAttribute("com.ibm.websphere.servlet.application.host"); //Get WAS server name.
			String appName = (String)  getServletContext().getAttribute("com.ibm.websphere.servlet.application.name"); //Get Application name.
			

			try {
				invalidateString = req.getParameter("invalidate");
				invalidate = Boolean.valueOf(invalidateString).booleanValue();

				invalTimeoutString = req.getParameter("invalTimeout");
				if(invalTimeoutString != null) {
					invalTimeout = Integer.parseInt(invalTimeoutString);
				}
                
                checkBindingListenerStatus = (req.getParameter("checkBindingListenerStatus") != null);    
			}
			catch (Throwable t) {}

			System.out.println("SessionTester.doGet params(checkBinding, inval, invalTO): " +
					checkBindingListenerStatus + ", " + invalidate + ", " + invalTimeout);
			System.out.println("SessionTester.doGet session retrieved is:  \n" + session);
			
			if(invalTimeout != 0) {
				session.setMaxInactiveInterval(invalTimeout * 60);
			}


			if (invalidate) {
				session.invalidate();
				out.println(ServletUtilities.headWithTitle(title) +
				"<BODY BGCOLOR=\"$FDF5E6\">\n" +
				"<H1 ALIGN=\"CENTER\">" + "Thanks. Please visit again!" + "</H1>\n" +
				"</BODY></HTML>");
				return;
			}
            

			IntWrapper accessCount = (IntWrapper)session.getAttribute("accessCount");

			if(accessCount == null) {
				accessCount = new IntWrapper(0);
				heading = "Welcome, Newcomer.";
			}
			else {
				heading = "Welcome back.";
				accessCount.incrementValue();
			}

			session.setAttribute("accessCount", accessCount);

			String size = req.getParameter("size");
			String testString = new String();
			if (size!= null) {
				try {
					int sessionSize = Integer.parseInt(size);
					Integer sessionSizeInt = new Integer(sessionSize);
					/*
					 * Check to see if the objectmap already contains a String of this size.
					 */
					testString = (String)objectMap.get(sessionSizeInt);

					if (testString == null) {
						StringBuffer testStringBuffer = new StringBuffer();
						for (int i=0;i<sessionSize;i++) {
							testStringBuffer.append("x");
						}
						testString = testStringBuffer.toString();
						synchronized(objectMap) {
							objectMap.put(sessionSizeInt, testString);
						}
					}
					session.setAttribute("testString", testString);
				}
				catch(NumberFormatException e) {
				}
			}
			reqURI = req.getRequestURI();
			if(null != req.getQueryString()) {
				encReqURI = resp.encodeURL(reqURI + "?" + req.getQueryString());
			}
			else {
				encReqURI = resp.encodeURL(reqURI);
			}
			encReqURI = resp.encodeURL(encReqURI);
			out.println(ServletUtilities.headWithTitle(title) +
						"<BODY BGCOLOR=\"$FDF5E6\">\n" +
						"<H1 ALIGN=\"CENTER\">" + heading + "</H1>\n" +
						"<H2>Information on your session:</H2>\n" +
						"<TABLE BORDER=1 ALIGN=\"CENTER\">\n" +
						"<TR BGCOLOR=\"#FFAD00\">\n" +
						"  <TH>Info Type<TH>Value\n" +
						"<TR>\n" +
						"  <TD>ID\n" +
						"  <TD>" + session.getId() + "\n" +
						"<TR>\n" +
						"  <TD>Creation time\n" +
						"  <TD>" +
						new Date(session.getCreationTime()) + "\n" +
						"<TR>\n" +
						"  <TD>Time of Last Access\n" +
						"  <TD>" +
						new Date(session.getLastAccessedTime()) + "\n" +
						"<TR>\n" +
						"  <TD>Session Max Inactive Interval\n" +
						"  <TD>" +
						session.getMaxInactiveInterval() + "\n" +
						"<TR>\n" +
						"  <TD>Server\n" +
						"  <TD>" + serverName + "\n" +
						"<TR>\n" +
						"  <TD>Listener called\n" +
						"  <TD>" + bindingListenerCalled + "\n" +
						"<TR>\n" +
						"  <TD>Number of Previous Accesses\n" +
						"  <TD>" + accessCount + "\n" +
						"<TR>\n" +
						"  <TD>Request Parameter: Size\n" +
						"  <TD>" + req.getParameter("size") + "\n" +
						"<TR>\n" +
						"  <TD>Request URI\n" +
						"  <TD>" + req.getRequestURI() + "\n" +
						"<TR>\n" +
						"  <TD>Query String\n" +
						"  <TD>" + req.getQueryString()+ "\n" +
						"<TR>\n" +
						"  <TD>Session test string size\n" +
						"  <TD>" + testString.length() + "\n" +
						"</TABLE>\n" +
						"\n" +
						"<A href=\"" + reqURI + "\">Reached URI</A> : " + reqURI + "<br>\n" +
						"<A href=\"" + encReqURI + "\">Encoded URI</A> : " + encReqURI + "<br>\n" +
						"<A href=\"" + req.getRequestURI() + "?invalidate=true\">Invalidate Session</A>\n" +
						"</BODY></HTML>");

	}

	/**
	 * Method doPost
	 * @see jakarta.servlet.http.HttpServlet#doPost (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
			/*
			 * Perform the same action on both a Get and a Post.
			 */
			doGet(req,resp);
	}

}
