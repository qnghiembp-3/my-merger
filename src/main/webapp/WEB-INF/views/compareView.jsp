<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Compare Result</title>
</head>
<body>
    <div
        style="position: fixed; top: 8px; left: 1%; width: 98%; background: #5f9ea09c;">
        <button class="btnBack" onclick="back()">Back</button>
        <div
            style="text-align: center; font-weight: 700; margin-bottom: 10px;">
            ${name }</div>
        <div
            style="text-align: center; font-weight: 700; margin-bottom: 10px;">
            <c:if test="${fn:length(changeSelections) > 0}">
            
                Diff: <select id="changeSelections" onchange="gotoAnchor()">
                    <c:forEach items="${changeSelections}" var="changeSelection">
                        <option value="${changeSelection.position }">${changeSelection.content }</option>
                    </c:forEach>
                </select>
            </c:if>
            <br /> <br />
            <button onclick="merge()">Copy from Source to Target</button>
            <button onclick="zip()">Zip Target</button>
        </div>
    </div>
    <div class="div-table">
        <div class="div-table-row">
            <div class="div-table-col position">Lines</div>
            <div class="div-table-col" align="center">Source</div>
            <div class="div-table-col" align="center">Target</div>
        </div>

        <c:forEach items="${changes}" var="change">
            <div class="div-table-row">
                <div class="div-table-col position">
                    <a id="${change.position}">${change.position}</a>
                </div>
                <div class="div-table-col">${change.source1}</div>
                <div class="div-table-col">${change.target1}</div>
            </div>
        </c:forEach>
    </div>
    <style>
.div-table {
    display: table;
    width: 100%;
    background-color: #eee;
    border: 1px solid #6b696945;
    border-spacing: 10px 0;
    margin-top: 110px;
}

.div-table-row {
    display: table-row;
    border: 1px solid GREY;
    /* width: auto; */
    clear: both;
}

.div-table-col {
    display: table-cell;
    width: 47%;
    height: auto;
    min-height: 1px;
    word-break: break-all;
}

.position {
    width: 4%;
    text-align: center;
}

.editOldInline {
    background: #B3E5FC
}

.editNewInline {
    background: #FFF9C4;
}

.btnBack {
    position: absolute;
    top: 8px;
    left: 8px;
}
</style>
    <script>
        function gotoAnchor() {
            var lineNum = document.getElementById("changeSelections").value;
            //var url = location.href;
            //location.href = "#"+lineNum;
            //history.replaceState(null,null,url);

            var top = document.getElementById(lineNum).offsetTop - 180;
            window.scrollTo(0, top);
        }

        function back() {
            window.location.href = "comparePackage?back=true&snapshot1=${snapshot1}&snapshot2=${snapshot2}&checkLength=${checkLength}";
        }
        
        function merge(){
            var isSelectionEmpty = document.getElementById("changeSelections");
            
            if(isSelectionEmpty != null){
                var lineNum = document.getElementById("changeSelections").value;
                var index = document.getElementById("changeSelections").selectedIndex;
                var content = document.getElementById("changeSelections").options[index].text;
                var type = content.split(" ")[0];
                window.location.href = "compare?fileName=${fileName}&name=${name}&snapshot1=${snapshot1}&snapshot2=${snapshot2}&lineNum="+lineNum + "&type=" +type + "&objType=${objType}&packLine=${packLine}&changeType=${changeType}";               
            }else{
                window.location.href = "compare?fileName=${fileName}&name=${name}&snapshot1=${snapshot1}&snapshot2=${snapshot2}&lineNum=0&objType=${objType}&packLine=${packLine}&changeType=${changeType}";
            }
        }
        
        function zip(){
            window.location.href = "zip?snapshot2=${snapshot2}&fileName=${fileName}&name=${name}";
            
        }
    </script>
</body>
</html>