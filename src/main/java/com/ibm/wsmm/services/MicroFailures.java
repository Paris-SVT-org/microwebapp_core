// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.wsmm.services;
import java.io.*;
import java.util.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** One instance of this is created per request,
 * to process the parameters related to failures
 * and HTTP sessions.
 */
public class MicroFailures
{

	/*
	 * The following provides three different failure behaviors:
	 * 1. deadlock - puts a thread to sleep 
	 * 2. memory leak - allocates memory that is stashed 
	 * 3. fail request - generates an internal server failure
	 * 
	 * These behaviors may be generated randomly, with a probability
	 * (expressed as an integer between 0-100) for each supplied on
	 * the request.  In addition, the request failure may also be
	 * generated persistently across requests.  
	 */
	private static Random generator = new Random();
	private static Vector mem = new Vector(1,1);
	private static int highWaterMark = 0;
		
	private static final int DEFAULT_DEADLOCK_PROBABILITY = 0;
	private static final int DEFAULT_DEAD_TIME = 10*60;
	private static final int DEFAULT_FAILURE_PROBABILITY = 0;
	private static final int DEFAULT_LEAK_PROBABILITY = 0;
	private static final int DEFAULT_LEAK_AMOUNT = 0;
	private static final boolean DEFAULT_FAILURE_SETTING = false;
	private static final int DEFAULT_SESS_CHUNKS = 0;
	private static final int DEFAULT_SESS_CHUNKSIZE = 0;
	
	private int deadlockProbability = DEFAULT_DEADLOCK_PROBABILITY;
	private int failureProbability = DEFAULT_FAILURE_PROBABILITY;
	private int leakProbability = DEFAULT_LEAK_PROBABILITY;
	private int leakAmount = DEFAULT_LEAK_AMOUNT;
	private int deadTime = 0; //seconds for this request
	
	private static boolean persistentFailure = DEFAULT_FAILURE_SETTING;
	private static int deadTimeAll = 0; //seconds
	
	private boolean setFail = DEFAULT_FAILURE_SETTING;
	private boolean clearFail = DEFAULT_FAILURE_SETTING;
	private String syntaxFail = null;
	private int newDeadTimeAll = -1;
	
	private String user = null;
	private boolean login = false, logout = false;
	private HttpSession hsess = null;
	private int sessChunks = DEFAULT_SESS_CHUNKS, sessChunkSize = DEFAULT_SESS_CHUNKSIZE;

	public MicroFailures(HttpServletRequest req) {
	    login = "true".equalsIgnoreCase(req.getParameter("login"));
	    logout = "true".equalsIgnoreCase(req.getParameter("logout"));
	    user = req.getParameter("user");
	    hsess = req.getSession(login);
		try { 
			sessChunks=Integer.parseInt((String)req.getParameter("sessChunks"));
		} catch(Exception e){
			sessChunks = DEFAULT_SESS_CHUNKS;
		}
		try { 
			sessChunkSize=Integer.parseInt((String)req.getParameter("sessChunkSize"));
		} catch(Exception e){
			sessChunkSize = DEFAULT_SESS_CHUNKSIZE;
		}
		try { 
			deadlockProbability=Integer.parseInt((String)req.getParameter("deadp"));
		} catch(Exception e){
			deadlockProbability = DEFAULT_DEADLOCK_PROBABILITY;
		}
		try { 
			deadTime =Integer.parseInt((String)req.getParameter("deadTime"));
		} catch(Exception e){
			deadTime = DEFAULT_DEAD_TIME;
		}
		try { 
			failureProbability=Integer.parseInt((String)req.getParameter("failp"));
		} catch(Exception e){
			failureProbability = DEFAULT_FAILURE_PROBABILITY;
		}
		try { 
			leakProbability=Integer.parseInt((String)req.getParameter("leakp"));
		} catch(Exception e){
			leakProbability = DEFAULT_LEAK_PROBABILITY;
		}
		try { 
			leakAmount=Integer.parseInt((String)req.getParameter("amount"));
		} catch(Exception e){
			leakAmount = DEFAULT_LEAK_AMOUNT;
		}
		String failAllS = req.getParameter("failAll");
		if ("true".equalsIgnoreCase(failAllS))
            setFail = true;
        else if ("false".equalsIgnoreCase(failAllS))
            clearFail = true;
        else {
            if (failAllS != null && failAllS.length() > 0)
                syntaxFail = failAllS;
            if ("true".equalsIgnoreCase(req.getParameter("setfail")))
                setFail = true;
            else if ("false".equalsIgnoreCase(req.getParameter("clearfail")))
                clearFail = true;
        }
		String deadTimeAllS = req.getParameter("deadTimeAll");
		if (deadTimeAllS != null && deadTimeAllS.length() > 0)
			newDeadTimeAll = Integer.parseInt(deadTimeAllS);
	}
	
