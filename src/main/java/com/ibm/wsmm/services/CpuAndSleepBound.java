// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.wsmm.services;
import java.io.IOException;
import java.net.InetAddress;
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
public class CpuAndSleepBound extends HttpServlet {

	private MicroServices microServices = new MicroServices();

	private DecimalFormat df = new DecimalFormat("#,##0");

	private String hostName = "(unknown)";

	public CpuAndSleepBound() {
		try {
			InetAddress host = InetAddress.getLocalHost();
			hostName = host.getHostName();
		} catch (Exception ex) {
		}
	}

	/**
	* @see jakarta.servlet.http.HttpServlet#void (jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	*/
	public void doGet(
		HttpServletRequest req,
		HttpServletResponse resp)
		throws ServletException, IOException {
		long inTime = System.currentTimeMillis();
		boolean deterministic = false;
		long countMean = 30000;
		long countMax = 100000;
		long sleepInterval = countMean / 10;
		long yieldInterval = 1000;
		int sleepLength = 1;
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
			"<HTML><HEAD><TITLE>CPU And Sleep</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1><A href=\"index.html#CpuAndSleepBound\">CPU+Sleep</A> Request/Result</H1>");
		out.println("<H2>Parameters</H2>");

		// get the parameter from the request
		out.print(
			"<BR> <A href=\"index.html#CpuAndSleepBound_deterministic\">deterministic</A>: ");
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
			"<BR> <A href=\"index.html#CpuAndSleepBound_countMean\">countMean</A>: ");
		try {
			countMean =
				Long.parseLong(
					(String) req.getParameter("countMean"));
			out.println(df.format(countMean));
			sleepInterval = countMean / 10;
		} catch (Exception e) {
			out.println(df.format(countMean) + " ms (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndSleepBound_countMax\">countMax</A>: ");
		try {
			countMax =
				Long.parseLong(
					(String) req.getParameter("countMax"));
			out.println(df.format(countMax));
		} catch (Exception e) {
			out.println(df.format(countMax) + " (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndSleepBound_sleepInterval\">sleepInterval</A>: ");
		try {
			sleepInterval =
				Long.parseLong(
					(String) req.getParameter("sleepInterval"));
			out.println(df.format(sleepInterval));
		} catch (Exception e) {
			out.println(df.format(sleepInterval) + " (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndSleepBound_yieldInterval\">yieldInterval</A>: ");
		try {
			yieldInterval =
				Long.parseLong(
					(String) req.getParameter("yieldInterval"));
			out.println(df.format(yieldInterval));
		} catch (Exception e) {
			out.println(df.format(yieldInterval) + " (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndSleepBound_sleepLength\">sleepLength</A>: ");
		try {
			sleepLength =
				Integer.parseInt(
					(String) req.getParameter("sleepLength"));
			out.println(df.format(sleepLength) + " ms");
		} catch (Exception e) {
			out.println(df.format(sleepLength) + " ms (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndSleepBound_debConc\">debConc</A>: ");
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
		out.print("<BR> <A href=\"index.html#CpuAndSleepBound_zk\">zk</A>: ");
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
			boolean ans =
				microServices.CpuAndSleep(
					deterministic,
					countMean,
					countMax,
					yieldInterval,
					sleepInterval,
					sleepLength,
					debConc,
					zk);

			micFail.leakRequest(); 
			micFail.deadlockRequest();

			long outTime = System.currentTimeMillis();
			out.println("<P> " + ans);
			out.println(
				"<P>Servlet run time = "
					+ df.format(outTime - inTime)
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
