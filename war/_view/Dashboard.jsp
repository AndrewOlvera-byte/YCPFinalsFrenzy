<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>YCP Finals Frenzy</title>
    <style type="text/css">
        body {
            margin: 0;
            padding: 0;
            background-color: #d0d0d0; /* Darker gray background */
            font-family: Arial, sans-serif;
        }
        
        .header-bar {
            background-color: #2e8b57; /* Green header */
            color: white;
            height: 40px;            /* Fixed header height */
            padding: 0 20px;         /* Only horizontal padding */
            box-shadow: 0 0 20px rgba(46, 139, 87, 0.8); /* Lighting effect from header */
            position: relative;      /* For absolute positioning of the logo */
        }
        
        /* Style for the larger, centered logo */
        .header-bar .logo {
    		position: absolute;
    		left: 50%;
    		top: 50%;
    		transform: translate(-50%, -50%);
    		height: 90px; /* Adjust if needed */
    		width: auto;
		}

        
        .content {
            padding: 20px;
        }
        
        td.label { 
            text-align: right; 
            font-size: 16px;
        }
        
        .error { color: red; }
        
        .formatted {
            white-space: pre-wrap;
            font-size: 16px;
            text-align: left;
            padding: 15px;
            margin-bottom: 10px;
        }
        
        .scrollable-container {
            width: 315px;
            height: 300px;
            overflow: auto;
            background-color: #b0b0b0;
            border-radius: 10px;
            padding: 15px;
            white-space: pre-wrap;
            border: 1px solid #999;
            text-align: left;
            font-size: 16px;
            margin-bottom: 10px;
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
        
        /* Input container style */
        .input-container {
            width: 315px;
            padding: 15px;
            background-color: #b0b0b0; /* Darker gray for container */
            border-radius: 10px;
            margin-top: 20px;
            display: flex;
            align-items: center;
        }
        
        /* Style for the input field */
        .input-field {
            flex: 1;
            padding: 10px;
            font-size: 16px;
            border: 1px solid #999;
            border-radius: 5px;
            box-sizing: border-box;
            margin-right: 10px;
            transition: box-shadow 0.3s ease;
        }
        
        /* Hover and focus effect for the input field */
        .input-field:hover,
        .input-field:focus {
            box-shadow: 0 0 10px rgba(46, 139, 87, 0.8);
            outline: none;
        }
        
        /* Style for the submit button */
        .submit-button {
            padding: 10px 20px;
            font-size: 16px;
            background-color: #2e8b57;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        
        .submit-button:hover {
            background-color: #3c9d6a;
        }
    </style>
</head>
<body>
    <div class="header-bar">
        <!-- Logo added with a class for styling -->
        <img class="logo" src="${pageContext.request.contextPath}/images/Logo.png" alt="YCP Finals Frenzy Logo">
    </div>
    
    <div class="content">
        <!-- Flex container: Game info on the left, image with overlay on the right -->
        <div class="dashboard-container">
            <!-- Main content: Game state information on the left -->
            <div class="main-content">
                <div id="initialDisplay">
                    <div class="scrollable-container">
                        ${response.message}
                    </div>
                    <div class="input-container">
                        <form action="${pageContext.request.contextPath}/dashboard" method="post" style="display: flex; width: 100%;">
                            <input type="text" id="input-field" name="input" class="input-field" placeholder="Input" />
                            <input type="submit" value="Submit" class="submit-button" />
                        </form>
                    </div>
                    <pre class="formatted">${response.roomInventory}</pre>
                    <pre class="formatted">${response.playerInventory}</pre>
                    <pre class="formatted">${response.charactersInRoom}</pre>
                    <pre class="formatted">${response.playerInfo}</pre>
                    <pre class="formatted">${response.roomConnections}</pre>
                </div>
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
    </div>

    <!-- JavaScript to scroll the container to the bottom on page load and focus the input -->
    <script type="text/javascript">
        window.onload = function() {
            var container = document.querySelector('.scrollable-container');
            if (container) {
                container.scrollTop = container.scrollHeight;
            }
            // Set focus on the input field after the page loads
            var inputField = document.querySelector('input[name="input"]');
            if (inputField) {
                inputField.focus();
            }
        };
    </script>
</body>
</html>
