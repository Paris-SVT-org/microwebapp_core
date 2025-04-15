// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.wsmm.services;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @version 	1.0
 * @author
 */
public class CpuAndGetBound extends HttpServlet {

	private MicroServices microServices = new MicroServices();

	private DecimalFormat df = new DecimalFormat("#,##0");

	private String hostName = "(unknown)";
	
	private static final String defaultTarget = "http://localhost:9080/"
		+ "A/CpuAndSleepBound?deterministic=true"
		+ "&countMax=10&sleepLength=10";

	public CpuAndGetBound() {
		try {
			InetAddress host = InetAddress.getLocalHost();
			hostName = host.getHostName();
		} catch (Exception ex) {
		}
	}

	public void doGet(
		HttpServletRequest req,
		HttpServletResponse resp)
		throws ServletException, IOException {
		long inTime = System.currentTimeMillis();
		boolean deterministic = false;
		long countMean = 30000;
		long countMax = 100000;
		long getInterval = countMean / 10;
		long yieldInterval = 1000;
		URL targetUrl = null;
		int readBufSize = 1024;
		boolean debConc = true;
		boolean zk = true;
		MicroFailures micFail = new MicroFailures(req);

		if (micFail.failRequest(req, resp)) {
			return;
		}

		// initialize Writer
		resp.setContentType("text/html");
		ServletOutputStream out = resp.getOutputStream();
		out.println(
			"<HTML><HEAD><TITLE>CPU And GET</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1><A href=\"index.html#CpuAndGetBound\">CPU+GET</A> Request/Result</H1>");
		out.println("<H2>Parameters</H2>");

		// get the parameter from the request
		out.print(
			"<BR> <A href=\"index.html#CpuAndGetBound_deterministic\">deterministic</A>: ");
		try {
			deterministic =
				(
					(String) req.getParameter(
						"deterministic")).equalsIgnoreCase(
					"true");
			out.println(deterministic ? "Yes" : "No");
		} catch (Exception e) {
			out.println(
				deterministic ? "Yes (Default)" : "No (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndGetBound_countMean\">countMean</A>: ");
		try {
			countMean =
				Long.parseLong(
					(String) req.getParameter("countMean"));
			out.println(df.format(countMean));
			getInterval = countMean / 10;
		} catch (Exception e) {
			out.println(df.format(countMean) + " ms (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndGetBound_countMax\">countMax</A>: ");
		try {
			countMax =
				Long.parseLong(
					(String) req.getParameter("countMax"));
			out.println(df.format(countMax));
		} catch (Exception e) {
			out.println(df.format(countMax) + " (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndGetBound_getInterval\">getInterval</A>: ");
		try {
			getInterval =
				Long.parseLong(
					(String) req.getParameter("getInterval"));
			out.println(df.format(getInterval));
		} catch (Exception e) {
			out.println(df.format(getInterval) + " (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndGetBound_yieldInterval\">yieldInterval</A>: ");
		try {
			yieldInterval =
				Long.parseLong(
					(String) req.getParameter("yieldInterval"));
			out.println(df.format(yieldInterval));
		} catch (Exception e) {
			out.println(df.format(yieldInterval) + " (Default)");
		}
		out.print("<BR> <A href=\"index.html#CpuAndGetBound_target\">target</A>: ");
		String targetStr = (String) req.getParameter("target");
		if (targetStr != null && targetStr.length() > 0)
			try {
				targetUrl = new URL(targetStr);
			} catch (MalformedURLException e) {
				out.print(MicroServices.htmlEncode("('" + targetStr
						+ "' is malformed: " + e.getMessage() + ") "));
			}
		if (targetUrl == null)
			targetUrl = new URL(defaultTarget);
		out.println(MicroServices.htmlEncode(targetUrl.toString()));
		out.print(
			"<BR> <A href=\"index.html#CpuAndGetBound_readBufSize\">readBufSize</A>: ");
		try {
			readBufSize =
				Integer.parseInt(
					(String) req.getParameter("readBufSize"));
			out.println(df.format(readBufSize) + " bytes");
		} catch (Exception e) {
			out.println(df.format(readBufSize) + " bytes (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndGetBound_debConc\">debConc</A>: ");
		try {
			debConc =
				!(
					(String) req.getParameter(
						"debConc")).equalsIgnoreCase(
					"false");
			out.println(debConc ? "Yes" : "No");
		} catch (Exception e) {
			out.println(
				debConc ? "Yes (Default)" : "No (Default)");
		}
		out.print("<BR> <A href=\"index.html#CpuAndGetBound_zk\">zk</A>: ");
		try {
			zk =
				!(
					(String) req.getParameter(
						"zk")).equalsIgnoreCase(
					"false");
			out.println(zk ? "Yes" : "No");
		} catch (Exception e) {
			out.println(zk ? "Yes (Default)" : "No (Default)");
		}

		micFail.printFailureParameters(out);

		// invoke Service
		try {
			out.println("<H2>Result</H2>");
			String ans = microServices.CpuAndGet(deterministic, countMean,
					countMax, yieldInterval, getInterval, targetUrl,
					readBufSize, debConc, zk, true);
			micFail.leakRequest();
			micFail.deadlockRequest();
			long outTime = System.currentTimeMillis();
			out.println("<P> " + ans);
			out.println("<P>Servlet run time = " + df.format(outTime - inTime)
					+ " ms");
		} catch (Exception e) {
			out.println("<H2> An Error occured</H2>");
			out.println("<P> " + e.getMessage());
		}

		out.println("<H2>Details</H2>");
		out.println("Ran at " + new Date() + ", on " + hostName);

		// close Writer
		out.println("</BODY></HTML>");
		out.close();
	}

	/**
	* @see jakarta.servlet.http.HttpServlet#void (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	*/
	public void doPost(
		HttpServletRequest req,
		HttpServletResponse resp)
		throws ServletException, IOException {
		doGet(req, resp);
	}

}
