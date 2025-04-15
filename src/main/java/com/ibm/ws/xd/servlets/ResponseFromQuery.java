// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.xd.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class ResponseFromQuery extends HttpServlet {

	private final String qh = "responseCode=";

	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String query = request.getQueryString();
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		if (query != null && query.startsWith(qh)) {
			String sts = query.substring(qh.length());
			int st = Integer.parseInt(sts);
			String msg =
				"Returning requested status "
					+ st
					+ " (query='"
					+ query
					+ "').";
			if (400 <= st && st < 600) {
				response.sendError(st, msg);
			} else {
				response.setStatus(st);
				out.println(msg);
				out.flush();
			}
			System.out.println("ResponseFromQuery(" + query + ") parsed " + st);
		} else {
			out.println("Usage: ResponseFromQuery?responseCode=NNN");
			out.flush();
		}
	}

}
