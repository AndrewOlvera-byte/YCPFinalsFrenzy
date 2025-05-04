<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <title>Choose Your Class</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css"/>
</head>
<body>
  <div class="header-bar">
    <img class="logo" src="${pageContext.request.contextPath}/images/Logo.png" alt="Frenzy Logo"/>
  </div>
  <div class="content center">
    <h2>Select Your Class</h2>
    <form action="${pageContext.request.contextPath}/dashboard" method="post">
      <button name="selectedClass" value="ATTACK" class="btn">Attack</button>
      <button name="selectedClass" value="DEFENSE" class="btn">Defense</button>
      <button name="selectedClass" value="NORMAL" class="btn">Normal</button>
    </form>
  </div>
</body>
</html>
