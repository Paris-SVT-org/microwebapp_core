/*COPYRIGHT_START***********************************************************
*
* IBM Confidential OCO Source Material
* 5724-I63, 5724-H88 (C) COPYRIGHT International Business Machines Corp. 1997, 2004
* The source code for this program is not published or otherwise divested
* of its trade secrets, irrespective of what has been deposited with the
* U.S. Copyright Office.
*
*   IBM DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING
*   ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
*   PURPOSE. IN NO EVENT SHALL IBM BE LIABLE FOR ANY SPECIAL, INDIRECT OR
*   CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF
*   USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
*   OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE
*   OR PERFORMANCE OF THIS SOFTWARE.
*
* @(#)file   MyAttributeListener.java
* @(#)author  Aditya Desai
*
*
*COPYRIGHT_END*************************************************************/
package com.ibm.ws.sessionTester.listeners;

import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;

/**
 * @author aditya
 *
 */
public class MyAttributeListener implements HttpSessionAttributeListener {
	
	//----------------------------------------
	// Private Variables
	//----------------------------------------
	/*
	 * A counter that is used to identify the instance 
	 * id of this attributeListener instance.
	 */
	private static int counter = 1;
	private static Object syncObject = new Object();
	
	/*
	 * My instance id
	 */
	private int _myInstanceID;
	
	//----------------------------------------
	// Class Constructor
	//----------------------------------------
	public MyAttributeListener() {
		super();
		synchronized(syncObject) {
			_myInstanceID = counter;
			counter ++;
		}
	}

	//----------------------------------------
	// Public Methods
	//----------------------------------------

	/**
	 * Method attributeAdded
	 * @see jakarta.servlet.http.HttpSessionAttributeListener#attributeAdded(jakarta.servlet.http.HttpSessionBindingEvent)
	 */
	public void attributeAdded(HttpSessionBindingEvent event) {
		System.out.println("AttrListener #" + _myInstanceID + " received attrAdded event for session " + event.getSession().getId() + ". name="+event.getName());

	}

	/** 
	 * Method attributeRemoved
	 * @see jakarta.servlet.http.HttpSessionAttributeListener#attributeRemoved(jakarta.servlet.http.HttpSessionBindingEvent)
	 */
	public void attributeRemoved(HttpSessionBindingEvent event) {
		System.out.println("AttrListener #" + _myInstanceID + " received attrRemoved event for session " + event.getSession().getId() + ". name="+event.getName());
	}

	/**
	 * Method attributeReplaced
	 * @see jakarta.servlet.http.HttpSessionAttributeListener#attributeReplaced(jakarta.servlet.http.HttpSessionBindingEvent)
	 */
	public void attributeReplaced(HttpSessionBindingEvent event) {
		System.out.println("AttrListener #" + _myInstanceID + " received attrReplaced event for session " + event.getSession().getId() + ". name="+event.getName() + " old Value:"  +event.getValue());
	}

}
