<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>YCP Finals Frenzy</title>
    <style type="text/css">
        td.label { text-align: right; }
        .error { color: red; }
    </style>
</head>
<body>
    <h1>YCP Finals Frenzy</h1>
    <!-- Render the initial state from the server -->
    <div id="initialDisplay">
        <p>Message: ${response.message}</p>
        <p>Room Inventory: ${response.roomInventory}</p>
        <p>Player Inventory: ${response.playerInventory}</p>
        <p>Characters InRoom: ${response.charactersInRoom}</p>
        <p>Player Info: ${response.playerInfo}</p>
        <p>Room Connections: ${response.roomConnections}</p>
    </div>
    
    <form action="${pageContext.request.contextPath}/dashboard" method="post">
        <table>
            <tr>
                <td class="label">Enter Input:</td>
                <td><input type="text" name="input" size="12" /></td>
            </tr>
            <tr>
                <td colspan="2">
                    <input type="submit" value="Submit" />
                </td>
            </tr>
        </table>
    </form>
</body>
</html>
