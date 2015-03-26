<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
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
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    pageContext.setAttribute("path", path);
    pageContext.setAttribute("basePath", basePath);

    String parent = (String) request.getAttribute("parent");
    if (parent == null) parent = "";
    String[] parentSplits = parent.split("/");
    List<String[]> nav = new ArrayList<String[]>();
    if (parentSplits.length == 1) {
        nav.add(new String[]{"ROOT", ""});
    } else {
        nav.add(new String[]{"ROOT", ""});
        for (int i = 1; i < parentSplits.length; i++) {
            nav.add(new String[]{parentSplits[i], nav.get(nav.size() - 1)[1] + "/" + parentSplits[i]});
        }
    }

    if (nav.size() == 1) nav.get(0)[0] = "ROOT";
    pageContext.setAttribute("parentSplits", nav);
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
    <link href="${path }/js/codemirror/lib/codemirror.css" rel="stylesheet" >
    <link href="${path }/css/index.css" rel="stylesheet">

    <!-- Just for debugging purposes. Don't actually copy this line! -->
    <!--[if lt IE 9]>
    <script src="${path }/bootstrap/js/ie8-responsive-file-warning.js"></script><![endif]-->

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="http://cdn.bootcss.com/html5shiv/3.7.0/html5shiv.min.js"></script>
    <script src="http://cdn.bootcss.com/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
</head>

<body>
<%@ include file="../common/headmenu.jsp" %>
<div class="container">
    <div class="row">
        <div class="col-md-6 text-left">
            <ol class="breadcrumb">
                <c:forEach items="${parentSplits}" var="p">
                    <li><a href="javascript:enter('${p[1]}');">${p[0]}</a></li>
                </c:forEach>
            </ol>
        </div>

        <div class="col-md-6 text-right">
            <button class="btn btn-primary" type="submit">添加任务</button>
            <button class="btn btn-default" type="submit">添加目录</button>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <table class="table table-hover">
                <thead>
                <tr>
                    <th>类型</th>
                    <th>状态</th>
                    <th>名称</th>
                    <th>创建时间</th>
                    <th>修改时间</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${files}" var="file">
                    <tr>
                        <c:if test="${file.type eq 'file'}">
                            <td>任务</td>
                            <td>${file.status}</td>
                            <td>${file.name}</td>
                            <td><fmt:formatDate value="${file.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td><fmt:formatDate value="${file.modifyTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td>
                                <button class="btn btn-default btn-xs" type="submit">开启/关闭</button>
                                <button class="btn btn-default btn-xs" type="submit"
                                        onclick="javascript:detail('${parent}/${file.name}');">详情
                                </button>
                                <button class="btn btn-default btn-xs" type="submit"
                                        onclick="javascript:detail('${parent}/${file.name}');">历史
                                </button>
                                <button class="btn btn-default btn-xs" type="submit">删除</button>
                            </td>
                        </c:if>
                        <c:if test="${file.type eq 'folder'}">
                            <td>任务组</td>
                            <td>-</td>
                            <td><a href="javascript:enter('${parent}/${file.name}');">${file.name}</a></td>
                            <td><fmt:formatDate value="${file.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td><fmt:formatDate value="${file.modifyTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td>
                                <button class="btn btn-default btn-xs" type="submit"
                                        onclick="javascript:enter('${parent}/${file.name}');">进入
                                </button>
                                <button class="btn btn-default btn-xs" type="submit">删除</button>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
<!-- /.container -->

<div class="modal fade" id="detail-modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">任务详情</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" role="form">
                    <div class="form-group">
                        <label for="inputName" class="col-sm-2 control-label">ID</label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="inputID" value="" readonly=>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputName" class="col-sm-2 control-label">名称</label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="inputName" value="">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="" class="col-sm-2 control-label">调度类型</label>

                        <div class="col-sm-10">
                            <select class="form-control" id="inputJobType">
                                <option value="HIVE">Hive脚本</option>
                                <option value="SHELL">Shell脚本</option>
                                <option value="PYTHON">Python脚本</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="" class="col-sm-2 control-label">依赖/定时</label>

                        <div class="col-sm-10">
                            <div class="radio">
                                <label>
                                    <input type="radio" name="scheduleType" id="radioSchedualByTime" value="1">
                                    定时表达式
                                </label>
                                <input type="text" class="form-control" id="inputCron" value="0 0 0 * * ?">
                            </div>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="scheduleType" id="radioSchedualByDependency" value="2">
                                    依赖
                                </label>
                                <input type="text" class="form-control" id="real-dependency" name="dependencies">
                                <input type="hidden" class="" id="dependency" name="">
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="" class="col-sm-2 control-label">脚本</label>

                        <div class="col-sm-10">
                        </div>
                    </div>

                    <div class="form-group">
                        <textarea id="edit-script"></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                <button type="button" class="btn btn-primary">保存</button>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<form action="" id="refresh-form" method="post">
    <input type="hidden" name="parent" value="${parent}" id="parent-input">
</form>
<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="${path }/js/jquery-1.11.1.min.js"></script>
<script src="${path }/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${path }/js/codemirror/lib/codemirror.js"></script>
<script type="text/javascript" src="${path }/js/codemirror/mode/sql/sql.js"></script>
<script type="text/javascript" src="${path }/js/codemirror/mode/python/python.js"></script>

<script type="text/javascript">
    var BASE_PATH = '${path }';
</script>
<script src="${path }/js/diaodu.js"></script>
</body>
</html>

