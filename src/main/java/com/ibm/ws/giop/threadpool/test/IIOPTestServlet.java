//1.2, 9/12/05
//IBM Confidential OCO Source Material
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004 2005
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.

package com.ibm.ws.giop.threadpool.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;

public class IIOPTestServlet extends HttpServlet {

    public static IIOPTestServlet  instance    = null;
    public PrintWriter out             = null;
    public Random      rand            = new Random();
    public int         loopCount       = 50;
    public boolean     interactiveMode = false;

    public boolean     getServletName      = false;
    public boolean     startStress         = false;
    public int         entryCount          = 3000;
    public Exception   exception           = null;

    public String       servletName         = null;
    public String       html                = null;
    public HttpServletRequest request;
    public HttpServletResponse response;

    public HttpServletRequest  req = null;
    public HttpServletResponse resp = null;
    public String ids = " ";

    public IIOPTestServlet() {
        if ( instance == null ) {
            instance = this;
        }
    }

    public static IIOPTestServlet getInstance() {
        return instance;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean error = false;

        final String methodName = "IIOPTestServlet.service()";
        System.out.println( methodName+" "+request.getQueryString() );

        this.request = request;
        this.response= response;

        if ( !error ) {
            super.service( request, response );
        }
    }

    private void sendException(HttpServletRequest request, HttpServletResponse response, Throwable e ) throws IOException
    {
        out = response.getWriter();
        if ( interactiveMode ) {
            out.println( "<br>" );
            out.println( getStackTrace( e ) );
            out.println( "<br>" );
        } else {
            out.print( "\n\n\nIIOPTestServlet exception\n\n\n"+ getStackTrace( e )+"\n\n\n");
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        req = request;
        resp = response;
        ids = request.getParameter("ids");

        //---------------------------------------------
        // Check for required parms
        //---------------------------------------------
        String method = request.getParameter("method");
        if ( method == null ) {
            method="defaultAction";
        }
        //---------------------------------------------

        interactiveMode     = request.getParameter("quietMode")==null?true:false;

        response.setContentType("text/html");

        if ( interactiveMode ) out = response.getWriter();

        //---------------------------------------------
        // Ping request?
        //---------------------------------------------
        if ( method.equalsIgnoreCase("ping") ) {
            if ( !interactiveMode ) out = response.getWriter();
            out.print( "pong" );
            return;
        }
        //---------------------------------------------

        //---------------------------------------------
        // Check for parm sets
        //---------------------------------------------
        String tmp = request.getParameter("loopCount");
        if ( null != tmp ) {
            loopCount = new Integer( tmp ).intValue();
        }
        //---------------------------------------------

        try {
            //---------------------------------------------
            // 
            //---------------------------------------------
            if ( interactiveMode ) {
                getServletContext().getRequestDispatcher("/IIOPTestServletWorkArea.jsp").include(request,response);
            }
            //---------------------------------------------

            //---------------------------------------------
            // Execute the request
            //---------------------------------------------
            Class c = this.getClass();
            Method m = c.getMethod( method, (Class)null );
            m.invoke( this, (Object)null );
            //this.getClass().getMethod( method, null ).invoke( this, null );
            
            //---------------------------------------------

            if ( interactiveMode ) {
                out.println( "</body></html>" );
            }

            if ( !interactiveMode ) out = response.getWriter();

            if ( html==null ) {
                out.print( "done" );
            } else {
                html = null;
            }

        } catch ( Exception e ) {
            if ( interactiveMode ) {
                out.println( "<br><PRE><b>" );
                out.println( getStackTrace( e ) );
                out.println( "</b></PRE><br>" );
                out.println( "</body></html>" );
            } else {
                out = response.getWriter();
                out.print( "\n\n\nIIOPTestServlet exception\n\n\n"+ getStackTrace( e )+"\n\n\n");
            }
        }
    }

    public void ping() throws Exception {
        out.println( "pong" );
    }

    public void defaultAction() {
    }

    static String getStackTrace(Throwable e) {
        if ( null == e ) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter( sw );
        e.printStackTrace( pw );
        return sw.toString();
    }

}
//-------------------------------------------------------------------------------------



