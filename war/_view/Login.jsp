<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head><title>Login</title></head>
<body>
  <h2>Login</h2>
  <c:if test="${not empty errorMessage}">
    <div style="color:red;">${errorMessage}</div>
  </c:if>
  <form method="post"
        action="${pageContext.request.contextPath}/login">
    <label>
      Username or Email:
      <input type="text" name="usernameOrEmail" />
    </label><br/><br/>
    <label>
      Password:
      <input type="password" name="password" />
    </label><br/><br/>
    <button type="submit">Login</button>
  </form>
</body>
</html>
