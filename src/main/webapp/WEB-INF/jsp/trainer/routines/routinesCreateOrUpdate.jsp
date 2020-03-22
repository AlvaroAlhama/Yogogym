<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="yogogym" tagdir="/WEB-INF/tags" %>

<yogogym:layout pageName="routines">

	<c:choose>
		<c:when test="${routine['new']}">
			<h2>New Routine for <c:out value="${client.firstName} ${client.lastName}"/></h2>
		 </c:when>
		 <c:otherwise>
		     <h2>Edit Routine for <c:out value="${client.firstName} ${client.lastName}"/></h2>
		 </c:otherwise>
	</c:choose>
    

	<h3>Routine Data</h3>
	<form:form modelAttribute="routine" class="form-horizontal">
		<div class="form-group has-feedback">
			<yogogym:inputField label="Name" name="name"/>
			<yogogym:inputField label="Description" name="description"/>
			<yogogym:inputField label="Repetitions Per Week" name="repsPerWeek"/>
		
			<div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                
                <c:choose>
					<c:when test="${routine['new']}">
						 <button class="btn btn-default" type="submit">Add Routine</button>
					 </c:when>
					 <c:otherwise>
					      <button class="btn btn-default" type="submit">Update Routine</button>
					 </c:otherwise>
				</c:choose>
                   
                </div>
            </div>
		</div>
	</form:form>
    
</yogogym:layout>
