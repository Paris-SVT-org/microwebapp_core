<%@page import="java.lang.Integer" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
<HEAD>
<TITLE>Redirect to another page</TITLE>
</HEAD>
<BODY>
<%
     try {
     	String urlStr = request.getParameter("url");
	    if (urlStr==null) urlStr="redirected.html";

	    %><h1>Redirecting to <%= urlStr %> </h1><%
	    response.sendRedirect(urlStr);
     } catch (Exception e) {
%> Caught exception <%= e %> 
<% 
     } 
%>

</BODY>
</HTML>

