<%@ page import="com.nnapz.hbaseexplorer.OrmInterface; org.apache.hadoop.hbase.HTableDescriptor; org.apache.hadoop.hbase.util.Bytes" %>
<%@ page import="java.util.Map.Entry" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>SCAN ${rowKey}</title>
    <r:script disposition="head">
    <%--
      some code to manage the toggling. I have a feeling that it could be done somewhat more simple ...
      but if IE and FFox should work...
    --%>
        function collapseAll() {
          var e = document.getElementsByClassName("tsrow");
          if (e) for (var i = 0; i< e.length; i++) {
            $(e[i]).hide();
          }
        }
    <%-- enable/uncollapse all elements with this class and toggle the switch --%>
        function on(classname) {
    <%-- toggle all elements that share that key name in their id prefix --%>
        var e = document.getElementsByClassName("rowkey" + classname);
          if (e) for (var i = 0; i< e.length; i++) $(e[i]).show();
          $(document.getElementById('rkswitchON' + classname)).hide();
          $(document.getElementById('rkswitchOFF' + classname)).show();
        }
    <%-- disable/collapse all elements with this class and toggle the switch --%>
        function off(classname) {
    <%-- toggle all elements that share that key name in their id prefix --%>
        var e = document.getElementsByClassName("rowkey" + classname);
          if (e) for (var i = 0; i< e.length; i++) $(e[i]).hide();
          $(document.getElementById('rkswitchON' + classname)).show();
          $(document.getElementById('rkswitchOFF' + classname)).hide();
        }

        $(document).ready(function(){
          $('#hidden-div').hide();
          $('#tableName').on('change', function(){
            var table = $(this).val();
            var structures = $('.structure');
            $('#hidden-div').append(structures);
            $(this).closest('form').find('.structures').append( $('.structure.'+table) );
          });
          $('#tableName').change();
        });

    </r:script>
</head>

<body onload="collapseAll();">
<content tag="customnavi">
    <li class="divider-vertical"></li>
    <li><g:link controller="hbaseSource" action="show"
                id="${hbaseSourceInstance?.id}">Show <strong>${hbaseSourceInstance?.name}</strong></g:link>
</content>

