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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * <p>
 * @author whs
 */
public class WaitServer extends Thread
{
    Socket fSocket = null;
    
    public WaitServer( Socket s )
    {
        fSocket = s;
    }
    
    @Override
    public void run()
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = fSocket.getInputStream();
            out = fSocket.getOutputStream();
            
            BufferedReader br = new BufferedReader( new InputStreamReader(in,"UTF8"));
            PrintWriter pw = new PrintWriter( new BufferedWriter( new OutputStreamWriter(out,"UTF8")));
            while (true)
            {
                String line = br.readLine();
                if (line == null)
                {
                    break;
                }
                long ms = 0;
                try
                {
                    ms = Long.parseLong(line);
                }
                catch (NumberFormatException e2)
                {
                    // ignore this
                }
                boolean useSleep = ms >= 0;
                if (useSleep)
                {
                    long start = System.nanoTime();
                    try
                    {
                        Thread.sleep(ms);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    long end = System.nanoTime();
                    pw.println("OK "+(end-start));
                    pw.flush();
                }
                else
                {
                    pw.println("OK -1");
                    pw.flush();
                }
            }
        }
        catch (SocketException e)
        {
            // ignore it
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
            try
            {
                fSocket.close();
            }
            catch (IOException e)
            {
                // ignore
            }
            
            synchronized (fsConnections)
            {
                fsConnections.remove(this);
            }
        }
    }
    
    private void close()
    {
        try
        {
            fSocket.close();
        }
        catch (IOException e)
        {
            // Ignore
        }
    }

    private static boolean fsClosing = false;
    private static ServerSocket fsServerSocket = null;
    private static List<WaitServer> fsConnections = new ArrayList<WaitServer>();
    
    public final static int fsDefaultPort = 5867;
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        fsClosing = false;
        int port = fsDefaultPort;
        int backlog = 20;
        try
        {
        	System.out.println("Start WaitServer listening on port " + fsDefaultPort);
            fsServerSocket = new ServerSocket(port,backlog);
            while (true)
            {
                Socket s = fsServerSocket.accept();
                WaitServer ws = new WaitServer(s);
                synchronized (fsConnections)
                {
                    fsConnections.add(ws);
                }
                ws.start();
            }
        }
        catch (Exception e)
        {
            if (!fsClosing)
            {
                System.out.println("Uncaught exception");
                e.printStackTrace(System.out);
                if (fsServerSocket != null)
                {
                    try
                    {
                        fsServerSocket.close();
                    }
                    catch (IOException e1)
                    {
                        // ignore
                    }
                }
            }
        }
    }

    public static void shutdown()
    {
        fsClosing = true;
        try
        {
            fsServerSocket.close();
            
        }
        catch (IOException e)
        {
            System.out.println("Uncaught exception");
            e.printStackTrace(System.out);
        }
        fsServerSocket = null;
        synchronized (fsConnections)
        {
            for (WaitServer ws : fsConnections)
            {
                ws.close();
            }
            fsConnections.clear();
        }
    }
}
