//IBM Confidential OCO Source Material
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.

package com.ibm.ws.xd.test.runfirst;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * The Class InvocationProxy.
 * 
 * @author brianm
 * 
 *         This class helps you invoke MBeans by translating the calls to a
 *         standard interface
 */
public class InvocationProxy {

    /** The Constant thisClassName. */
    private final static String thisClassName = "InvocationProxy";

    /**
     * Gets the proxy.
     * 
     * @param proxyInterface
     *            the proxy interface Class<?>[] interfaces
     * @param proxyObject
     *            the proxy object
     * 
     * @return the proxy
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    static public Object getProxy(Class<?>[] proxyInterface, Object proxyObject) {
        final String methodName = thisClassName + ".getProxy()";
        Object proxy = null;
        proxy = Proxy.newProxyInstance(proxyInterface[0].getClassLoader(), proxyInterface, new ObjectInvocationHandler(proxyObject));
        return proxy;
    }

    /**
     * The Class ObjectInvocationHandler.
     */
    static class ObjectInvocationHandler implements InvocationHandler {

        /** The this class name. */
        final String thisClassName = "InvocationProxy.ObjectInvocationHandler";

        /** The proxy object. */
        Object proxyObject;

        /** The clazz. */
        Class clazz;

        /**
         * Instantiates a new object invocation handler.
         * 
         * @param proxyObject
         *            the proxy object
         */
        ObjectInvocationHandler(Object proxyObject) {
            final String methodName = thisClassName + " - CTOR";

            this.proxyObject = proxyObject;
            this.clazz = proxyObject.getClass();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         * java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object o, Method m, Object args[]) throws Throwable {
            final String methodName = thisClassName + ".invoke() object=" + o.getClass().getSimpleName() + " method=" + m.getName();
            Object object;

            Method method = clazz.getMethod(m.getName(), m.getParameterTypes());
            object = method.invoke(proxyObject, args);

            return object;
        }
    }
}
