<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head><title>Register</title></head>
<body>
  <h2>Register</h2>
  <c:if test="${not empty errorMessage}">
    <div style="color:red;">${errorMessage}</div>
  </c:if>
  <form method="post"
        action="${pageContext.request.contextPath}/register">
    <label>
      Username:
      <input type="text" name="username" />
    </label><br/><br/>
    <label>
      Email:
      <input type="email" name="email" />
    </label><br/><br/>
    <label>
      Password:
      <input type="password" name="password" />
    </label><br/><br/>
    <button type="submit">Sign Up</button>
  </form>
</body>
</html>
