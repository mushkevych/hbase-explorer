<%--
  Show running threads (included page)
--%>
<g:if test="${threadService.threads && threadService.threads.size() > 0}">
<div class="well" style="padding:10px;">
  <strong>Running Tasks</strong>
	<ul>
		<g:each var="thread" in="${threadService.threads}">
			<li>${thread.name} : ${threadService.getStatus(thread.name)}</li>		
		</g:each>		
	</ul>
	</div>
</g:if>


