package com.ibm.ws.classify.test;

public interface RemoteTimeZoneService_SEI extends java.rmi.Remote
{
 public java.lang.String getTimeZone();
 public java.lang.String getTimeZoneForClient(java.lang.String clientId);
 public java.lang.String getClock();
}