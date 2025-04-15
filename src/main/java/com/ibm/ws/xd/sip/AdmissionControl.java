/*
 * Created on 2005-11-4
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.ibm.ws.xd.sip;


/**
 * @author peiyunz
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AdmissionControl {
	public static boolean exceptionThrow = false;
	public static boolean enable = true;
	private long prevTime;
	private float token;
	private float rate;
	private float bucket;

	public AdmissionControl() {
		this(120, 100);
	}

	public AdmissionControl(float avgRate, float bucketSize) {
		prevTime = System.currentTimeMillis();
		bucket = bucketSize;
		token = bucket / 10;
		rate = avgRate;
	}

	public synchronized boolean isOverLimit() {
		boolean bReturn = false;
		if (enable) {
			long currentTime = System.currentTimeMillis();
			token += (currentTime - prevTime) * rate / 1000;
			token = Math.min(token, bucket);
			prevTime = currentTime;

			if (token >= 1) {
				token--;
			} else {
				bReturn = true;
			}
		} else {
			bReturn = enable;
		}

		return bReturn;
	}
	/**
	 * @return
	 */
	public float getBucketSize() {
		return bucket;
	}

	/**
	 * @return
	 */
	public float getRate() {
		return rate;
	}

	/**
	 * @param f
	 */
	public void setBucketSize(float f) {
		if(f < 1){
			f = 1;
		}
		bucket = f;
		token = f/10;
	}

	/**
	 * @param f
	 */
	public void setRate(float f) {
		rate = f;
	}

	public String getInfo() {
		return "Average Rate: \t" + getRate() + "\t Bucket Size: \t" + getBucketSize();
	}
}