	private final static String deadpL =     "<A href=\"index.html#deadp\"      >deadp</A>";
	private final static String loginL =     "<A href=\"index.html#login\"      >login</A>";
	private final static String logoutL =    "<A href=\"index.html#logout\"     >logout</A>";
	private final static String userL =      "<A href=\"index.html#user\"       >user</A>";
	private final static String deadTimeL =  "<A href=\"index.html#deadTime\"   >deadTime</A>";
	private final static String failpL =     "<A href=\"index.html#failp\"      >failp</A>";
	private final static String leakpL =     "<A href=\"index.html#leakp\"      >leakp</A>";
	private final static String amountL =    "<A href=\"index.html#amount\"     >amount</A>";
	private final static String failAllL =   "<A href=\"index.html#failAll\"    >failAll</A>";
	private final static String deadTimeAllL="<A href=\"index.html#deadTimeAll\">deadTimeAll</A>";

	/** Prints the parameter values and state related to
	 * failures and HTTP sessions, and updates the
	 * HTTP session state (except that session creation
	 * is done earlier, in the constructor).
	 * @param out the ServletOutputStream into which to write
	 * @throws IOException
	 */
	public void printFailureParameters(ServletOutputStream out) throws IOException {
		String nl = "<BR>";
		out.println(nl+deadpL+   "		   : "+String.valueOf(deadlockProbability)+ " %");
		out.println(nl+deadTimeL+"		   : "+String.valueOf(deadTime)+ " seconds");
		out.println(nl+failpL+   "		   : "+String.valueOf(failureProbability)+ " %");
		out.println(nl+leakpL+   "		   : "+String.valueOf(leakProbability)+ " %");
		out.println(nl+amountL+  "		   : "+String.valueOf(leakAmount)+" bytes");
		if (setFail || clearFail)
		    out.println(nl+failAllL+"       : "+String.valueOf(setFail));
		else if (syntaxFail != null)
		    out.println(nl+"<STRONG>Syntax error in "+failAllL+"!</STRONG>");
		else
		    out.println(nl+failAllL+" not specified");
		if (newDeadTimeAll >= 0)
		    out.println(nl+deadTimeAllL+"       := "+String.valueOf(newDeadTimeAll) + " seconds");
		else
		    out.println(nl+deadTimeAllL+"       unchanged, ="+String.valueOf(deadTimeAll) + " seconds");
		out.println(nl+"<EM>persistentFailure=" + persistentFailure + "</EM>");
	    out.println(nl+loginL+ "       : "+String.valueOf(login));
	    out.println(nl+logoutL+"       : "+String.valueOf(logout));
	    out.println(nl+userL+  "       : "+user);
		if (hsess == null)
		    out.println(nl + "<EM>No HTTP session</EM>");
		else {
		    Object oldUser = hsess.getAttribute("user");
		    Integer oldCount = (Integer) hsess.getAttribute("session.reqCount");
		    int newCount = (oldCount != null) ? oldCount.intValue() + 1 : 1;
		    out.println(nl + "Session created at " + new Date(hsess.getCreationTime()));
		    out.println(nl + "Session last accessed at " + new Date(hsess.getLastAccessedTime()));
		    out.println(nl + "Session is " + (hsess.isNew()?"":" not ") + "new");
		    out.println(nl + "Session request count = " + newCount);
		    hsess.setAttribute("session.reqCount", new Integer(newCount));
		    out.println(nl + "User was " + String.valueOf(oldUser));
		    out.println(nl + "Session memory: " + sessChunks + " chunks, each of " + sessChunkSize + " ints");
		    int[][] blocks = (int[][]) hsess.getAttribute("Memory");
		    boolean nomem = sessChunks<=0 || sessChunkSize <= 0;
		    if (nomem ? (blocks != null) : (blocks == null
					|| blocks.length != sessChunks
					|| blocks[0].length != sessChunkSize)) {
		    	try {
					blocks = nomem ? null : MicroServices.memAlloc(sessChunkSize, sessChunks);
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					pw.flush();
					out.println(nl + "Exception during allocation: " + sw.toString() + nl);
				}
		    	hsess.setAttribute("Memory", blocks);
		    }
		    if (login && user != null) {
                hsess.setAttribute("user", user);
            }
		    if (logout)
		        hsess.invalidate();
		}
	}
	
