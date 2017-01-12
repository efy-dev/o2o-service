<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title><sitemesh:write property='title'/></title>
    <sitemesh:write property='head'/>
    <script src="<c:url value='/resources/jquery/jquery-1.11.1.min.js'/>"></script>
    <!--[if (gte IE 9)|!(IE)]><!-->
    <script src="<c:url value="/resources/jquery/jquery.min.js"/>"></script>
    <!--<![endif]-->
    <%@include file="mobileMainHeader.jsp" %>



</head>
<body>

<%--导航--%>
<jsp:include flush="true"
             page="/getMenu.do?jmenuId=nav&jnodeId=nav&resultPage=/common/nav&match=${requestScope['javax.servlet.forward.servlet_path']}%3F${fn:replace(pageContext.request.queryString,'&','%26')}"/>
<sitemesh:write property='body'/>
<%@include file="mobileFooter.jsp" %>
</body>

</html>
