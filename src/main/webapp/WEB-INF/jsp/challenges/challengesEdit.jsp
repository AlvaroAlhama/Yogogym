<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>


<petclinic:layout pageName="challenges">
    
    <jsp:attribute name="customScript">
        <script>
            $(function () {
                $("#start").datepicker({dateFormat: 'yy/mm/dd'});
                $("#end").datepicker({dateFormat: 'yy/mm/dd'});
            });
        </script>
    </jsp:attribute>
    
    <jsp:body>
        <h2>Reto</h2>

        <form:form modelAttribute="challenge" class="form-horizontal" action="/challenges/save">
        
            <div class="form-group has-feedback">
            	<petclinic:inputField label="Descripci�n" name="description"/>
            	<petclinic:inputField label="Fecha de Inicio" name="start"/>
                <petclinic:inputField label="Fecha de Fin" name="end"/>
            	<petclinic:inputField label="Recompensa" name="reward"/>
            	<petclinic:inputField label="Puntos" name="points"/>
            	<h4>Ejercicio:</h4>
            	<petclinic:inputField label="Ejercicio" name="exercise.name"/>
            	<petclinic:inputField label="Descripci�n" name="exercise.description"/>
            	<petclinic:inputField label="Calor�as" name="exercise.kcal"/>
            	<!-- petclinic:selectField label="Intensidad" name="exercise.intensity" names="High,Low" size="2" -->
            	<petclinic:inputField label="Repeticiones" name="reps"/>
            	<petclinic:inputField label="Peso" name="weight"/>  
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <input type="hidden" name="id" value="${challenge.id}"/>
                    <button class="btn btn-default" type="submit">Guardar Reto</button>
                </div>
            </div>
        </form:form>

    </jsp:body>

</petclinic:layout>
