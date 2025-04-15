package com.ibm.ws.xd.test;

public interface XDTestConfigHelper{
	public String[] getUnManagedNodes() throws Exception;
	public String[] getManagedNodes() throws Exception ;
	public int getServerPort(String node, String server, String portName) throws Exception;
	public String getNodeHostName(String node) throws Exception;
}
