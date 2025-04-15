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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * <p>
 * @author whs
 */
public class WaiterPool
{
    private BlockingQueue<Waiter> fWaiters = new LinkedBlockingQueue<Waiter>();

    private boolean isEmpty = true;
    
    private boolean fClosed = false;
    
    public WaiterPool()
    {
        // nothing to do
    }
    
    /** Reveals whether servers were never added. 
     * @return the boolean answer*/
    public boolean getIsEmpty() {
    	return isEmpty;
    }
    
    /**
     * @return the waiter object
     * @throws InterruptedException
     */
    public Waiter getWaiter() throws InterruptedException
    {
		if (fClosed)
			throw new IllegalStateException("Closed!");
		return fWaiters.take();
	}
    
    public void returnWaiter( Waiter w ) throws InterruptedException
    {
    	if (fClosed)
			w.close();
		else
			fWaiters.put(w);
    }
    
    private static final Pattern fsServers = Pattern.compile("([^:,]+)(?::([^,]+))?(?:,)?");
    private static final Pattern fsLine = Pattern.compile("([^,]+),(.*)");
    
    /**
     * @param count the number of each server to add to the pool.
     * @param servers syntax: 
     * <pre>
     * list := endpoint [ "," list ]
     * endpoint := server [ ":" port ]
     * server := ;; ipaddress or name
     * port := ;; port as a number
     * </pre>
     * @param out
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     * @throws InterruptedException 
     */
    public void addServers(int count, String servers, PrintWriter out) throws UnsupportedEncodingException, IOException, InterruptedException
    {
        for (int i=0; i<count; i++)
        {
            Matcher m = fsServers.matcher(servers);
            while (m.find())
            {
                String server = m.group(1);
                String port = m.group(2);
                
                InetAddress ia = InetAddress.getByName(server);
                int p = -1;
                if (port != null)
                {
                    p = Integer.parseInt(port);
                }
                
                if (out != null)
                {
                    out.println(ia+":"+p);
                }
                Waiter w = new WaitClientWaiter(ia,p);
                fWaiters.put(w);
                isEmpty = false;
            }
        }
    }
    
    /**
     * @param line syntax: 
     * <pre>
     * line := count "," list
     * list := endpoint [ "," list ]
     * endpoint := server [ ":" port ]
     * server := ;; ipaddress or name
     * port := ;; port as a number
     * </pre>
     * @param out
     * @throws InterruptedException 
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    public void addServers( String line, PrintWriter out ) throws UnsupportedEncodingException, IOException, InterruptedException
    {
        Matcher m = fsLine.matcher(line);
        if (m.matches())
        {
            int count = Integer.parseInt(m.group(1));
            String servers = m.group(2);
            addServers(count, servers, out);
        } else
        	throw new IllegalArgumentException("Syntax error in target list '" + line +"'");
    }
    
    public void close() {
    	fClosed = true;
    	while (!fWaiters.isEmpty()) {
    		Waiter w;
    		try {
				w = fWaiters.take();
			} catch (InterruptedException e) {
				continue;
			}
    		w.close();
    	}
    }
}
