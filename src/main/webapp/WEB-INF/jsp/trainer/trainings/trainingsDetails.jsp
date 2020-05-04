<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="yogogym" tagdir="/WEB-INF/tags" %>

<yogogym:layout pageName="trainings">

	<c:if test="${error != null}">
		<div class="text-center alert alert-danger" role="alert">
			<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
	  		<span class="sr-only">Error:</span>
   			${error} for ${client.lastName}, ${client.firstName}
   		</div>
	</c:if>
	
	<spring:url value="/trainer/${trainerUsername}/clients/{clientId}" var="clientUrl">
		<spring:param name="clientId" value="${client.id}"/>
	</spring:url>
	<h2>Trainer: Training Details of <a href="${fn:escapeXml(clientUrl)}"><c:out value="${client.firstName} ${client.lastName}"/></a></h2>
	<h3>Name: <c:out value="${training.name}"/></h3>
	<p><b>Starts:</b> <c:out value="${training.initialDate}"/></p>
	<p><b>Ends:</b> <c:out value="${training.endDate}"/></p>
	<p><b>Editing Permission:</b> <c:out value="${training.editingPermission}"/></p>
	<br>
	
	<c:choose>
		<c:when test="${training.editingPermission!='CLIENT'}">
			<spring:url value="/trainer/${trainerUsername}/clients/${client.id}/trainings/${training.id}/edit" var="trainingEditUrl" />
			<a href="${fn:escapeXml(trainingEditUrl)}">Edit Training</a>
		</c:when>
		<c:otherwise>
			<p><a style="color:grey">Edit Training</a></p>
		</c:otherwise>
	</c:choose>
	<c:choose>
		<c:when test="${training.author==trainerUsername}">
			<br>
			<br>
			<spring:url value="/trainer/${trainerUsername}/clients/${client.id}/trainings/${training.id}/delete" var="trainingDeleteUrl" />
			<a href="${fn:escapeXml(trainingDeleteUrl)}">Delete Training</a>
		</c:when>
		<c:otherwise>
			<br>
			<p><a style="color:grey">Delete Training</a></p>
		</c:otherwise>
	</c:choose>
	
	<c:if test="${hasNotRoutine}">
		<br>
		<br>
		<spring:url value="/trainer/${trainerUsername}/clients/${client.id}/trainings/${training.id}/copyTraining" var="trainingCopyUrl" />
		<a href="${fn:escapeXml(trainingCopyUrl)}">Copy Training</a>
	</c:if>
	<br>
	<br>
	<br>
		
	<h3>Routines</h3>
	<spring:url value="/trainer/${trainerUsername}/clients/${client.id}/trainings/${training.id}/routines/create" var="routineAddurl" />
	
	<c:if test="${training.editingPermission!='CLIENT'}">
		<c:choose>
			<c:when test="${training.endDate < actualDate}">
				<h3><a style="color:grey">Add Routine</a></h3>
			</c:when>
			<c:otherwise>
				<h3><a href="${fn:escapeXml(routineAddurl)}">Add Routine</a></h3>		
			</c:otherwise>
		</c:choose>
	</c:if>
	
	<ul>
		<c:forEach var="routine" items="${training.routines}">
			<spring:url value="/trainer/${trainerUsername}/clients/${client.id}/trainings/${training.id}/routines/{routineId}" var="routineUrl">
	        	<spring:param name="routineId" value="${routine.id}"/>
	        </spring:url>
			<li><a href="${fn:escapeXml(routineUrl)}"><c:out value="${routine.name}"/></a></li>
		</c:forEach>
	</ul>

	<h3>Diet</h3>
	<spring:url value="/trainer/${trainerUsername}/clients/${client.id}/trainings/{trainingId}/diets/create" var="dietAddurl" >
	<spring:param name="trainingId" value="${training.id}"/>
	</spring:url>
	
	<c:if test="${training.editingPermission!='CLIENT'}">
		<c:choose>
			<c:when test="${training.endDate < actualDate}">
				<h3><a style="color:grey">Add Diet</a></h3>
			</c:when>
			<c:otherwise>
				<h3><a href="${fn:escapeXml(dietAddurl)}">Add Diet</a></h3>	
			</c:otherwise>
		</c:choose>
	</c:if>

	
	<ul>
		
		<c:if test="${!empty training.diet}">
			<spring:url value="/trainer/${trainerUsername}/clients/${client.id}/trainings/{trainingId}/diets/{dietId}" var="dietUrl">
		       	<spring:param name="dietId" value="${training.diet.id}"/>
				<spring:param name="trainingId" value="${training.id}"/>
		    </spring:url>
			<li><a href="${fn:escapeXml(dietUrl)}"><c:out value="${training.diet.name}"/></a></li>
		</c:if>
	</ul>
	
</yogogym:layout>
