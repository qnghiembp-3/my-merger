<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Home page</title>
</head>
<body>
    <form action="comparePackage" method="post" enctype="multipart/form-data">
        
        Snapshot 1: <input type="file"
            name="file1" />
        <br />
        <br />
        <br />
        Snapshot 2: <input type="file"
            name="file2" /> 
        <br />
        <br />
        <br />   
        <input type="submit" value="Compare"/>
    </form>
</body>
</html>