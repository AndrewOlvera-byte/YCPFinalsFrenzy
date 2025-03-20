<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>YCP Finals Frenzy</title>
    <style type="text/css">
        td.label { text-align: right; }
        .error { color: red; }
        
        .formatted {
            white-space: pre-wrap;
        }
        
        .scrollable-container {
            width: 200px;         /* fixed width */
            height: 300px;        /* fixed height */
            overflow: auto;       /* scrollbars when content overflows */
            background-color: #f0f0f0; /* light gray background */
            border-radius: 8px;   /* rounded corners */
            padding: 10px;        /* inner spacing */
            white-space: pre-wrap; /* preserve newlines and tabs */
            border: 1px solid #ccc; /* subtle border */
            text-align: center;
        }
    </style>
</head>
<body>
    <h1>YCP Finals Frenzy</h1>
    <!-- Render the initial state from the server -->
    <div id="initialDisplay">
        <div class="scrollable-container">
        	${response.message}
        </div>
        
        
        <pre class="formatted">Room Inventory: ${response.roomInventory}</pre>
        <pre class="formatted">Player Inventory: ${response.playerInventory}</pre>
        <pre class="formatted">Characters InRoom: ${response.charactersInRoom}</pre>
        <pre class="formatted">Player Info: ${response.playerInfo}</pre>
        <pre class="formatted">Room Connections: ${response.roomConnections}</pre>
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
