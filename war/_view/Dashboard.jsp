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
            width: 400px;
            height: 300px;
            overflow: auto;
            background-color: #f0f0f0;
            border-radius: 8px;
            padding: 10px;
            white-space: pre-wrap;
            border: 1px solid #ccc;
            text-align: center;
        }
        
        /* Flex container for side-by-side layout */
        .dashboard-container {
            display: flex;
            align-items: flex-start;
        }
        
        /* Main content for game info (left side) */
        .main-content {
            flex: 1;
            margin-right: 20px;
        }
        
        /* Sidebar for room image (right side) with relative positioning for overlay */
        .sidebar {
            flex: 0 0 1000px;
            position: relative;
        }
        .sidebar img {
            width: 100%;
            height: auto;
            display: block;
        }
        /* Styling for the overlay number */
        .number-label {
            position: absolute;
            top: 0.75in;  /* about 1 inch from the top */
            left: 50%;
            transform: translateX(-50%);
            font-size: 72px;
            font-weight: bold;
            color: #fff;
            text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.7);
            z-index: 10;
        }
    </style>
</head>
<body>
    <h1>YCP Finals Frenzy</h1>
    
    <!-- Flex container: Game info on the left, image with overlay on the right -->
    <div class="dashboard-container">
        <!-- Main content: Game state information on the left -->
        <div class="main-content">
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
        </div>
        
        <!-- Sidebar: Room image with dynamic room number overlay -->
		<div class="sidebar" style="position: relative;">
		    <img src="${response.roomImage}" alt="Current Room Image" style="width:100%; height:auto;"/>
		    <div class="number-label">${response.roomNumber}</div>
		    <!-- Directly output the overlay HTML for items and characters -->
		    ${response.roomItemsOverlay}
		    ${response.roomCharactersOverlay}
		</div>
    </div>

    <!-- JavaScript to scroll the container to the bottom on page load -->
    <script type="text/javascript">
        window.onload = function() {
            var container = document.querySelector('.scrollable-container');
            if (container) {
                container.scrollTop = container.scrollHeight;
            }
        };
    </script>
</body>
</html>
