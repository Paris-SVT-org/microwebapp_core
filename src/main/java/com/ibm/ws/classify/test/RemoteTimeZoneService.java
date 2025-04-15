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
public class RemoteTimeZoneService extends HttpServlet {
	
	
	public String getTimeZone() {
	  return java.util.Calendar.getInstance().getTimeZone().getDisplayName();
	}

	public String getTimeZoneForClient(String clientId) {
	  return java.util.Calendar.getInstance().getTimeZone().getDisplayName();
	}

	public String getClock() {
	  return java.util.Calendar.getInstance().getTime().toString();
	}
	
}
