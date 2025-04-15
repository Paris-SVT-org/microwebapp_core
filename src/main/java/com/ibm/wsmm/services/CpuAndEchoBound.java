// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.wsmm.services;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.ibm.wsmm.services.waiter.Waiter;
import com.ibm.wsmm.services.waiter.WaiterPool;

/**
 * @version 	1.0
 * @author
 */
public class CpuAndEchoBound extends HttpServlet 
{

    private static final long serialVersionUID = 1L;

    private MicroServices microServices = new MicroServices();

	private DecimalFormat df = new DecimalFormat("#,##0");

	private String hostName = "(unknown)";
	
	private static class MyLock {}
	
	private static Object myLock = new MyLock();
	
	private static String waitersSpec = null;
	
    private static WaiterPool fsPool = new WaiterPool();
    
	public CpuAndEchoBound() {
		try {
			InetAddress host = InetAddress.getLocalHost();
			hostName = host.getHostName();
		} catch (Exception ex) {
            // ignore it
		}
	}

	@Override
    public void doGet(
		HttpServletRequest req,
		HttpServletResponse resp)
		throws ServletException, IOException {
        
        String target = req.getParameter("target");
        if (target != null && target.length() != 0 )
        {
            resp.setContentType("text/html");
            ServletOutputStream out = resp.getOutputStream();
            out.println(
                "<HTML><HEAD><TITLE>CPU And Echo</TITLE></HEAD>");
            out.println("<BODY>");
            out.println("<H1><A href=\"index.html#CpuAndEchoBound\">CPU+Echo</A> Request/Result</H1>");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw );
            try
            {
                pw.println("<pre>");
                synchronized (myLock) {
					fsPool.addServers(target, pw);
				}
                pw.println("</pre>");
            }
            catch (Exception e)
            {
                pw.println("</pre>");
                pw.println("<p>Oops!</p><pre>");
                e.printStackTrace(pw);
                pw.println("</pre>");
            }
            pw.flush();
            String s = sw.toString();
            out.println(s);
            out.println("<H2>Details</H2>");
            out.println("Ran at " + new Date() + ", on " + hostName);

            // close Writer
            out.println("</BODY></HTML>");
            out.close();
            
            return;
        }
        
		long inTime = System.currentTimeMillis();
		boolean deterministic = false;
		int countMean = 30000;
		int countMax = 100000;
		int getInterval = countMean / 10;
		int yieldInterval = 1000;
        long waitLength = 5;
		boolean debConc = true;
		boolean zk = true;
		String wspec = null;
		MicroFailures micFail = new MicroFailures(req);

		if (micFail.failRequest(req, resp)) {
			return;
		}

		// initialize Writer
		resp.setContentType("text/html");
		ServletOutputStream out = resp.getOutputStream();
		out.println(
			"<HTML><HEAD><TITLE>CPU And Echo</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1><A href=\"index.html#CpuAndEchoBound\">CPU+Echo</A> Request/Result</H1>");
		out.println("<H2>Parameters</H2>");

