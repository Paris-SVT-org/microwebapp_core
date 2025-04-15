package com.ibm.wsmm.services;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GC extends HttpServlet {

	private String hostName = "(unknown)";

	public GC() {
		try {
			InetAddress host = InetAddress.getLocalHost();
			hostName = host.getHostName();
		} catch (Exception ex) {
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		long before = System.currentTimeMillis();
		System.gc();
		long after = System.currentTimeMillis();
		resp.setContentType("text/html");
		ServletOutputStream out = resp.getOutputStream();
		out.println("<HTML><HEAD><TITLE>mem Bound</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("Started at " + new Date(before) + ", on " + hostName
				+ "; GC took " + (after - before) + " ms.");
		// close Writer
		out.println("</BODY></HTML>");
		out.close();
	}
}
