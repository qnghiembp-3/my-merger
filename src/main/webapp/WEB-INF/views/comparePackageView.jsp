<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Compare Result</title>
<style>
.collapsible {
    background-color: #777;
    color: white;
    cursor: pointer;
    padding: 18px;
    width: 100%;
    border: none;
    text-align: left;
    outline: none;
    font-size: 15px;
}

.active, .collapsible:hover {
    background-color: #555;
}

.collapsible:after {
    content: '\002B';
    color: white;
    font-weight: bold;
    float: right;
    margin-left: 5px;
}

.active:after {
    content: "\2212";
}

.content {
    padding: 0 18px;
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.2s ease-out;
    background-color: #f1f1f1;
}

.div-table {
    display: table;
    width: 100%;
    background-color: #eee;
    border-spacing: 10px 0;
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

.header {
    /* text-align: center; */
    font-weight: 700;
}
a:visited{
    color: blue;
}
</style>
</head>
<body>

    <button class="collapsible">Objects (${fn:length(object)})</button>
    <div class="content">
        <div class="div-table">
            <div class="div-table-row">
                <div class="div-table-col header">File Name</div>
                <div class="div-table-col header">Status</div>
            </div>

            <c:forEach items="${object}" var="object_item">
                <div class="div-table-row">
                    <div class="div-table-col">
                        <a
                            href="compare?name=${object_item.objectName}&fileName=${object_item.fileName}&snapshot1=${snapshot1}&snapshot2=${snapshot2}&objType=${object_item.objectType}&packLine=${object_item.lineNum}&changeType=${object_item.changeType}">${object_item.objectName}</a>
                    </div>
                    <div class="div-table-col">${object_item.changeType}</div>
                </div>
            </c:forEach>
        </div>
    </div>
    <br />

    <button class="collapsible">Coach and Service (${fn:length(process)})</button>
    <div class="content">
        <div class="div-table">
            <div class="div-table-row">
                <div class="div-table-col header">File Name</div>
                <div class="div-table-col header">Status</div>
            </div>

            <c:forEach items="${process}" var="process_item">
                <div class="div-table-row">
                    <div class="div-table-col">
                        <a
                            href="compare?name=${process_item.objectName}&fileName=${process_item.fileName}&snapshot1=${snapshot1}&snapshot2=${snapshot2}&objType=${process_item.objectType}&packLine=${process_item.lineNum}&changeType=${process_item.changeType}">${process_item.objectName}</a>
                    </div>
                    <div class="div-table-col">${process_item.changeType}</div>
                </div>
            </c:forEach>
        </div>
    </div>
    <br />

    <button class="collapsible">BPD (${fn:length(bpd)})</button>
    <div class="content">
        <div class="div-table">
            <div class="div-table-row">
                <div class="div-table-col header">File Name</div>
                <div class="div-table-col header">Status</div>
            </div>

            <c:forEach items="${bpd}" var="bpd_item">
                <div class="div-table-row">
                    <div class="div-table-col">
                        <a
                            href="compare?name=${bpd_item.objectName}&fileName=${bpd_item.fileName}&snapshot1=${snapshot1}&snapshot2=${snapshot2}&objType=${bpd_item.objectType}&packLine=${bpd_item.lineNum}&changeType=${bpd_item.changeType}">${bpd_item.objectName}</a>
                    </div>
                    <div class="div-table-col">${bpd_item.changeType}</div>
                </div>
            </c:forEach>
        </div>
    </div>
    <br />

    <button class="collapsible">Environment Variable
        (${fn:length(environmentVariableSet)})</button>
    <div class="content">
        <div class="div-table">
            <div class="div-table-row">
                <div class="div-table-col header">File Name</div>
                <div class="div-table-col header">Status</div>
            </div>

            <c:forEach items="${environmentVariableSet}" var="env_item">
                <div class="div-table-row">
                    <div class="div-table-col">
                        <a
                            href="compare?name=${env_item.objectName}&fileName=${env_item.fileName}&snapshot1=${snapshot1}&snapshot2=${snapshot2}&objType=${env_item.objectType}&packLine=${env_item.lineNum}&changeType=${env_item.changeType}">${env_item.objectName}</a>
                    </div>
                    <div class="div-table-col">${env_item.changeType}</div>
                </div>
            </c:forEach>
        </div>
    </div>
    <br />

    <button class="collapsible">Coach View
        (${fn:length(coachView)})</button>
    <div class="content">
        <div class="div-table">
            <div class="div-table-row">
                <div class="div-table-col header">File Name</div>
                <div class="div-table-col header">Status</div>
            </div>

            <c:forEach items="${coachView}" var="coachView_item">
                <div class="div-table-row">
                    <div class="div-table-col">
                        <a
                            href="compare?name=${coachView_item.objectName}&fileName=${coachView_item.fileName}&snapshot1=${snapshot1}&snapshot2=${snapshot2}&objType=${coachView_item.objectType}&packLine=${coachView_item.lineNum}&changeType=${coachView_item.changeType}">${coachView_item.objectName}</a>
                    </div>
                    <div class="div-table-col">${coachView_item.changeType}</div>
                </div>
            </c:forEach>
        </div>
    </div>
    <br />

    <script>
        var coll = document.getElementsByClassName("collapsible");
        var i;

        for (i = 0; i < coll.length; i++) {
            coll[i].addEventListener("click", function() {
                this.classList.toggle("active");
                var content = this.nextElementSibling;
                if (content.style.maxHeight) {
                    content.style.maxHeight = null;
                } else {
                    content.style.maxHeight = content.scrollHeight + "px";
                }
            });
        }
    </script>
</body>
</html>