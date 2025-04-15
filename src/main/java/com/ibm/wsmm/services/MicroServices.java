// IBM Confidential OCO Source Material
// 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.wsmm.services;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ibm.wsmm.services.waiter.Waiter;

public class MicroServices {
	private static final Object lck = new Object();

	private static final String idStr = " for " + lck;

	private static int begun = 0, done = 0, conc = 0;

	private static int concMin = 1000000, concMax = 0;

	private static double conc1 = 0;

	private static long lastTime = 0, lastRept = 0, lastConc = 0;

	private static DateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private boolean deterministic = true, zk = false, didPrintheader = false,
			reportChanges = false;

	private int countMean = 0, countMax = 0, yieldIterations = 0,
			sleepIterations = 0;

	private long sleepLength = 0, tocount = 0;

	private static void deltaConc(int delta) {
		long now;
		boolean report;
		int repBegun = 0, repDone = 0, repConc = 0, repMin = 0, repMax = 0;
		double repAvg = 0;
		synchronized (lck) {
			now = System.currentTimeMillis();
			if (conc == 0 && delta == 0 && now > lastConc + 150 * 1000)
				return;
			conc1 += conc * (now - lastTime);
			conc += delta;
			if (conc > 0 || delta != 0)
				lastConc = now;
			if (delta > 0)
				begun++;
			else if (delta < 0)
				done++;
			if (conc < concMin)
				concMin = conc;
			if (conc > concMax)
				concMax = conc;
			lastTime = now;
			report = now > (lastRept + 30000);
			if (report) {
				repBegun = begun;
				repDone = done;
				repConc = conc;
				repMin = concMin;
				repMax = concMax;
				repAvg = conc1 / (now - lastRept);
				concMin = conc;
				concMax = conc;
				conc1 = 0;
				lastRept = now;
			}
		}
		if (report)
			System.out.println("begun = " + repBegun + ", done = " + done
					+ ", conc = " + conc + " range = [" + repMin + ", "
					+ repAvg + ", " + repMax + "] at "
					+ df.format(new Date(now)) + idStr);
		return;
	}

	private static Runnable reporter = new Runnable() {
		public void run() {
			try {
				while (true) {
					deltaConc(0);
					Thread.sleep(60000);
				}
			} catch (InterruptedException ex) {
				System.out.println("Interrupted reporter" + idStr);
			} finally {
				System.out.println("Exiting reporter" + idStr);
			}
		}
	};
	static {
		System.out.println("Initializing" + idStr);
		new Thread(reporter).start();
	}

	public static String getId() {
		return idStr;
	}

	/**
	 * Allocates memory in blocks of <code>chunkSize</code> (which must be at
	 * least 2) ints. Processes <code>m</code> blocks concurrently. Processing
	 * consists of twiddling <code>m*nInner*(chunkSize/100)</code> ints
	 * (striped across the blocks), then freeing one block and allocating a new
	 * one in its place, sleeping for <code>sleepMs</code> milliseconds, and
	 * repeating for <code>nOuter</code> counts.
	 */
	public int memBound(int chunkSize, int m, int nInner, int sleepMs,
			int nOuter) throws Exception {
		deltaConc(1);
		try {
			int[][] blocks = new int[m][];
			int sum = 0;
			for (int i = 0; i < m; i++) {
				blocks[i] = new int[chunkSize];
				blocks[i][0] = blocks[i][1] = 1;
			}
			int finger = 0;
			for (int outer = 0; outer < nOuter; outer++) {
				for (int inner = 0; inner < nInner; inner++) {
					for (int s = (inner + outer * nInner) % 100; s < chunkSize; s += 100) {
						for (int i = 0; i < m; i++) {
							sum += (blocks[(i + 2) % m][(s + 2) % chunkSize] = blocks[(i + 1)
									% m][(s + 1) % chunkSize]
									+ blocks[i][s]);
						}
					}
					Thread.yield();
				}
				try {
					if (sleepMs > 0)
						Thread.sleep(sleepMs);
				} catch (InterruptedException ex) {
				}
				blocks[finger] = new int[chunkSize];
				blocks[finger][0] = blocks[finger][1] = 1;
				finger = (finger + 1) % m;
			}
			return sum;
		} finally {
			deltaConc(-1);
		}
	}

	public boolean CPUBound(long count) throws Exception {
		return CpuAndSleep(false, count, 100000, 1000, count / 10, 1, true,
				true);
	}

