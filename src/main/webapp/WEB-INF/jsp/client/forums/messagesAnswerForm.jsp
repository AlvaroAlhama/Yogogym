<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form modelAttribute="answer" class="form-horizontal">

	<textarea onKeyUp = "checkCharacters(this,256)" name="content" style="padding:15px; margin: 10px 0px; max-width: 100%; min-width: 100%; min-height: 90px; max-height: 180px; border: none; background-color: rgba(0,0,0,0.1)"></textarea>
	
	<br>
	
	<button type="button" onclick="sendAnswer(this)">Send</button>
	<button type="button" onclick="cancelAnswer(this)">Cancel</button>
	<strong style="float:right; font-size: 0.9em; margin-right: 5px;" id="charcterWrapper">0/256</strong>
			       
</form:form>