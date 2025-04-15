package com.ibm.wsmm.services;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple command-line access to the MicroServices functions,
 * designed to serve as a simple "longer-running job" (i.e.
 * JavaMain, native code, etc) for testing.  Repeatedly
 * calls the underlying MicroServices function for the number
 * of seconds given by the runtimeInSeconds switch (default 10); 
 * see the MicroWebApp documentation page for details of other args.
 * Examples:
 * 
 * MicroJob CpuAndSleepBound --countMax 100000 --countMean 50000
 * MicroJob IOBound --nOfBytes 500000 --runtimeInSeconds 600
 * MicroJob memBound --runtimeInSeconds 18 --chunkSize 1000000
 * 
 * Switches are, sadly, case-sensitive, due to sacrificing useability
 * for elegance of code.
 * 
 * Note: Forces exit with System.exit(0), so not suitable for
 * most embedded (non-command-line) uses. Does Not Play Well With
 * Others In The Same JVM.
 * 
 * @author chess
 */
public class MicroJob implements Runnable {
	
	/* The basic operational modes */
	private final static String CPU = "CpuAndSleepBound";
	private final static String MEM = "MemBound";
	private final static String IO = "IOBound";
	private final static String[] VERBS = {CPU,MEM,IO};
	/* The parameters */
	private final static String DETERMINISTIC = "deterministic";
	private final static String COUNT_MAX = "countMax";
	private final static String COUNT_MEAN = "countMean";
	private final static String YIELD_INTERVAL = "yieldInterval";
	private final static String SLEEP_INTERVAL = "sleepInterval";
	private final static String ZERO_KEY = "zk";
	private final static String SLEEP_LENGTH = "sleepLength";
	private final static String DEB_CONC = "debConc";
	private final static String N_OF_BYTES = "nOfBytes";
	private final static String CHUNK_SIZE = "chunkSize";
	private final static String NUMBER_OF_CONCURRENT_BLOCKS = "m";
	private final static String N_INNER = "nInner";
	private final static String N_OUTER = "nOuter";
	private final static String SLEEP_MS = "sleepMS";
	private final static String RUNTIME_IN_SECONDS = "runtimeInSeconds";
	private final static String VERB = "verb";
	/**
	 * This map both contains the default values for all the settings,
	 * and also contains (as its keys) all allowed switchnames, set to
	 * the types (Integer or Boolean or String) that the corresponding
	 * switches require.  
	 */
	private static final Map DEFAULT_SETTINGS;
	static {	
		DEFAULT_SETTINGS = new HashMap();
		DEFAULT_SETTINGS.put(VERB,CPU);
		DEFAULT_SETTINGS.put(DETERMINISTIC,new Boolean(false));
		DEFAULT_SETTINGS.put(COUNT_MAX,new Integer(100000));
		DEFAULT_SETTINGS.put(COUNT_MEAN, new Integer(30000));
		DEFAULT_SETTINGS.put(YIELD_INTERVAL, new Integer(1000));
		DEFAULT_SETTINGS.put(SLEEP_INTERVAL, new Integer(3000));
		DEFAULT_SETTINGS.put(ZERO_KEY, new Boolean(true));
		DEFAULT_SETTINGS.put(SLEEP_LENGTH, new Integer(1));
		DEFAULT_SETTINGS.put(DEB_CONC, new Boolean(true));
		DEFAULT_SETTINGS.put(N_OF_BYTES, new Integer(1000));
		DEFAULT_SETTINGS.put(CHUNK_SIZE, new Integer(2));
		DEFAULT_SETTINGS.put(NUMBER_OF_CONCURRENT_BLOCKS, new Integer(2));
		DEFAULT_SETTINGS.put(N_INNER,new Integer(2));
		DEFAULT_SETTINGS.put(N_OUTER, new Integer(2));
		DEFAULT_SETTINGS.put(SLEEP_MS, new Integer(2));	
		DEFAULT_SETTINGS.put(RUNTIME_IN_SECONDS, new Integer(10));
	}
	
	public static void main(String[] args) {
		MicroJob mj = new MicroJob();
		try {
			mj.setSettings(args);			
		} catch(IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println("For instance: CpuAndSleepBound --countMax 200000 --countMean 50000 --runtimeInSeconds 50");
			System.err.println("Repeatedly calls the underlying MicroService for runtimeInSeconds seconds (default 10).");
			System.err.println("See MicroWebApp help for usage of other switches.");
			System.exit(100);
		}
		mj.run();
	}
	
	/**
	 * Interpret the string as a thing of the same type as the sample.
	 * Works only for Strings, Integers, and Booleans.  Used in 
	 * overengineered argument parsing code.
	 * 
	 * @param string some letters and numbers and things
	 * @param sample an object of some type
	 * @return the interpreted object
	 */
	private static Object InterpretToMatch(String string, Object sample) {
		if (sample instanceof String) return string;
		if (sample instanceof Boolean) return new Boolean(string);
		if (sample instanceof Integer) return new Integer(string);
		throw new IllegalArgumentException();
	}
		
	/* end static stuff */
	
	/* Map from them capitalized things up there to values */
	private final Map settings = new HashMap(DEFAULT_SETTINGS);
	/* Where to say what we're about to do. */
	private PrintStream outputPrintStream = System.out;
	