	/*
     * leakRequest simulates a memory leak based on the leakp and amount
     * parameters to the request. The default behavior is no leak.
     */
	public void leakRequest() {
		leakRequest(mem);
	}
 
 	public void leakRequest(Vector mem) {
		boolean doLeak = roll(leakProbability);
		
		if (doLeak && leakAmount > 0) {
			StringBuffer buf = new StringBuffer(leakAmount);
			for (int i = 0; i < leakAmount; i++) {
				if (i < Character.MIN_VALUE)
					buf.append(Character.MIN_VALUE);
				else if (i > Character.MAX_VALUE)
					buf.append(Character.MAX_VALUE);
				else 
					buf.append((char) i);
			}
			mem.add(buf);
			highWaterMark += leakAmount;
			System.out.println("Leaking " + leakAmount + 
								" bytes, total " + mem.size() + 
								" leaks, " + highWaterMark + " bytes");
		}
	}
  	
   	/*
   	 * Determines if a request should be failed based on the 
   	 * failp, setfail, and clearfail parameters, or
   	 * on the desired HTTP session behavior; fails the request
   	 * if appropriate. The default behavior is no failure.
   	 */
	public boolean failRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
		if (user != null) {
            Object oldUser = (hsess!=null)?hsess.getAttribute("user"):null;
            if (login && oldUser != null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Requested login but session already had user="
                                        + oldUser);
                return true;
            }
            if ((!login) && !user.equals(oldUser)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Requested user " + user + " but session user was "
                                + oldUser);
                return true;
            }
        }
		boolean doFail = false;
		
		if (setFail)
			persistentFailure = true;
		if (clearFail)
			persistentFailure = false;
		
		if (persistentFailure)
			doFail = true;
		else 
			doFail = roll(failureProbability);
   		
		if (doFail) 
			System.out.println("Failing request");

		if (doFail) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return true;
        }
		return false;
	}
	
   	/*
   	 * deadlockRequest simulates a deadlock with a 10 minute sleep 
   	 * based on the deadp parameter.  The default behavior is no deadlock.
   	 */
	public void deadlockRequest() {
		boolean doLock;
		int doDeadSecs;
		if (newDeadTimeAll == 0)
			deadTimeAll = 0;
		if (deadTimeAll > 0) {
			doLock = true;
			doDeadSecs = deadTimeAll;
		} else {
			doLock = roll(deadlockProbability);
			doDeadSecs = deadTime;
		}
		if (doLock) {
			System.out.println(
				"Hanging request for "
					+ doDeadSecs
					+ " seconds" + MicroServices.getId() + ".");
			MicroServices mics = new MicroServices();
			try {
				mics.sleepBound(doDeadSecs * 1000, true, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Done hanging" + MicroServices.getId() + ".");
		}
		if (newDeadTimeAll > 0)
			deadTimeAll = newDeadTimeAll;
	}
   	
	private boolean roll(int prob) {
		boolean youLose = false;
		int randomValue = generator.nextInt(100);
		if (prob > randomValue)
			youLose = true;
    	
		return youLose;
	}

}

