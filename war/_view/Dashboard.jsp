<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<c:if test="${not empty sessionScope.selectedClass}">
<html>
<head>
    <title>YCP Finals Frenzy</title>
    <style type="text/css">
        body {
            margin: 0;
            padding: 0;
            background-color: #d0d0d0;
            font-family: Arial, sans-serif;
        }
        .header-bar {
            background-color: #2e8b57;
            color: white;
            height: 40px;
            padding: 0 20px;
            box-shadow: 0 0 20px rgba(46, 139, 87, 0.8);
            position: relative;
        }
        .header-bar .logo {
            position: absolute;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            height: 90px;
            width: auto;
        }

        /* ─── Game Over overlay styling ───────────────────────────────── */
        .game-over-screen {
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: black;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            z-index: 1000;
        }
        .game-over-screen img {
            max-width: 80%;
            max-height: 80%;
        }
        .restart-button {
            margin-top: 2rem;
            padding: 1rem 2rem;
            font-size: 1.2rem;
            cursor: pointer;
            background-color: #2e8b57;
            color: white;
            border: none;
            border-radius: 5px;
        }
        .restart-button:hover {
            background-color: #3c9d6a;
        }
        /* ─── End Game Over styles ───────────────────────────────────── */

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
            font-size: 12px;
            text-align: left;
            padding: 5px;
            margin-bottom: 5px;
        }
        .scrollable-container {
            width: 315px;
            height: 150px;
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
        .dashboard-container {
            display: flex;
            align-items: flex-start;
        }
        .main-content {
            flex: 1;
            margin-right: 20px;
        }
        .sidebar {
            flex: 0 0 1000px;
            position: relative;
        }
        .sidebar img {
            width: 100%;
            height: auto;
            display: block;
        }
        .number-label {
            position: absolute;
            top: 0.75in;
            left: 50%;
            transform: translateX(-50%);
            font-size: 72px;
            font-weight: bold;
            color: #fff;
            text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.7);
            z-index: 10;
        }
        .input-container {
            width: 315px;
            padding: 15px;
            background-color: #b0b0b0;
            border-radius: 10px;
            margin-top: 20px;
            display: flex;
            align-items: center;
        }
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
        .input-field:hover,
        .input-field:focus {
            box-shadow: 0 0 10px rgba(46, 139, 87, 0.8);
            outline: none;
        }
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
        <img class="logo"
             src="${pageContext.request.contextPath}/images/Logo.png"
             alt="YCP Finals Frenzy Logo">
    </div>

    <!-- Game Over Screen -->
    <c:if test="${response.gameOver}">
        <div class="game-over-screen">
            <img src="${pageContext.request.contextPath}/images/GameOver.png"
                 alt="Game Over" />
            <form method="post"
                  action="${pageContext.request.contextPath}/dashboard">
                <button type="submit"
                        name="restart"
                        value="true"
                        class="restart-button">
                    Restart Game
                </button>
            </form>
        </div>
    </c:if>

    <!-- Main Game UI (hidden when gameOver) -->
    <c:if test="${!response.gameOver}">
        <div class="content">
            <div class="dashboard-container">
                <div class="main-content">
                    <div id="initialDisplay">
                        <div class="scrollable-container">
                            ${response.message}
                        </div>
                        <div class="input-container">
                            <form action="${pageContext.request.contextPath}/dashboard"
                                  method="post"
                                  style="display: flex; width: 100%;">
                                <input type="text"
                                       id="input-field"
                                       name="input"
                                       class="input-field"
                                       placeholder="Input" />
                                <input type="submit"
                                       value="Submit"
                                       class="submit-button" />
                            </form>
                        </div>
                        <pre class="formatted">${response.roomInventory}</pre>
                        <pre class="formatted">${response.playerInventory}</pre>
                        <pre class="formatted">${response.charactersInRoom}</pre>
                        <pre class="formatted">${response.playerInfo}</pre>
                        <pre class="formatted">${response.roomConnections}</pre>
                    </div>
                </div>
                <div class="sidebar">
                    <img src="${response.roomImage}"
                         alt="Current Room Image" />
                    <div class="number-label">${response.roomNumber}</div>
                    ${response.roomItemsOverlay}
                    ${response.roomCharactersOverlay}
                </div>
            </div>
        </div>
    </c:if>

    <script type="text/javascript">
        window.onload = function() {
            var container = document.querySelector('.scrollable-container');
            if (container) {
                container.scrollTop = container.scrollHeight;
            }
            var inputField = document.querySelector('input[name="input"]');
            if (inputField) {
                inputField.focus();
            }
        };
    </script>
</body>
</html>
</c:if>