	/**
	 * I'm going to be lazy here and pass you off to MicroWebApp
	 * for the meanings of the args, except for runTimeInSeconds,
	 * which tells the program for how many seconds to run
	 * altogether, repeatedly calling the underlying MicroService
	 * with the given parameters.
	 * 
	 * @param args command-line-type arguments.
	 */
	public void setSettings(String[] args) {
			// I'm sure there's some much more Javay way of doing this...
			for (int i=0;i<args.length;) {
				String switchName;
				Object newSetting;
				if (args[i].startsWith("--")) {
					switchName = args[i].substring(2);
					if (!this.settings.containsKey(switchName)) {
						fatal("Unknown switch "+args[i]);  // No return
					} 
					i++;
					if (i==args.length) {
						fatal("Missing value for "+switchName);  // No return
					}
				} else {
					switchName = VERB;
				}
				Object currentSetting = this.settings.get(switchName);
				try {  // Structured exception handling is really alot of work
					newSetting = InterpretToMatch(args[i],currentSetting);	
				} catch(IllegalArgumentException e) {
					throw new IllegalArgumentException("Invalid value ["+args[i]+"] for "+switchName);						
				}					
				this.settings.put(switchName,newSetting);
				i++;
			}
			// Now all arguments have been consumed and settings set.
			// Make sure the verb is one we've heard of.
			boolean validVerb = false;
			for (int i=0;i<VERBS.length;i++) {
				if (VERBS[i].equalsIgnoreCase((String)this.settings.get(VERB))) {
					validVerb = true;
					break;
				}
			}
			if (!validVerb) fatal("Unknown verb "+this.settings.get(VERB));
	}
	
	/**
	 * Change the print stream to which this will output
	 * routine things like what it's about to do 
	 * (defaults to System.out).  Not really very effective,
	 * as the underlying utility code always talks to 
	 * System.out regardless.  But hey.
	 * 
	 * @param ps the one to use instead.
	 */
	public void setOutputPrintStream(PrintStream ps) {
		this.outputPrintStream = ps;
	}

	/**
	 * Something unrecoverably bad was found in the arguments.
	 * 
	 * @param msg What it was.
	 * @throws IllegalArgumentException obviously
	 */
	private void fatal(String msg) throws IllegalArgumentException {
		throw new IllegalArgumentException(msg);
	}

	/**
	 * Actually do the stuff requested by the settings, repeatedly
	 * calling the underlying MicroService until the runtime has
	 * elapsed.  Note that at that point it does a System.exit(),
	 * which is rather rude and should probably be fixed, but at
	 * least it's effective.
	 */
	public void run() {
		MicroServices microServices = new MicroServices();  // Thank you, Java.
		// Start the reaper thread.
		final int runtimeInSeconds = ((Integer)this.settings.get(RUNTIME_IN_SECONDS)).intValue();
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000*runtimeInSeconds);
				} catch(InterruptedException e) {
					// Apparently someone wants to stop us early,
					// so we'll just flow right along and reap.				
				}
				System.exit(0); 
			}}).start();
		// Actually do the thing requested, forever, awaiting the reaper
		String verb = (String)this.settings.get(VERB);
		this.outputPrintStream.println("Doing "+verb+" for "+runtimeInSeconds+" seconds");
		for (;;) {
			if (verb.equalsIgnoreCase(CPU)) {
				/* boolean ans = */				
					microServices.CpuAndSleep(
						((Boolean)this.settings.get(DETERMINISTIC)).booleanValue(),
						((Integer)this.settings.get(COUNT_MEAN)).intValue(),
						((Integer)this.settings.get(COUNT_MAX)).intValue(),
						((Integer)this.settings.get(YIELD_INTERVAL)).intValue(),
						((Integer)this.settings.get(SLEEP_INTERVAL)).intValue(),
						((Integer)this.settings.get(SLEEP_LENGTH)).intValue(),
						((Boolean)this.settings.get(DEB_CONC)).booleanValue(),
						((Boolean)this.settings.get(ZERO_KEY)).booleanValue());
			} else if (verb.equalsIgnoreCase(IO)) {
				try {
					/* boolean ans = */				
						microServices.IOBound(
							((Integer)this.settings.get(N_OF_BYTES)).intValue());
				} catch(Exception e) {
					this.outputPrintStream.println("IOBound threw an exception.");
					e.printStackTrace();  // You have a better idea?
				}
			} else if (verb.equalsIgnoreCase(MEM)) {
				try {
					/* int ans = */				
						microServices.memBound(
							((Integer)this.settings.get(CHUNK_SIZE)).intValue(),
							((Integer)this.settings.get(NUMBER_OF_CONCURRENT_BLOCKS)).intValue(),
							((Integer)this.settings.get(N_INNER)).intValue(),
							((Integer)this.settings.get(SLEEP_MS)).intValue(),
							((Integer)this.settings.get(N_OUTER)).intValue()
							);
				} catch(Exception e) {
					this.outputPrintStream.println("memBound threw an exception.");
					e.printStackTrace(); 
				}
			} else {
				// This is actually caught during argument parsing, so
				// getting here is a bug.  
				fatal("Internal error: unknown verb "+verb);			
			}			
		}
	}

}
 