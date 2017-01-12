<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title><sitemesh:write property='title'/></title>
    <%@include file="mobileMainHeader.jsp" %>
    <sitemesh:write property='head'/>
</head>
<body>

<div class="topbar wh">
    <div class="hd">
        <ul class="ul-item">
            <%--导航--%>
            <header class="am-header custom-header">
                <div class="am-header-left am-header-nav">
                    <a href="javascript:history.go(-1)" class="chevron-left"></a>
                </div>
                <!-- //End--chevron-left-->
                <h1 class="am-header-title">注册成功</h1>
                <!-- //End--title-->
                <div class="am-header-right am-header-nav">
                    <a href="#chevron-right" class="chevron-right" id="menu">
                        <i class="line"></i>
                    </a>
                </div>
                <!-- //End--chevron-left-->
                <div class="menu-list">
                    <ul class="bd">
                        <li><a href="http://mall.efeiyi.com" title="首页">首页</a></li>
                        <li><a href="/app/me/index.html" title="个人中心">个人中心</a></li>
                    </ul>
                </div>
            </header>
        </ul>
    </div>
</div>


<sitemesh:write property='body'/>
<%@include file="mobileFooter.jsp" %>


</body>

</html>
