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
        <div class="col-md-12">
           <h1 align="center">MPS</h1>
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