<div class="container-fluid">
    <div class="row-fluid">
        <h1>SCAN on ${hbaseSourceInstance.name}</h1>
        <g:if test="${flash.message}">
            <div class="alert alert-info">
                <a class="close" data-dismiss="alert">×</a>
                ${flash.message}
            </div>
        </g:if>
        <g:hasErrors bean="${hbaseSourceInstance}">
            <div class="alert alert-error">
                <a class="close" data-dismiss="alert">×</a>
                <g:renderErrors bean="${hbaseSourceInstance}" as="list"/>
            </div>
        </g:hasErrors>
        <g:form method="post" class="form-inline">
            <input type="hidden" name="id" value="${hbaseSourceInstance?.id}"/>
            <fieldset style="margin-top: 10px;">
                <div class="control-group scan-form-field">
                    <label class="control-label" for="tableName">Table:</label>

                    <div class="controls">
                        <g:select id="tableName" class="input-medium" name="tableName" value="${tableName}"
                                  from="${tableNames*.nameAsString}"/>
                    </div>
                </div>

                <div class="control-group scan-form-field">
                    <label class="control-label" for="rows">Rows:</label>

                    <div class="controls">
                        <input type="text" class="span1" id="rows" name="rows"
                               value="${request.getParameter('rows') ?: 3}"/>
                    </div>
                </div>

                <div class="control-group scan-form-field ">
                    <label class="control-label " for="versions">Versions:</label>

                    <div class="controls">
                        <input type="text" class="span1" id="versions" name="versions"
                               value="${request?.getParameter('versions') ?: 100}"/>
                    </div>
                </div>

                <div class="control-group scan-form-field structures">

                    <%
                        for (HTableDescriptor tDescriptor : tableNames) {
                            if (tDescriptor == null) {
                                continue;
                            }
                            String tName = tDescriptor.nameAsString;
                    %>
                    <div class="structure ${tName}">
                        <%
                            OrmInterface ormInterface = ormContext.getOrmFor(tName);
                            Map<String, Class> keyComponents = ormInterface.getRowKeyComponents(tName);
                            for (String componentName : keyComponents.keySet()) {
                        %>
                        <label class="control-label" for="${tName}_${componentName}">${componentName}:</label>

                        <div class="controls">
                            <div class="input-append">
                                <input type="text" class="input-xlarge" id="${tName}_${componentName}"
                                       name="${componentName}" value="${request?.getParameter(componentName)}"/>
                            </div>
                        </div>
                        <% } %>
                        <g:actionSubmit class="btn btn-success" style="margin-left:-1px;" value="Scan"/>
                        <% } %>
                    </div>
                </div>
            </fieldset>
        </g:form>
        <div id="hidden-div"></div>
    </div>
    <hr>

    <div class="row-fluid">
        <g:if test="${scan && !scan.isEmpty()}">
            %{--<g:if test="${scan.keySet()?.first() != rowKey}">--}%
                %{--<div class="alert">--}%
                    %{--<strong>Warning!</strong> No exact match on Rowkey <i>${request.getParameter('rowKey')}</i>--}%
                %{--</div>--}%
            %{--</g:if>--}%
            <table id="getresult" class="table getresult" style="width: 100%;">
                <thead>
                <tr>
                    <th class="scanresult" width="17%;">rowkey - timestamp</th>
                    <g:each in="${allFamilies}" var="familyName">
                        <th class="scanresult">${familyName}</th>
                    </g:each>
                </tr>
                </thead>
                <tbody>
                <!-- Map<byte[], Map<Long, Map<String, Map<String, String>>>> -->
                <% for (byte[] rowKey : scan.keySet()) {
                    OrmInterface ormInterface = ormContext.getOrmFor(tableName);
                    String strRowKey = ormInterface.parseRowKey(tableName, rowKey)

                    Map<Long, Map<String, Map<String, String>>> res = scan.get(rowKey);
                    Long[] timestamps = res.keySet().toArray(new Long[res.size()]);
                    boolean isOne = (timestamps.length == 1);

                    Map<String, Map<String, String>> mapByTimestamp;
                    if (!ormContext.containsOrmFor(tableName)) {
                        // in case of no custom ORM, we have to process qualifier+value thru the patternService
                        mapByTimestamp = hbs.getFlatMap(patternService, hbaseSourceInstance, tableName, res)
                    } else {
                        // sort in DESCENDING order
                        Arrays.sort(timestamps, Collections.reverseOrder());
                        mapByTimestamp = res.get(timestamps[0]);
                    }
                %>
                <!-- flat list first -->
                <tr class="flatrow">
                    <td>
                        <span class="rowkey">${strRowKey}</span><br>

                        <div style="float: right; margin: 0">
                            <span class="switch" id="rkswitchON${rowKey}" onclick="on('${rowKey}');">
                                <g:if test="${isOne}">Show 1 Timestamp</g:if><g:else>Show all ${timestamps.length} Timestamps</g:else>
                            </span>
                            <span class="switch" id="rkswitchOFF${rowKey}" style="display: none"
                                  onclick="off('${rowKey}');">Hide Timestamps</span>
                        </div>
                    </td>
                    <% for (String familyName : allFamilies) { %>
                    <td>
                        <%
                                Map<String, String> columns = mapByTimestamp.get(familyName);
                                if (columns != null) {
                                    for (Entry<String, String> column : columns.entrySet()) {
                                        String htmlColumn = column.getKey()
                                        String htmlValue = column.getValue()
                        %>
                        <span class="columnname">${htmlColumn}</span>: ${htmlValue}<br>
                        <%
                                    }
                                }
                        %>
                    </td>
                    <% } %>
                </tr>
                <%-- ts rows --%>
                <%
                        for (Long ts : timestamps) {
                %>
                <tr class="tsrow rowkey${rowKey}">
                    <td><g:formatDate date="${new Date(ts)}" format="yyyy-MM-dd HH:mm:ss"/> (${ts})</td>
                    <%
                            Map<String, Map<String, String>> families = res.get(ts);
                            for (String familyName : allFamilies) {
                    %>
                    <td>
                        <%
                                Map<String, String> columns = families.get(familyName);
                                if (columns != null) {
                                    for (Entry<String, String> column : columns.entrySet()) {
                                        String htmlColumn = column.getKey()
                                        String htmlValue = column.getValue()
                        %>
                        <span class="columnname">${htmlColumn}</span>: ${htmlValue}<br>
                        <%
                                    }
                                }
                        %>
                    </td>
                    <%
                            }
                    %>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </g:if>
        <g:else>
            <div class="alert alert-info">
                <strong>No Result.</strong>
            </div>
        </g:else>
        <hr>

        <div style="text-align:center; width:100%;">Scan took ${took} ms</div>
    </div>
</div>
</body>

</html>