	public boolean CpuAndSleep(boolean deterministic, long countMean,
			long countMax, long yieldIterations, long sleepIterations,
			int sleepLength, boolean debConc, boolean zk) {

		if (debConc)
			deltaConc(1);
		try {
			double i = 0.0;
			int seed = 10007;
			int base = 1664525;
			double max = 4294967295.0;
			long yik = zk ? 0 : yieldIterations / 2;
			long sik = zk ? 0 : sleepIterations / 2;
			long tocount = countMean;
			if (!deterministic) {
				double d = Math.log((double) 1.0 - Math.random());
				tocount = (long) Math.ceil(-d * countMean);
				boolean unclipped = tocount <= countMax;
				if (!unclipped)
					tocount = countMax;
			}
			for (long now = 0; now < tocount; now++) {
				seed = (seed * base) + 1;
				i += ((double) seed / (double) max) / (now + 1);
				if ((now % yieldIterations) == yik) {
					Thread.yield();
				}
				if ((now % sleepIterations) == sik) {
					try {
						Thread.sleep(sleepLength);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return i < 0;
		} finally {
			if (debConc)
				deltaConc(-1);
		}
	}

	public String CpuAndGet(boolean deterministic, long countMean, long countMax,
			long yieldIterations, long getIterations, URL getUrl,
			int readBufSize, boolean debConc, boolean zk, boolean returnFirst) {
		if (debConc)
			deltaConc(1);
		byte[] readBuf = new byte[readBufSize];
		String ans = null;
		try {
			double i = 0.0;
			int seed = 10007;
			int base = 1664525;
			double max = 4294967295.0;
			long yik = zk ? 0 : yieldIterations / 2;
			long sik = zk ? 0 : getIterations / 2;
			long tocount = countMean;
			if (!deterministic) {
				double d = Math.log((double) 1.0 - Math.random());
				tocount = (long) Math.ceil(-d * countMean);
				boolean unclipped = tocount <= countMax;
				if (!unclipped)
					tocount = countMax;
			}
			for (long now = 0; now < tocount; now++) {
				seed = (seed * base) + 1;
				i += ((double) seed / (double) max) / (now + 1);
				if ((now % yieldIterations) == yik) {
					Thread.yield();
				}
				if ((now % getIterations) == sik) {
					try {
						ans = oneGet(getUrl, readBuf, true, true, ans,
								returnFirst);
						returnFirst = false;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return ans;
		} finally {
			if (debConc)
				deltaConc(-1);
		}
	}

	public String oneGet(URL url, byte[] readBuf, boolean readall,
			boolean reuse, String firstAns, boolean returnFirst)
			throws IOException {
		URLConnection conn = url.openConnection();
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);
		if (!reuse)
			conn.setRequestProperty("Connection", "close");
		conn.connect();
		StringWriter ansWr = null;
		String nl = "\r\n";
		if (returnFirst) {
			ansWr = new StringWriter();
			for (int i = 0; true; i++) {
				String key = conn.getHeaderFieldKey(i);
				if (key == null) {
					break;
				}
				String val = conn.getHeaderField(i);
				ansWr.write(htmlEncode(key) + ": " + htmlEncode(val) + nl);
			}
			ansWr.write("<br>" + nl);
		}
		InputStream ans = conn.getInputStream();
		if (returnFirst) {
			InputStreamReader isr = new InputStreamReader(ans);
			int last = ' ';
			while (true) {
				int c = isr.read();
				if (c < 0)
					break;
				int next = c;
				if (c == '<')
					ansWr.write("&lt;");
				else if (c == '>')
					ansWr.write("&gt;");
				else if (c == '&')
					ansWr.write("&amp;");
				else if (c == 10 || c == 13) {
					if (last + c != 10 + 13)
						ansWr.write("<br>" + nl);
					else
						next = ' ';
				} else
					ansWr.write(c);
				last = next;
			}
			isr.close();
			firstAns = ansWr.toString();
		} else if (readall) {
			while (true) {
				int nread = ans.read(readBuf);
				if (nread < 0)
					break;
			}
		} else
			ans.read();
		ans.close();
		return firstAns;
	}

	public void CpuAndWait(boolean deterministic, int countMean, int countMax,
			int yieldIterations, int getIterations, Waiter waiter,
			long sleepLength, boolean debConc, boolean zk, boolean returnFirst,
			boolean reportChanges) {
		this.reportChanges = reportChanges;
		CpuAndWait(deterministic, countMean, countMax, yieldIterations,
				getIterations, waiter, sleepLength, debConc, zk, returnFirst);

	}

	public String CpuAndWait(boolean deterministic, int countMean,
			int countMax, int yieldIterations, int getIterations,
			Waiter waiter, long sleepLength, boolean debConc, boolean zk,
			boolean returnFirst) {
		if (debConc)
			deltaConc(1);
		String ans = null;
		try {
			double i = 0.0;
			int seed = 10007;
			int base = 1664525;
			double max = 4294967295.0;
			int yik = zk ? 0 : yieldIterations / 2;
			int sik = zk ? 0 : getIterations / 2;
			long tocount = countMean;
			if (!deterministic) {
				double d = Math.log((double) 1.0 - Math.random());
				tocount = (long) Math.ceil(-d * countMean);
				boolean unclipped = tocount <= countMax;
				if (!unclipped)
					tocount = countMax;
			}

			if (this.reportChanges)
				reportValues(deterministic, countMean, countMax,
						yieldIterations, getIterations, sleepLength, zk,
						tocount);

			for (long now = 0; now < tocount; now++) {
				seed = (seed * base) + 1;
				i += ((double) seed / (double) max) / (now + 1);
				if ((now % yieldIterations) == yik) {
					Thread.yield();
				}
				if ((now % getIterations) == sik) {
					waiter.doWait(sleepLength);
					ans = null;
					returnFirst = false;
				}
			}
			return ans;
		} finally {
			if (debConc)
				deltaConc(-1);
		}
	}

	/*we observed mysterious CPU utlization on 64 bit platform. We have
	 * found that the mysterious CPU utlization is related to the JIT optimization,
	 * such as Inlining optimizaiotion. Through experiment, we find that
	 * the mystery in above CpuAndWait() method
	 * results from 1) the "if" statement in the "for" loop 
	 * refers to the loop variable; 2) computing in for loop uses the variables
	 * defined in this method and the variables are not used outside the for loop.
	 * This is for SIP AMFM autoFVT test, especially for the test on 64 bit platform.
	 */
	public String CpuAndWaitForFVT(boolean deterministic, int countMean,
			int countMax, Waiter waiter, long sleepLength, boolean debConc) {
		
		if (debConc)
			deltaConc(1);
		String ans = null;
		try {
			double i = 0.0;
			long seed = (int)(10007*Math.random());
			int base = 1664525;
			double max = 4294967295.0;
			long tocount = countMean;
			
		
			if (!deterministic) {
				double d = Math.log((double) 1.0 - Math.random());
				tocount = (long) Math.ceil(-d * countMean);
				boolean unclipped = tocount <= countMax;
				if (!unclipped)
					tocount = countMax;
			}
	
			for(long now = 0; now<tocount; now++) {
				seed = (seed +now)* base ;
				i += ((double)seed / max) / (now + 1);
					
			}

			waiter.doWait(sleepLength);
			ans = ""+i;
			return ans;
		} finally {
			if (debConc)
				deltaConc(-1);
		}
	}
	
	public void reportValues(boolean deterministic, int countMean,
			int countMax, int yieldIterations, int sleepIterations,
			long sleepLength, boolean zk, long tocount) {
		boolean shouldReport = false;

		if (!didPrintheader) {
			System.out.println("MicroServices::reportValues "
					+ "deterministic    " + "countMean     " + "countMax    "
					+ "yieldIterations     " + "sleepIterations    "
					+ "sleepLength    " + "zk    " + "tocount    ");
			didPrintheader = true;
			shouldReport = true;
		}

		if (this.deterministic != deterministic)
			shouldReport = true;
		this.deterministic = deterministic;

		if (this.countMean != countMean)
			shouldReport = true;
		this.countMean = countMean;

		if (this.countMax != countMax)
			shouldReport = true;
		this.countMax = countMax;

		if (this.yieldIterations != yieldIterations)
			shouldReport = true;
		this.yieldIterations = yieldIterations;

		if (this.sleepIterations != sleepIterations)
			shouldReport = true;
		this.sleepIterations = sleepIterations;

		if (this.sleepLength != sleepLength)
			shouldReport = true;
		this.sleepLength = sleepLength;

		if (this.zk != zk)
			shouldReport = true;
		this.zk = zk;

		if (this.tocount != tocount)
			shouldReport = true;
		this.tocount = tocount;

		if (shouldReport) {
			System.out.println("MicroServices::reportValues "
					+ this.deterministic + "    " + this.countMean + "     "
					+ this.countMax + "    " + this.yieldIterations + "     "
					+ this.sleepIterations + "    " + this.sleepLength + "    "
					+ this.zk + "    " + this.tocount + "    ");

		}

	}

	public int sleepBound(int ms, boolean deterministic, boolean precise)
			throws Exception {
		long inTime = System.currentTimeMillis();
		deltaConc(1);
		try {
			int sleepTime;
			if (deterministic)
				sleepTime = ms;
			else {
				double d = Math.log((double) 1.0 - Math.random());
				double r = (double) (-1.0 * ms);
				sleepTime = 1 + (int) (r * d);
			}
			long when = System.currentTimeMillis() + sleepTime;
			long now;
			for (;;) {
				try {
					for (;;) {
						now = System.currentTimeMillis();
						if (when <= now) {
							return (int) (System.currentTimeMillis() - inTime);
						} else if (!precise) {
							long r = when - (now + 1);
							if (r < 1)
								r = 1;
							Thread.sleep(r);
						} else if (when >= now + 20) {
							Thread.sleep(10 * ((when - now) / 10) - 15);
						} else {
							Thread.yield();
						}
					}
				} catch (InterruptedException ex) {
				}
			}
		} finally {
			deltaConc(-1);
		}
	}

	public boolean IOBound(int noOfBytes) throws Exception {
		deltaConc(1);
		try {
			BufferedOutputStream bos = null;
			FileOutputStream fos = null;
			String s = new String("Simulate IO by writing this string");
			int length = s.length();
			long startTime;
			Thread t = Thread.currentThread();
			String fname = "WS" + t.getName() + ".jnk";

			try {
				fos = new FileOutputStream(fname);
				bos = new BufferedOutputStream(fos);
			} catch (Exception e) {
				System.err.println("Could not open file: " + fname);
				bos = null;
			}

			if (bos != null) {
				for (long bytesWritten = s.length(); bytesWritten < noOfBytes;) {
					try {
						bos.write(s.getBytes(), 0, s.length());
					} catch (Exception e) {
						System.out.println(s);
					}
					bytesWritten += s.length();
				}

				try {
					bos.flush();
					FileDescriptor fd = fos.getFD();
					fd.sync();
					bos.close();
				} catch (Exception e) {
					System.err.println("Unable to close the file " + fname);
				}
			} else {
				for (long bytesWritten = 0; bytesWritten < noOfBytes; bytesWritten += s
						.length()) {
					System.out.println(s);
				}
			}
			return true;
		} finally {
			deltaConc(-1);
		}
	}

	/**
	 * Allocate <code>sessChunks</code> blocks with each block size of
	 * <code>sessChunkSize</code> ints.
	 */
	public static int[][] memAlloc(int sessChunkSize, int sessChunks)
			throws Exception {
		int[][] blocks = new int[sessChunks][];
		for (int i = 0; i < sessChunks; i++) {
			blocks[i] = new int[sessChunkSize];
		}
		return blocks;
	}

	/**
	 * Randomly allocate <code>TotalBytes</code> bytes, evenly spread among a
	 * uniform-randomly chosen number of int arrays.
	 * 
	 * @param maxSessChunks
	 *            the int maximum number of arrays
	 * @param minSessChunks
	 *            the int minimum number of arrays
	 * @param TotalBytes
	 *            the int number of bytes to allocate
	 * @return the int matrix allocated
	 */
	public static int[][] memAllocRandom(int maxSessChunks, int minSessChunks,
			int TotalBytes) {
		java.util.Random r = new java.util.Random();
		for (int i = 0; i < 5; i++)
			r.nextInt();
		int sessChunks = r.nextInt(maxSessChunks - minSessChunks + 1)
				+ minSessChunks;
		int remInts = TotalBytes / 4;
		int[][] blocks = new int[sessChunks][];
		for (int i = 0; i < sessChunks; i++) {
			int[] block = new int[remInts / (sessChunks - i)];
			remInts -= block.length;
			blocks[i] = block;
		}
		return blocks;
	}

	/**
	 * Allocate <code>m</code> blocks with each block size of
	 * <code>chunkSize</code> (which must be at least 2) ints. And than
	 * sleeping for <code>sleepMs</code> milliseconds,
	 */

	public int[][] memAllocAndSleep(int chunkSize, int m, int sleepMs)
			throws Exception {
		deltaConc(1);
		try {
			int[][] blocks = new int[m][];

			for (int i = 0; i < m; i++) {
				blocks[i] = new int[chunkSize];
			}

			try {
				if (sleepMs > 0)
					Thread.sleep(sleepMs);
			} catch (InterruptedException ex) {
			}

			return blocks;
		} finally {
			deltaConc(-1);
		}
	}

	public static String htmlEncode(String x) {
		if (x == null)
			return null;
		StringWriter sw = new StringWriter(x.length());
		for (int i = 0; i < x.length(); i++) {
			char c = x.charAt(i);
			if (c == '<')
				sw.write("&lt;");
			else if (c == '>')
				sw.write("&gt;");
			else if (c == '&')
				sw.write("&amp;");
			else
				sw.write(c);
		}
		return sw.toString();
	}

}
