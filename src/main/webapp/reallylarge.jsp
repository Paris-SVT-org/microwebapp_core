<%@page import="java.lang.Integer" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
<HEAD>
<TITLE>Really, really big HTML page</TITLE>
</HEAD>
<BODY>
<%
     try {
     	 String sizeStr = request.getParameter("size");
	 if (sizeStr==null) sizeStr="1000";
     	 int size = Integer.parseInt(sizeStr)*1000;
	 %><h1>Generating <%= size %> bytes.</h1><%
	 String msg="All work and no play makes Jack a dull boy.";
	 while (size > 0) {
	       if (size<msg.length()) msg=msg.substring(0,size);
	       %>
	       <%= msg %>
	       <%
	       size-=msg.length();
	 }
     } catch (Exception e) {
%> Caught exception <%= e %> 
<% 
     } 
%>

</BODY>
</HTML>

