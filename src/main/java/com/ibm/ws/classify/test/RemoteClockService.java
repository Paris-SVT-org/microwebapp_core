/*
 * Created on Apr 27, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.ibm.ws.classify.test;

import jakarta.servlet.http.HttpServlet;
/**
 * @author priyanka
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RemoteClockService extends HttpServlet{
	
	public String getServerClock() {
	  return java.util.Calendar.getInstance().getTime().toString();
	}
	
	public String getServerTimeZone() {
	  return java.util.Calendar.getInstance().getTimeZone().getDisplayName();
	}

}
