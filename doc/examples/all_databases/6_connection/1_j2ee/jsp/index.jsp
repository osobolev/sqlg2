<%@ page import = "j2ee.TestBean" %>

<jsp:useBean id="test" class="j2ee.TestBean" scope="session"/>

<html>
<head><title>Test</title></head>
<body>
DB server time is <%= test.getServerTime() %> 
</body>
</html>
