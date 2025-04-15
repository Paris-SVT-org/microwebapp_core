// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

//
// Created Aug 10, 2007
//
package com.ibm.wsmm.services.waiter;

import java.io.IOException;

/**
 * 
 * <p>
 * @author whs
 */
public interface Waiter
{
    public void reset() throws IOException;
    public void doWait( long milliseconds );
    /**
     * do a wait
     * @param waitTime  Syntax: delay ["+"|"-"]
     * The "+" indicates sleep is to be used (default).
     * A "-" indicates no sleep, delay is ignored.
     */
    public void doWait( String waitTime );
    public void close();
}