		// get the parameter from the request
		out.print(
			"<BR> <A href=\"index.html#CpuAndEchoBound_deterministic\">deterministic</A>: ");
		try {
			deterministic = (req.getParameter( "deterministic")).equalsIgnoreCase( "true");
			out.println(deterministic ? "Yes" : "No");
		} catch (Exception e) {
			out.println(
				deterministic ? "Yes (Default)" : "No (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndEchoBound_countMean\">countMean</A>: ");
		try {
			countMean = Integer.parseInt( req.getParameter("countMean"));
			out.println(df.format(countMean));
			getInterval = countMean / 10;
		} catch (Exception e) {
			out.println(df.format(countMean) + " (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndEchoBound_countMax\">countMax</A>: ");
		try {
			countMax = Integer.parseInt( req.getParameter("countMax"));
			out.println(df.format(countMax));
		} catch (Exception e) {
			out.println(df.format(countMax) + " (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndEchoBound_waitInterval\">waitInterval</A>: ");
		try {
			getInterval = Integer.parseInt( req.getParameter("waitInterval"));
			out.println(df.format(getInterval));
		} catch (Exception e) {
			out.println(df.format(getInterval) + " (Default)");
		}
		out.print(
			"<BR> <A href=\"index.html#CpuAndEchoBound_yieldInterval\">yieldInterval</A>: ");
		try {
			yieldInterval = Integer.parseInt( req.getParameter("yieldInterval"));
			out.println(df.format(yieldInterval));
		} catch (Exception e) {
			out.println(df.format(yieldInterval) + " (Default)");
		}
		out.print("<BR> <A href=\"index.html#CpuAndEchoBound_waitLength\">waitLength</A>: ");
        try {
            waitLength = Long.parseLong( req.getParameter("waitLength") );
            out.println(df.format(waitLength));
        } catch (Exception e) {
            out.println(waitLength + " ms (Default)");
        }
		out.print(
			"<BR> <A href=\"index.html#CpuAndEchoBound_debConc\">debConc</A>: ");
		try {
			debConc = !(req.getParameter( "debConc")).equalsIgnoreCase( "false");
			out.println(debConc ? "Yes" : "No");
		} catch (Exception e) {
			out.println(
				debConc ? "Yes (Default)" : "No (Default)");
		}
		out.print("<BR> <A href=\"index.html#CpuAndEchoBound_zk\">zk</A>: ");
		try {
			zk =!( req.getParameter( "zk")).equalsIgnoreCase( "false");
			out.println(zk ? "Yes" : "No");
		} catch (Exception e) {
			out.println(zk ? "Yes (Default)" : "No (Default)");
		}
		final WaiterPool wpool;
		wspec = req.getParameter("allTargets");
		if (wspec != null && wspec.length() > 0) {
			out
					.println("<BR> <A href=\"index.html#CpuAndEchoBound_allTargets\">allTargets</A>="
							+ wspec);
			synchronized (myLock) {
				if (waitersSpec == null || !waitersSpec.equals(wspec)) {
					waitersSpec = wspec;
					fsPool.close();
					fsPool = new WaiterPool();
					PrintWriter pw = new PrintWriter(out);
					try {
						pw.println("<pre>");
						fsPool.addServers(wspec, pw);
						pw.println("</pre>");
					} catch (Exception e) {
						pw.println("</pre>");
						pw.println("<p>Oops!</p><pre>");
						e.printStackTrace(pw);
						pw.println("</pre>");
					}
					pw.flush();
				}
				wpool = fsPool;
			}
		} else
			wpool = fsPool;

		micFail.printFailureParameters(out);

        Waiter waiter = null;
        
        if (wpool.getIsEmpty()) {
			out.println("<H2>No servers defined!</H2>");
			out
					.println("No successful use of "
							+ "<A href=\"index.html#CpuAndEchoBound_target\">target</A> or "
							+ "<A href=\"index.html#CpuAndEchoBound_allTargets\">allTargets</A> "
							+ "parameters!");
		} else {
			// invoke Service
			try {
	            waiter = wpool.getWaiter();
				out.println("<H2>Result</H2>");
				String ans = microServices.CpuAndWait(deterministic, countMean,
						countMax, yieldInterval, getInterval, waiter,
						waitLength, debConc, zk, true);
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
	        finally
	        {
	            if (waiter != null)
	            {
	                try
	                {
	                    wpool.returnWaiter(waiter);
	                }
	                catch (InterruptedException e)
	                {
	                    // Ignore
	                }
	            }
	        }
        }

		out.println("<H2>Details</H2>");
		out.println("Ran at " + new Date() + ", on " + hostName);

		// close Writer
		out.println("</BODY></HTML>");
		out.close();
	}

	@Override
    public void doPost(
		HttpServletRequest req,
		HttpServletResponse resp)
		throws ServletException, IOException {
		doGet(req, resp);
	}

}
