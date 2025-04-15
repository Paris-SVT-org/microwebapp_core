// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

//
// Created Jul 27, 2007
//
package com.ibm.wsmm.services.waiter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 
 * <p>
 * @author whs
 */
public class WaitClientWaiter implements Waiter
{
    private InetAddress fAddr = null;
    private int fPort = WaitServer.fsDefaultPort;
    
    private Socket fSocket = null;
    
    private BufferedReader fIn = null;
    private PrintWriter fOut = null;
    
    public WaitClientWaiter( InetAddress addr ) throws UnsupportedEncodingException, IOException
    {
        this(addr,WaitServer.fsDefaultPort);
    }
    
    public WaitClientWaiter( InetAddress addr, int port ) throws UnsupportedEncodingException, IOException
    {
        fAddr = addr;
        fPort = port;
        if (fPort == -1)
        {
            fPort = WaitServer.fsDefaultPort;
        }
        reset();
    }
    
    public void reset() throws IOException
    {
        if (fSocket != null)
        {
            try
            {
                fSocket.close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
        
        fSocket = new Socket(fAddr,fPort);
        fIn = new BufferedReader( new InputStreamReader(fSocket.getInputStream(),"UTF8"));
        fOut = new PrintWriter( new BufferedWriter( new OutputStreamWriter(fSocket.getOutputStream(),"UTF8")));
    }

    /* (non-Javadoc)
     * @see com.ibm.ws.xd.workprofiler.profilecpu.ProfileCPU.Waiter#doWait(long)
     */
    public void doWait(long milliseconds)
    {
        doWait( Long.toString(milliseconds) );
    }
    
    /* (non-Javadoc)
     * @see com.ibm.ws.xd.workprofiler.profilecpu.ProfileCPU.Waiter#doWait(long)
     */
    public void doWait(String waittime)
    {
        fOut.println(waittime);
        fOut.flush();
        
        try
        {
            fIn.readLine();
        }
        catch (IOException e)
        {
            System.out.println("Oops on readline");
        }
    }

    public void close()
    {
        if (fIn != null)
        {
            try
            {
                fIn.close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
        if (fOut != null)
        {
            fOut.close();
        }
        try
        {
            fSocket.close();
        }
        catch (IOException e)
        {
            // ignore
        }
    }
    
    public static void xxx( WaitClientWaiter w, long ms )
    {
        long start = System.nanoTime();
        w.doWait(ms);
        long end = System.nanoTime();
        System.out.println("Trying to wait for "+ms+"ms, waited for "+(end-start)+"ns");

    }
    
    public static void xxx( WaitClientWaiter w, String wt )
    {
        long start = System.nanoTime();
        w.doWait(wt);
        long end = System.nanoTime();
        System.out.println("Trying to wait for "+wt+"ms, waited for "+(end-start)+"ns");

    }
    
    public static void main(String[] args)
    {
        try
        {
            InetAddress addr = null;
            if (args.length < 1)
            {
                addr = InetAddress.getLocalHost();
            }
            else
            {
                addr = InetAddress.getByName(args[0]);
            }
            WaitClientWaiter wcw = new WaitClientWaiter(addr);
            xxx(wcw,"10");
            xxx(wcw,"0");
            xxx(wcw,"-10");
            xxx(wcw,1);
            xxx(wcw,70);
            xxx(wcw,100);
        }
        catch (Exception e)
        {
            System.out.println("Uncaught exception");
            e.printStackTrace(System.out);
        }
        
    }
}
