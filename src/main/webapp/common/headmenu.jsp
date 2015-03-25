<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%
String uri = request.getRequestURI();
if(uri.contains("diaodu")){
	pageContext.setAttribute("page","diaodu");
}else if(uri.contains("node_status")){
	pageContext.setAttribute("page","node_status");
}
%>

<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse"
				data-target=".navbar-collapse">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="${path}/">MPS</a>
		</div>
		<div class="collapse navbar-collapse">
			<ul class="nav navbar-nav">
				<!--
					<li class="active"><a href="${path}">盘古Pangu</a></li>
					 -->
				<li class="${page=='diaodu'?'active':'' }"><a
					href="${path }/pages/diaodu.jsp">调度</a></li>
                <li class="${page=='node_status'?'active':'' }"><a
                        href="${path }/pages/node_status.jsp">节点状态</a></li>
			</ul>
		</div>
		<!--/.nav-collapse -->
	</div>
</div>


