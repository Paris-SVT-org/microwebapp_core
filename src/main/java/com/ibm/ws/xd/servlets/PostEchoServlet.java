// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.xd.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class PostEchoServlet extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		InputStream in = request.getInputStream();
		OutputStream out = response.getOutputStream();
		byte[] buf = new byte[1024];
		for (;;) {
			int len = in.read(buf);
			if (len <= 0) break;
			out.write(buf,0,len);
		}
	}	

}

