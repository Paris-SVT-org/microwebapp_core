package com.ibm.wsmm.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.ibm.ws.xd.test.CpuLoad;

public class CpuLoadServlet extends HttpServlet {

	private boolean dobuf = false;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		int npar = 4;
		int kCount = 200;
		float rate = 5;
		int sms = 50;
		float maxMins = 2.1f;

		// initialize Writer
		resp.setContentType("text/plain");
		PrintWriter outwr = resp.getWriter();
		StringWriter swr = new StringWriter();
		PrintWriter bufwr = new PrintWriter(swr, false);

		// get the parameters from the request
		try {
			npar = Integer.parseInt(req.getParameter("concurrency"));
		} catch (Exception e) {
		}
		try {
			kCount = Integer.parseInt(req.getParameter("kCount"));
		} catch (Exception e) {
		}
		try {
			rate = Float.parseFloat(req.getParameter("rate"));
		} catch (Exception e) {
		}
		try {
			sms = Integer.parseInt(req.getParameter("sleepMillis"));
		} catch (Exception e) {
		}
		try {
			maxMins = Float.parseFloat(req.getParameter("maxMins"));
		} catch (Exception e) {
		}

		String hostName = "(unknown)";
		try {
			InetAddress host = InetAddress.getLocalHost();
			hostName = host.getHostName();
		} catch (Exception ex) {
		}
		PrintWriter thewr = dobuf ? bufwr : outwr;
		thewr.println("Starting at " + new Date() + ", on " + hostName);
		thewr.println("Going at rate=" + rate + "/s, kCount=" + kCount
				+ ", sleepMillis=" + sms + ", concurrency=" + npar
				+ ", maxMins=" + maxMins);
		thewr.println();
		thewr.flush();
		CpuLoad cpuLoad = new CpuLoad();
		try {
			cpuLoad.go(thewr, npar, kCount * 1000, rate / (npar * 1000), sms,
					maxMins);
		} catch (Throwable e) {
			e.printStackTrace(thewr);
		}
		thewr.flush();
		if (dobuf)
			outwr.write(swr.toString());
		outwr.flush();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

}
