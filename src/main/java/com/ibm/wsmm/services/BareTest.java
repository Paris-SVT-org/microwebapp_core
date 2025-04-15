/*
 * Created on Sep 25, 2004 by mspreitz
 */
package com.ibm.wsmm.services;

/**
 * @author mspreitz
 */
public class BareTest {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println(
				"Usage: java ...BareTest nThreads [yieldInterval [debConc]]");
			return;
		}
		final int nth = Integer.parseInt(args[0]);
		int yi = 1;
		if (args.length > 1)
			yi = Integer.parseInt(args[1]);
		final boolean debConc =
			(args.length <= 2)
				|| args[2].equalsIgnoreCase("true");
		final boolean zk =
			(args.length <= 3)
				|| args[3].equalsIgnoreCase("true");
		final boolean deterministic = true;
		final int countMean = 71 * 1000;
		final int countMax = 100 * 1000 * 1000;
		final int yieldIterations = yi * 1000;
		final int sleepIterations = 35 * 1000;
		final int sleepLength = 70;
		final MicroServices d = new MicroServices();
		for (int i = 0; i < nth; i++) {
			Runnable r;
			r = new Runnable() {
				public void run() {
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
					}
					while (true) {
						try {
							d.CpuAndSleep(
								deterministic,
								countMean,
								countMax,
								yieldIterations,
								sleepIterations,
								sleepLength,
								debConc,
								zk);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			Thread t = new Thread(r);
			t.start();
		}
		System.out.println(
			"Forked "
				+ nth
				+ " threads, yieldIterations="
				+ yieldIterations
				+ ", debConc="
				+ debConc
				+ ", zk="
				+ zk
				+ ".");
	}

}
