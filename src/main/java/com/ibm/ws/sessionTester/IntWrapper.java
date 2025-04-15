/*
 * Created on Sep 19, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.ibm.ws.sessionTester;

import java.io.Serializable;

import java.util.HashSet;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

/**
 * This class serves as a wrapper for an int.
 * This class is used for testing that needs to check the system
 * behaviour when an application classloader only class is placed
 * in the session.
 */
public class IntWrapper implements Serializable, HttpSessionBindingListener {
	
	/**
	 * Object Variables.
	 */
	private int _i = 0;


	/**
	 * Class constructor.
	 */
	public IntWrapper(int i) {
		_i = i;
	}
	
	/**
	 * Method intValue
	 */
	public int intValue() {
		return _i;
	}

	/**
	 * Method incrementValue
	 */
	public void incrementValue() {
		_i = _i+1;		
	}
	
	/**
	 * Method toString
	 */
	public String toString() {
		return "("+_i+")";		
	}

	/**
	 * Method valueBound
	 * @see jakarta.servlet.http.HttpSessionBindingListener#valueBound(jakarta.servlet.http.HttpSessionBindingEvent)
	 */
	public void valueBound(HttpSessionBindingEvent event) {
		// System.out.println("IntWrapper.valueBound called with event:" + event);
		
	}

	/**
	 * Method valueUnbound
	 * @see jakarta.servlet.http.HttpSessionBindingListener#valueUnbound(jakarta.servlet.http.HttpSessionBindingEvent)
	 */
	public void valueUnbound(HttpSessionBindingEvent event) {
		System.out.println("IntWrapper.valueUnBound called with event:" + event);
		SessionTester.bindingListenerCalled = true;		
	}

}
