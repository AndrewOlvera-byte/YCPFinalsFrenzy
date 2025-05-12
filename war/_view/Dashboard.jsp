<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<c:if test="${not empty sessionScope.selectedClass}">
<html>
<head>
    <title>YCP Finals Frenzy</title>
    <style type="text/css">
        :root {
            /* Light theme variables */
            --bg-color: #d0d0d0;
            --text-color: #121212;
            --header-bg: #2e8b57;
            --header-shadow: rgba(46,139,87,0.8);
            --panel-bg: #ffffff;
            --panel-border: #999999;
            --container-bg: #f0f0f0; 
            --input-bg: #e0e0e0;
            --button-bg: #2e8b57;
            --button-color: white;
            --connection-bg: #b0b0b0;
            --scene-border: black;
        }
        
        [data-theme="dark"] {
            /* Dark theme variables */
            --bg-color: #121212;
            --text-color: #e0e0e0;
            --header-bg: #333333;
            --header-shadow: rgba(0,0,0,0.8);
            --panel-bg: #1e1e1e;
            --panel-border: #444444;
            --container-bg: #2b2b2b;
            --input-bg: #2b2b2b;
            --button-bg: #3c9d6a;
            --button-color: #e0e0e0;
            --connection-bg: #2b2b2b;
            --scene-border: #555555;
        }
        
        /* Theme switch styles */
        .theme-switch {
            position: absolute;
            right: 20px;
            top: 50%;
            transform: translateY(-50%);
            display: flex;
            align-items: center;
        }
        
        .theme-switch label {
            margin-right: 10px;
            color: white;
            font-size: 14px;
        }
        
        .switch {
            position: relative;
            display: inline-block;
            width: 60px;
            height: 26px;
        }
        
        .switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }
        
        .slider {
            position: absolute;
            cursor: pointer;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: #ccc;
            transition: .4s;
        }
        
        .slider:before {
            position: absolute;
            content: "";
            height: 18px;
            width: 18px;
            left: 4px;
            bottom: 4px;
            background-color: white;
            transition: .4s;
        }
        
        input:checked + .slider {
            background-color: #3c9d6a;
        }
        
        input:checked + .slider:before {
            transform: translateX(34px);
        }

        body { margin: 0; padding: 0; background-color: var(--bg-color); color: var(--text-color); font-family: Arial, sans-serif; }
        .header-bar { background-color: var(--header-bg); color: white; height: 40px; padding: 0 20px; box-shadow: 0 0 10px var(--header-shadow); position: relative; }
        .header-bar .logo { position: absolute; left: 50%; top: 50%; transform: translate(-50%,-50%); height: 90px; width: auto; }
        .game-over-screen { position: fixed; top:0; left:0; width:100%; height:100%; background:black; display:flex; flex-direction:column; justify-content:center; align-items:center; z-index:1000; }
        .game-over-screen img { max-width:80%; max-height:80%; }
        .restart-button { margin-top:2rem; padding:1rem 2rem; font-size:1.2rem; cursor:pointer; background-color:#2e8b57; color:white; border:none; }
        .restart-button:hover { background-color:#3c9d6a; }
        .content { padding:20px; }
        .dashboard-container { 
            display: flex; 
            gap: 20px; 
            height: calc(100vh - 80px); 
            align-items: flex-start;
        }
        .left-panel, .right-panel {
            height: calc(100vh - 120px);
            box-sizing: border-box;
            position: relative;
        }
        .left-panel { 
            flex: 1;
            display: flex; 
            flex-direction: column; 
            max-width: 400px;
        }
        .right-panel {
            flex: 2;
            display: flex;
            flex-direction: column;
        }
        .game-io { 
            height: calc(100vh - 295px);
            display: flex; 
            flex-direction: column; 
            border: 1px solid var(--panel-border); 
            overflow: hidden; 
            box-sizing: border-box;
            background: var(--panel-bg);
        }
        .scrollable-container { flex:1; overflow-y:auto; padding:15px; background:var(--container-bg); font-size:14px; white-space:pre-wrap; color:var(--text-color); scrollbar-width: thin; scrollbar-color: #3c9d6a transparent; }
        .input-container { padding:10px; border-top:1px solid var(--panel-border); background:var(--input-bg); display:flex; }
        .input-field { flex:1; padding:8px; font-size:14px; border:1px solid var(--panel-border); background:var(--panel-bg); color:var(--text-color); margin-right:10px; }
        .submit-button { padding:8px 15px; background:var(--button-bg); color:var(--button-color); border:none; cursor:pointer; }
        .room-connections {
            height: 155px;
            margin-top: 20px;
            display: grid; 
            grid-template-areas: ". north ." "west center east" ". south ."; 
            grid-template-columns: 50px 50px 50px; 
            grid-template-rows: 50px 50px 50px; 
            gap: 5px; 
            justify-content: center;
        }
        .connection-box { background:var(--connection-bg); border:1px solid var(--panel-border); display:flex; align-items:center; justify-content:center; font-weight:bold; font-size:12px; color:var(--text-color); }
        .connection-north { grid-area:north; background:black; color:white; border:5px solid yellow; }
        .connection-east  { grid-area:east;  background:black; color:white; border:5px solid red; }
        .connection-south { grid-area:south; background:black; color:white; border:5px solid green; }
        .connection-west  { grid-area:west;  background:black; color:white; border:5px solid blue; }
        .connection-center { grid-area:center; background:black; color:white; }
        .scene-container { 
            flex: none;
            height: calc(100vh - 295px);
            display: flex; 
            flex-direction: column; 
            border: 1px solid var(--scene-border); 
            background: var(--panel-bg);
            overflow: visible;
            box-sizing: border-box;
            position: relative;
        }
        .scene-header {
            position: absolute;
            top: -10px;
            left: 50%;
            transform: translate(-50%, 0);
            background: var(--panel-bg);
            padding: 0 10px;
            font-weight: bold;
            font-size: 16px;
            color: var(--text-color);
            z-index: 1;
        }
        .game-scene {
            flex: 1;
            position: relative;
            overflow: hidden;
            background: var(--panel-bg);
        }
        .room-image { width:100%; height:100%; object-fit:contain; object-position:center; }
        .scene-entities { position:absolute; top:0; left:0; width:100%; height:100%; }
        .player-status-bars { position:absolute; bottom:0; left:0; right:0; padding:10px; box-sizing:border-box; background:rgba(0,0,0,0.7); display:flex; gap:10px; }
        .health-bar, .skill-points-bar { flex:1; height:20px; overflow:hidden; background:#444; }
        .health-bar-fill { height:100%; background:#f44; display:flex; align-items:center; justify-content:center; color:white; font-size:12px; font-weight:bold; transition:width 0.3s; }
        .skill-points-bar-fill { height:100%; background:#44f; display:flex; align-items:center; justify-content:center; color:white; font-size:12px; font-weight:bold; transition:width 0.3s; }
        .entity { position:absolute; display:flex; flex-direction:column; align-items:center; }
        .entity-health-bar { width:560px; height:10px; background:#444; overflow:hidden; margin-bottom:5px; }
        .entity-health-fill { height:100%; background:#f44; display:flex; align-items:center; justify-content:center; color:white; font-size:8px; font-weight:bold; }
        
        /* Player info boxes styles */
        .player-info-container {
            display: flex;
            gap: 20px;
            margin-top: 20px;
            height: 185px;
        }
        
        .info-box {
            flex: 1;
            border: 1px solid var(--panel-border);
            background: var(--panel-bg);
            height: 100%;
            overflow-y: auto;
            box-sizing: border-box;
        }
        
        .info-box-header {
            background: var(--header-bg);
            color: white;
            padding: 8px;
            font-weight: bold;
            text-align: center;
            border-bottom: 1px solid var(--panel-border);
        }
        
        .info-box-content {
            padding: 10px;
            font-size: 14px;
            color: var(--text-color);
        }
        
        .info-list {
            list-style-type: none;
            padding: 0;
            margin: 0;
        }
        
        .info-list li {
            padding: 4px 0;
            border-bottom: 1px solid rgba(128, 128, 128, 0.2);
        }
        
        .info-list li:last-child {
            border-bottom: none;
        }
        
        /* Inventory layout: single-column numbered list */
        .inventory-list {
            list-style-type: decimal;
            list-style-position: inside;
            margin: 0;
            padding-left: 20px;
        }
    </style>
</head>
<body>
    <div class="header-bar">
        <img class="logo" src="${pageContext.request.contextPath}/images/Logo.png" alt="YCP Finals Frenzy Logo" />
        <div class="theme-switch">
            <label for="theme-toggle">Light</label>
            <label class="switch">
                <input type="checkbox" id="theme-toggle">
                <span class="slider"></span>
            </label>
            <label for="theme-toggle">Dark</label>
        </div>
    </div>

    <c:if test="${response.gameOver}">
        <div class="game-over-screen">
            <img src="${pageContext.request.contextPath}/images/GameOver.png" alt="Game Over" />
            <form method="post" action="${pageContext.request.contextPath}/dashboard">
                <button type="submit" name="restart" value="true" class="restart-button">Restart Game</button>
            </form>
        </div>
    </c:if>

    <c:if test="${!response.gameOver}">
        <div class="content">
            <div class="dashboard-container">
                <div class="left-panel">
                    <div class="game-io">
                        <div class="scrollable-container">${response.message}</div>
                        <div class="input-container">
                            <form action="${pageContext.request.contextPath}/dashboard" method="post" style="display:flex; width:100%;">
                                <input type="text" id="input-field" name="input" class="input-field" placeholder="Input" autofocus />
                                <input type="submit" value="Submit" class="submit-button" />
                            </form>
                        </div>
                    </div>
                    <div class="room-connections">
                        <c:if test="${not empty response.northRoom}"><div class="connection-box connection-north">N</div></c:if>
                        <c:if test="${not empty response.eastRoom}"><div class="connection-box connection-east">E</div></c:if>
                        <c:if test="${not empty response.southRoom}"><div class="connection-box connection-south">S</div></c:if>
                        <c:if test="${not empty response.westRoom}"><div class="connection-box connection-west">W</div></c:if>
                        <div class="connection-box connection-center">Room</div>
                    </div>
                </div>
                
                <div class="right-panel">
                    <div class="scene-container">
                        <div class="scene-header">${response.roomName}</div>
                        <div class="game-scene">
                            <img class="room-image" src="${response.roomImage}" alt="Room" />
                            <div class="scene-entities">
                                <div id="player-container"></div>
                                <div id="items-container"></div>
                                <div id="enemies-container"></div>
                            </div>
                            <div class="player-status-bars">
                                <div class="health-bar" style="position:relative;">
                                    <div class="health-bar-fill" style="width:${response.playerHealthPercent}%"></div>
                                    <div class="health-bar-text" style="position:absolute; top:0; left:50%; transform:translateX(-50%); width:100%; height:100%; display:flex; align-items:center; justify-content:center; color:white; font-size:12px; font-weight:bold;">${response.playerCurrentHP}/${response.playerMaxHP}</div>
                                </div>
                                <div class="skill-points-bar" style="position:relative;">
                                    <div class="skill-points-bar-fill" style="width:${response.playerSkillPointsPercent}%"></div>
                                    <div class="skill-points-bar-text" style="position:absolute; top:0; left:50%; transform:translateX(-50%); width:100%; height:100%; display:flex; align-items:center; justify-content:center; color:white; font-size:12px; font-weight:bold;">${response.playerSkillPoints}/3</div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="player-info-container">
                        <div class="info-box">
                            <div class="info-box-header">Inventory</div>
                            <div class="info-box-content">
                                <ul class="info-list inventory-list">
                                    <c:choose>
                                        <c:when test="${not empty response.playerInventory}">
                                            <c:forEach var="item" items="${fn:split(response.playerInventory, ',')}">
                                                <li>${fn:trim(item)}</li>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <li>(no items)</li>
                                        </c:otherwise>
                                    </c:choose>
                                </ul>
                            </div>
                        </div>
                        
                        <div class="info-box">
                            <div class="info-box-header">Companions</div>
                            <div class="info-box-content">
                                <ul class="info-list inventory-list">
                                    <c:choose>
                                        <c:when test="${not empty response.companionInventory}">
                                            <c:forEach var="item" items="${fn:split(response.companionInventory, ',')}">
                                                <li>${fn:trim(item)}</li>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <li>No items</li>
                                        </c:otherwise>
                                    </c:choose>
                                </ul>
                            </div>
                        </div>
                        
                        <div class="info-box">
                            <div class="info-box-header">Quests</div>
                            <div class="info-box-content">
                                ${response.questOverlay}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <script>
        window.onload = function() {
            var container = document.querySelector('.scrollable-container'); if (container) container.scrollTop = container.scrollHeight;
            var inputField = document.querySelector('#input-field'); if (inputField) inputField.focus();
            // sceneEntitiesData script injection
        };

        // Theme toggle functionality
        const themeToggle = document.getElementById('theme-toggle');
        
        // Check if user previously set a preference
        const currentTheme = localStorage.getItem('theme');
        if (currentTheme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
            themeToggle.checked = true;
        }
        
        // Toggle theme when the switch is clicked
        themeToggle.addEventListener('change', function() {
            if (this.checked) {
                document.documentElement.setAttribute('data-theme', 'dark');
                localStorage.setItem('theme', 'dark');
            } else {
                document.documentElement.removeAttribute('data-theme');
                localStorage.setItem('theme', 'light');
            }
        });
    </script>

    ${response.sceneEntitiesData}
</body>
</html>
</c:if>
