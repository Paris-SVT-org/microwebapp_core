/*
 * Created by mspreitz on Apr 14, 2005
 */
package com.ibm.ws.xd.test;

import java.io.PrintWriter;
import java.util.Date;

/**
 * A simple class that can be used to apply a controlled amount of CPU load. It
 * does a fixed amount of arithmetic per unit time, Thread.sleep()ing the rest.
 * 
 * @author mspreitz
 */
public class CpuLoad {

	public long foo = 3;

	final Object statsLock = new int[] { 47 };

	long startime, lastReport;

	Statistics sleeps = new Statistics();

	Statistics allSleeps = new Statistics();

	Statistics comps = new Statistics();

	Statistics allComps = new Statistics();

	/**
	 * Loops forever, doing a fixed amount of arithmetic per unit time, sleeping
	 * when not doing arithmetic. The arithmetic is done in two nested loops.
	 * The inner loop is pure arithmetic. The outer loop alternates between
	 * arithmetic and Thread.sleep()ing. Each sleep is invoked with a given
	 * fixed parameter; of course, beware the behavior of the Thread.sleep
	 * implementation! The invocations of the inner loop occur whenever enough
	 * time has passed.
	 * 
	 * @param npar
	 *            the int number of copies to run in parallel
	 * @param n1
	 *            the int length of the inner loop
	 * @param rate
	 *            the double rate of inner loops per millisecond per thread
	 * @param sms
	 *            the long number of milliseconds to sleep
	 * @param maxMins
	 *            the float limit on number of minutes to run
	 */
	public void go(final PrintWriter log, int npar, final int n1,
			final double rate, final int sms, final float maxMins) {
		final long timeLimit;
		synchronized (statsLock) {
			startime = System.currentTimeMillis();
			lastReport = -1;
			timeLimit = startime + Math.round(maxMins * 1000L * 60);
		}
		Thread[] ts = new Thread[npar];
		for (int t = 0; t < npar; t++) {
			Runnable r = new Runnable() {
				public void run() {
					long lastime = System.currentTimeMillis();
					double acc = 0;
					while (true) {
						long now;
						long before = System.currentTimeMillis();
						if (before >= timeLimit)
							return;
						if (acc <= 0) {
							try {
								Thread.sleep(sms);
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
							now = System.currentTimeMillis();
							long dt = now - before;
							synchronized (statsLock) {
								sleeps.addSample(dt);
								allSleeps.addSample(dt);
								maybePrint(log, now);
							}
						} else {
							for (int i = 0; i < n1; i++) {
								foo += (foo * 32769) % (32767 * 32767 + 255)
										+ Math.round(foo / 3.14159);
							}
							acc -= 1;
							now = System.currentTimeMillis();
							long dt = now - before;
							synchronized (statsLock) {
								comps.addSample(dt);
								allComps.addSample(dt);
								maybePrint(log, now);
							}
						}
						acc += rate * (now - lastime);
						lastime = now;
					}
				}
			};
			ts[t] = new Thread(r);
			ts[t].start();
		}
		for (int t = 0; t < npar; t++)
			try {
				ts[t].join();
			} catch (InterruptedException e) {
				log.println("Interrupted joining thread " + t);
				e.printStackTrace(log);
			}
	}

	void maybePrint(PrintWriter wr, long now) {
		long report = (now - startime) / (60 * 1000);
		if (report != lastReport) {
			lastReport = report;
			wr.println("At " + now + " (" + new Date(now) + "):");
			wr.println("   sleeps=" + fmt(sleeps));
			wr.println("allSleeps=" + fmt(allSleeps));
			wr.println("    comps=" + fmt(comps));
			wr.println(" allComps=" + fmt(allComps));
			wr.println("");
			sleeps.reset();
			comps.reset();
			wr.flush();
		}
	}

	private String fmt(Statistics stats) {
		return "{num=" + stats.getNumberOfSamples() + ", min=" + stats.getMin()
				+ ", avg=" + stats.getMean() + ", stdev="
				+ stats.getStandardDeviation() + ", max=" + stats.getMax()
				+ "}";
	}

	public static void main(String[] args) {
		int npar = 4;
		int kCount = 200;
		float rate = 5;
		int sms = 50;
		float maxMins = 100000;
		int argidx = 0;
		while (argidx < args.length) {
			String arg = args[argidx++];
			if (arg.startsWith("-") && "-rate".startsWith(arg)
					&& argidx < args.length)
				rate = Float.parseFloat(args[argidx++]);
			else if (arg.startsWith("-") && "-kCount".startsWith(arg)
					&& argidx < args.length)
				kCount = Integer.parseInt(args[argidx++]);
			else if (arg.startsWith("-") && "-sleepMillis".startsWith(arg)
					&& argidx < args.length)
				sms = Integer.parseInt(args[argidx++]);
			else if (arg.startsWith("-") && "-concurrency".startsWith(arg)
					&& argidx < args.length)
				npar = Integer.parseInt(args[argidx++]);
			else if (arg.startsWith("-") && "-maxMins".startsWith(arg)
					&& argidx < args.length)
				maxMins = Float.parseFloat(args[argidx++]);
			else {
				System.err.println("Usage: CpuLoad" + " [-rate <rate:float>]"
						+ " [-kCount <kCount:int>]"
						+ " [-sleep <sleepMillis:int>]"
						+ " [-concurrency <concurrency:int>]"
						+ " [-maxMins <minutes:float>]");
				return;
			}
		}
		CpuLoad loader = new CpuLoad();
		PrintWriter log = new PrintWriter(System.out);
		System.out.println("Going at rate=" + rate + "/s, kCount=" + kCount
				+ ", sleepMillis=" + sms + ", concurrency=" + npar
				+ ", maxMins=" + maxMins);
		loader.go(log, npar, kCount * 1000, rate / (npar * 1000), sms, maxMins);
	}
}
