 <%--  iPLMDocumentViewIntermediate.jsp
 * Ver| Date      | CDSID    | CR  | Comment
 * ---|-----------|----------|-----|---------------------
 * 01 | 11-Nov-18 |   dpuri  | 180 | Added New
--%>
<%@include file="emxNavigatorInclude.inc"%>
<%@page import="java.util.HashMap"%>
<%@page import = "matrix.db.JPO"%>
<%@page import = "com.matrixone.apps.domain.util.MapList"%>
<%@page import="com.matrixone.apps.domain.DomainObject"%>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<%
String fromProcessStructure = (String)emxGetParameter(request, "fromProcessStructure");
String strHCScoringDocId = "";
if("true".equals(fromProcessStructure)){
	String sProcessHeaderId = (String)emxGetParameter(request, "objectId");
	HashMap mpRequestMap = new HashMap();
	mpRequestMap.put("objectId", sProcessHeaderId);
	MapList vpmDocLists =  (MapList)JPO.invoke(context, "VPLMDocument", null, "getDocuments", JPO.packArgs(mpRequestMap), MapList.class);
	DomainObject doDoc = null;
	if( null != vpmDocLists && vpmDocLists.size()>0 ){
		strHCScoringDocId = (String)((Map)vpmDocLists.get(0)).get(DomainConstants.SELECT_ID);
		doDoc = DomainObject.newInstance(context,strHCScoringDocId);
	}
	if(strHCScoringDocId!=null && !"".equals(strHCScoringDocId) && doDoc.isKindOf(context,"iPLMHCScoringDocument")){
		%>
		<script language="Javascript">
		var objFrame
		objFrame = top.window.parent.top.findFrame(top.window.parent.top,'HCScoringDocInformationCmd');
		var url = "../common/emxForm.jsp?form=type_iPLMHCScoringDocumentForm&mode=edit&showPageURLIcon=false&submitAction=doNothing&formHeader=HCScoring Document EditDetails&HelpMarker=emxhelpdocumenteditdetails&suiteKey=Components&SuiteDirectory=components&StringResourceFileId=emxComponentsStringResource&objectId=<%=strHCScoringDocId%>";
		objFrame.document.location.href = url;
		</script>
		<%
	} else {
		%>
		<h3>No HC Scoring Document Found</h3>
		<%
	}
} else {
	%>
	<h3>No Data Found</h3>
	<%
}
%>

