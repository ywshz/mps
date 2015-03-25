<%--
  Created by IntelliJ IDEA.
  User: wangshu.yang
  Date: 2015/3/25
  Time: 9:41
  To change this template use File | Settings | File Templates.
--%>
<%--
  Created by IntelliJ IDEA.
  User: wangshu.yang
  Date: 2014/12/15
  Time: 14:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
    pageContext.setAttribute("path",path);
    pageContext.setAttribute("basePath",basePath);
%>

<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>MPS</title>

    <link href="${path }/bootstrap/css/bootstrap.css" rel="stylesheet">

    <link href="${path }/css/index.css" rel="stylesheet">

    <!-- Just for debugging purposes. Don't actually copy this line! -->
    <!--[if lt IE 9]><script src="${path }/bootstrap/js/ie8-responsive-file-warning.js"></script><![endif]-->

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="http://cdn.bootcss.com/html5shiv/3.7.0/html5shiv.min.js"></script>
    <script src="http://cdn.bootcss.com/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
</head>

<body>
<%@ include file="../common/headmenu.jsp"%>

<div class="container">
    <div class="row">
        <div class="col-md-12 text-right">
            <button class="btn btn-primary" type="submit">添加任务</button>
            <button class="btn btn-default" type="submit">添加目录</button>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th>类型</th>
                    <th>名称</th>
                    <th>创建时间</th>
                    <th>修改时间</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                    <c:forEach items="${files}" var="file">
                        <tr>
                            <td>${file.type}</td>
                            <td>${file.name}</td>
                            <td>${file.createTime}</td>
                            <td>${file.modifyTime}</td>
                            <td>
                                <button class="btn btn-default btn-xs" type="submit">详情</button>
                                <button class="btn btn-default btn-xs" type="submit">历史</button>
                                <button class="btn btn-default btn-xs" type="submit">删除</button>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
<!-- /.container -->


<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="${path }/js/jquery-1.11.1.min.js"></script>
<script src="${path }/bootstrap/js/bootstrap.min.js"></script>
<script src="${path }/highcharts/js/highcharts.js"></script>
<script type="text/javascript">
    var BASE_PATH = '${path }';
</script>
</body>
</html>

