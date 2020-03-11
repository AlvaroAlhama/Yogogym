<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="yogogym" tagdir="/WEB-INF/tags" %>

<yogogym:layout pageName="exercises">
    <h2>Exercises</h2>

    <table id="exercisesTable" class="table table-striped">
        <thead>
        <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Kcal</th>
            <th>Intensity</th>
            <th>Equipment</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${exercises}" var="exercise">
            <tr>
                <td>
                    <spring:url value="/mainMenu/exercises/{exerciseId}" var="exerciseUrl">
                        <spring:param name="exerciseId" value="${exercise.id}"/>
                    </spring:url>
                    <a href="${fn:escapeXml(exerciseUrl)}"><c:out value="${exercise.name}"/></a>
                </td>
                <td>
                    <c:out value="${exercise.description}"/>
                </td>
                <td>
                    <c:out value="${exercise.kcal}"/>
                </td>
                <td>
                    <c:out value="${exercise.intensity}"/>
                </td>                
                
                <td>
                    <spring:url value="/mainMenu/equipments/{equipmentId}" var="equipmentUrl">
                        <spring:param name="equipmentId" value="${exercise.equipment.id}"/>
                    </spring:url>
                    <a href="${fn:escapeXml(equipmentUrl)}"><c:out value="${exercise.equipment.name}"/></a>
                </td>
                                
            </tr>
        </c:forEach>
        </tbody>
    </table>
</yogogym:layout>
