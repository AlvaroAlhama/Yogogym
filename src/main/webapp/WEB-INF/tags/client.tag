<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="yogogym" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec"	uri="http://www.springframework.org/security/tags"%>

<%@ attribute name="name" required="true" rtexprvalue="true" %>

<sec:authorize access="isAuthenticated()">
	<c:set var="clientUsername">
		<sec:authentication property="principal.username" />
	</c:set>
</sec:authorize>

<yogogym:menuItem active="${name eq 'vets'}" url="/vets" title="Entrenamientos">
	<span class="glyphicon glyphicon-th-list" aria-hidden="true"></span>
	<span>Mis Entrenamientos</span>
</yogogym:menuItem>

<yogogym:menuItem active="${name eq 'vets'}" url="/vets" title="Rutinas">
	<span class="glyphicon glyphicon-th-list" aria-hidden="true"></span>
	<span>Rutinas</span>
</yogogym:menuItem>

<yogogym:menuItem active="${name eq 'vets'}" url="/vets" title="Dietas">
	<span class="glyphicon glyphicon-th-list" aria-hidden="true"></span>
	<span>Dietas</span>
</yogogym:menuItem>

<yogogym:menuItem active="${name eq 'challenges'}" url="/client/${clientUsername}/challenges" title="Challenges">
	<span class="glyphicon glyphicon-th-list" aria-hidden="true"></span>
	<span>Challenges</span>
</yogogym:menuItem>

<yogogym:menuItem active="${name eq 'vets'}" url="/vets" title="Dashboard">
	<span class="glyphicon glyphicon-th-list" aria-hidden="true"></span>
	<span>Dashboard</span>
</yogogym:menuItem>
