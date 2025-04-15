// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.xd.test;

public class Statistics {

  public Statistics() {
  }

  public double getMean() {
     return mean;
  }

  public double getMin() {
     return min;
  }

  public double getMax() {
     return max;
  }

  public double getSumOfSquares() {
     return sumsq;
  }

  public double getNumberOfSamples() {
     return samples;
  }

  public double getVariance() {
    return (sumsq/(samples-1));
  }

  public double getStandardDeviation() {
    return Math.sqrt(sumsq/(samples-1));
  }


  public synchronized void addSample(double sample) {
     samples++;
     double oldmean=mean;
     mean += ((sample-mean)/samples);
     sumsq += (sample-oldmean)*(sample-mean);
     min = Math.min(min,sample);
     max = Math.max(max,sample);
   }


  public void reset() {
     mean=sumsq=samples=0;
     min = Double.MAX_VALUE;
     max = Double.MIN_VALUE;
  }

  private double mean=0;
  private double sumsq=0;
  private double samples=0;
  private double min = Double.MAX_VALUE;
  private double max = Double.MIN_VALUE;
}
