<%--  iPLMAcceptConfig.jsp   - The Processing page for custom types created for JLR.
   Copyright (c) 1992-2013 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of Dassault Systemes
   Copyright notice is precautionary only and does not evidence any actual or
   intended publication of such program
--%>

<%@include file="../emxUICommonAppInclude.inc"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.matrixone.apps.domain.util.FrameworkUtil"%>
<%@page import="matrix.util.StringList"%>
<%@page import = "matrix.db.JPO"%>
<%@page import = "com.matrixone.apps.domain.util.EnoviaResourceBundle"%>
<%@page import = "matrix.db.Context"%>
<%@page import = "java.util.StringTokenizer"%>
<%@include file="../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file="../common/emxCompCommonUtilAppInclude.inc"%>
<%@page import="com.matrixone.apps.domain.util.FrameworkException"%>
<%@page import="java.util.Map,com.matrixone.apps.framework.ui.UIUtil,com.matrixone.apps.domain.DomainRelationship,com.matrixone.apps.domain.util.MapList"%>

<html>
<head><title>Transaction In Progress..</title></head>

<%
	Enumeration en = request.getParameterNames();
   System.out.println("@@@@@@@@@@@@@@@@@@@@@@@iPLMAcceptConfig.jsp Start @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
   // enumerate through the keys and extract the values from the keys
	while (en.hasMoreElements()) {
		String parameterName = (String) en.nextElement();
		String parameterValue = request.getParameter(parameterName);		
	}

    String strRowId[] = request.getParameterValues("emxTableRowId");
	int iCount ;
	StringTokenizer st ;
	String strFinalRowId = "";
	for(iCount=0; iCount<strRowId.length; iCount++)
	{
	   int i = 0;
	   st = new StringTokenizer(strRowId[iCount], "|");
	   System.out.println("&&&&&&&strRowId[iCount] -> "+strRowId[iCount]);
	   while (st.hasMoreElements()) {
			if(i == 0)
			{
			  strFinalRowId = strFinalRowId + st.nextElement() + ","; 
			  System.out.println("&&&&&&&&&&&strFinalRowId ->"+strFinalRowId);
			}
			i++;
			System.out.println(" st.nextElement() ->"+st.nextElement() + "-> I"+i);
		}
	}
	System.out.println(" ++++++++++++++++++++++++++strFinalRowId ->"+strFinalRowId);
	String [] strFinalRowIds = new String [1];
    	strFinalRowIds[0] = strFinalRowId ;
	JPO.invoke(context, "iPLMOk2UseListImportExcel", null, "processAcceptedList", strFinalRowIds);
	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@iPLMAcceptConfig.jsp End @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	
%>
<script language="javascript" type="text/javaScript">
 top.refreshTablePage();
   	 </script>
<body>
</body>
</html>
