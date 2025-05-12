<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>YCP Finals Frenzy - Login</title>
    <style type="text/css">
        body {
            margin: 0;
            padding: 0;
            background-color: #d0d0d0;
            font-family: Arial, sans-serif;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }
        .header-bar {
            background-color: #2e8b57;
            color: white;
            height: 40px;
            padding: 0 20px;
            box-shadow: 0 0 20px rgba(46, 139, 87, 0.8);
            position: relative;
            margin-bottom: 40px;
        }
        .header-bar .logo {
            position: absolute;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            height: 90px;
            width: auto;
        }
        .content {
            flex: 1;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            padding: 20px;
        }
        .login-container {
            background-color: #b0b0b0;
            border-radius: 10px;
            padding: 30px;
            width: 100%;
            max-width: 400px;
            box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
        }
        h2 {
            color: #2e8b57;
            text-align: center;
            margin: 0 0 30px 0;
            font-size: 24px;
        }
        .error {
            color: red;
            text-align: center;
            margin-bottom: 20px;
            padding: 10px;
            background-color: rgba(255, 0, 0, 0.1);
            border-radius: 5px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: bold;
        }
        .input-field {
            width: 100%;
            padding: 12px;
            font-size: 16px;
            border: 1px solid #999;
            border-radius: 5px;
            box-sizing: border-box;
            transition: box-shadow 0.3s ease;
            background-color: white;
        }
        .input-field:hover,
        .input-field:focus {
            box-shadow: 0 0 10px rgba(46, 139, 87, 0.8);
            outline: none;
            border-color: #2e8b57;
        }
        .submit-button {
            width: 100%;
            padding: 12px;
            font-size: 16px;
            background-color: #2e8b57;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            transition: background-color 0.3s ease;
            margin-top: 10px;
        }
        .submit-button:hover {
            background-color: #3c9d6a;
        }
        .register-link {
            text-align: center;
            margin-top: 20px;
            color: #333;
        }
        .register-link a {
            color: #2e8b57;
            text-decoration: none;
            font-weight: bold;
        }
        .register-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="header-bar">
        <img class="logo"
             src="${pageContext.request.contextPath}/images/Logo.png"
             alt="YCP Finals Frenzy Logo">
    </div>
    
    <div class="content">
        <div class="login-container">
            <h2>Welcome Back!</h2>
            <c:if test="${not empty errorMessage}">
                <div class="error">${errorMessage}</div>
            </c:if>
            
            <form method="post"
                  action="${pageContext.request.contextPath}/login">
                <div class="form-group">
                    <label for="usernameOrEmail">Username or Email</label>
                    <input type="text"
                           id="usernameOrEmail"
                           name="usernameOrEmail"
                           class="input-field"
                           placeholder="Enter your username or email"
                           required />
                </div>
                
                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password"
                           id="password"
                           name="password"
                           class="input-field"
                           placeholder="Enter your password"
                           required />
                </div>
                
                <button type="submit" class="submit-button">Login</button>
            </form>
            
            <div class="register-link">
                Don't have an account? <a href="${pageContext.request.contextPath}/register">Sign Up</a>
            </div>
        </div>
    </div>
</body>
</html>
