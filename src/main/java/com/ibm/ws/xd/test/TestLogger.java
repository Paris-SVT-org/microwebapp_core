//IBM Confidential OCO Source Material
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2006
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.
package com.ibm.ws.xd.test;

/**
 * @author gmontero
 *
 * Given the particulars of the XD auto FVT enviornment, this 
 * utility class will make sure messages show up in both the 
 * screen where the wsbld runtest is executed, as well as the 
 * output captured and saved.
 */
public class TestLogger extends Thread {
    static java.util.Stack _stack = new java.util.Stack();
    static Object _wait = new Object();
    
    private static final TestLogger _logger = new TestLogger();
    
    static {
    	_logger.start();
    }
 
    public static void logMsg(Object msg) {
        // this print will put msg in auto fvt capture
        System.out.println(msg);
        _stack.push(msg);
        synchronized(_wait) {
            _wait.notify();
        }
    }
 
 
    public void run() {
        while(true) {
            Object msg = null;
            if (_stack.size() > 0) 
                msg = _stack.pop();
            if (msg != null) {
                // this print will show up in the window running the wsbld cmd
                System.out.println(msg);
            } else {
                synchronized(_wait) {
                    try {
                        _wait.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
