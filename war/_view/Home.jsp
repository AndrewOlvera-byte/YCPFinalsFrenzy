<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head>
    <title>YCP Finals Frenzy - Home</title>
    <style type="text/css">
        body {
            margin: 0;
            padding: 0;
            background-color: #d0d0d0;
            font-family: Arial, sans-serif;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
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
            padding: 20px;
        }
        h1, h2 {
            text-align: center;
            color: #2e8b57;
            margin: 0;
        }
        h1 {
            font-size: 32px;
            margin-bottom: 10px;
        }
        h2 {
            font-size: 24px;
            margin-bottom: 40px;
        }
        .save-slots {
            display: flex;
            justify-content: space-around;
            margin: 50px auto;
            max-width: 900px;
            gap: 20px;
        }
        .save-slot {
            background-color: #b0b0b0;
            border-radius: 10px;
            padding: 30px;
            width: 250px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        .save-slot:hover {
            transform: scale(1.05);
            box-shadow: 0 0 20px rgba(46, 139, 87, 0.4);
        }
        .save-slot h3 {
            color: #2e8b57;
            margin: 0 0 15px 0;
            font-size: 20px;
        }
        .save-slot p {
            margin: 0;
            color: #333;
        }
        .modal {
            display: none;
            position: fixed;
            z-index: 100;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.7);
        }
        .modal-content {
            background-color: #b0b0b0;
            margin: 10% auto;
            padding: 30px;
            border-radius: 10px;
            width: 60%;
            max-width: 600px;
            box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
            position: relative;
        }
        .close {
            position: absolute;
            right: 20px;
            top: 10px;
            color: #666;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
            transition: color 0.3s ease;
        }
        .close:hover {
            color: #2e8b57;
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
        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 12px;
            font-size: 16px;
            border: 1px solid #999;
            border-radius: 5px;
            box-sizing: border-box;
            transition: box-shadow 0.3s ease;
            background-color: white;
        }
        .form-group input:focus,
        .form-group textarea:focus {
            box-shadow: 0 0 10px rgba(46, 139, 87, 0.8);
            outline: none;
            border-color: #2e8b57;
        }
        .class-buttons {
            display: flex;
            justify-content: space-between;
            gap: 20px;
            margin: 20px 0;
        }
        .class-button {
            background-color: #999;
            padding: 20px;
            border-radius: 10px;
            cursor: pointer;
            flex: 1;
            text-align: center;
            transition: all 0.3s ease;
            border: 2px solid transparent;
        }
        .class-button:hover {
            background-color: #888;
            transform: translateY(-2px);
        }
        .class-button.selected {
            background-color: #2e8b57;
            color: white;
            border-color: #fff;
        }
        .class-button h4 {
            margin: 0 0 10px 0;
            font-size: 18px;
        }
        .class-button p {
            margin: 0;
            font-size: 14px;
            line-height: 1.4;
        }
        button[type="submit"] {
            width: 100%;
            padding: 12px;
            font-size: 16px;
            background-color: #2e8b57;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            transition: background-color 0.3s ease;
            margin-top: 20px;
        }
        button[type="submit"]:hover {
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
    
    <div class="content">
        <h1>Welcome to YCP Finals Frenzy</h1>
        <h2>Choose a save slot to begin</h2>
        
        <div class="save-slots">
            <c:forEach var="saveSlot" items="${saveSlots}" varStatus="loop">
                <c:choose>
                    <c:when test="${empty saveSlot}">
                        <div class="save-slot" onclick="openNewGameModal(${loop.index + 1})">
                            <h3>Save Slot ${loop.index + 1}</h3>
                            <p>Empty - Create New Game</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="save-slot" onclick="loadSaveSlot('${saveSlot.playerId}', '${saveSlot.currentRoom}')">
                            <h3>Save Slot ${loop.index + 1}</h3>
                            <p>${saveSlot.saveName}</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
        
        <!-- New Game Modal -->
        <div id="newGameModal" class="modal">
            <div class="modal-content">
                <span class="close" onclick="closeNewGameModal()">&times;</span>
                <h2>Create New Character</h2>
                <form id="newGameForm" method="post" action="${pageContext.request.contextPath}/home">
                    <input type="hidden" name="action" value="createNewPlayer">
                    <input type="hidden" name="slotNumber" id="slotNumber" value="1">
                    
                    <div class="form-group">
                        <label for="playerName">Character Name</label>
                        <input type="text"
                               id="playerName"
                               name="playerName"
                               placeholder="Enter your character's name"
                               required>
                    </div>
                    
                    <div class="form-group">
                        <label>Choose a Class</label>
                        <div class="class-buttons">
                            <div class="class-button" onclick="selectClass('Warrior')" id="class-Warrior">
                                <h4>Warrior</h4>
                                <p>High HP, Strong Attack</p>
                            </div>
                            <div class="class-button" onclick="selectClass('Mage')" id="class-Mage">
                                <h4>Mage</h4>
                                <p>High Damage Multiplier, More Skill Points</p>
                            </div>
                            <div class="class-button" onclick="selectClass('Rogue')" id="class-Rogue">
                                <h4>Rogue</h4>
                                <p>Balanced Attack and Defense</p>
                            </div>
                        </div>
                        <input type="hidden" id="playerClass" name="playerClass" value="Warrior">
                    </div>
                    
                    <div class="form-group">
                        <label for="playerDescription">Character Description</label>
                        <textarea id="playerDescription"
                                  name="playerDescription"
                                  rows="4"
                                  placeholder="Describe your character"
                                  required></textarea>
                    </div>
                    
                    <button type="submit">Create Character</button>
                </form>
            </div>
        </div>
    </div>
    
    <script>
        function openNewGameModal(slotNumber) {
            document.getElementById('newGameModal').style.display = 'block';
            document.getElementById('slotNumber').value = slotNumber;
            selectClass('Warrior'); // Default class
        }
        
        function closeNewGameModal() {
            document.getElementById('newGameModal').style.display = 'none';
        }
        
        function selectClass(className) {
            document.querySelectorAll('.class-button').forEach(button => {
                button.classList.remove('selected');
            });
            document.getElementById('class-' + className).classList.add('selected');
            document.getElementById('playerClass').value = className;
        }
        
        function loadSaveSlot(playerId, currentRoom) {
            const form = document.createElement('form');
            form.method = 'post';
            form.action = '${pageContext.request.contextPath}/home';
            
            const actionInput = document.createElement('input');
            actionInput.type = 'hidden';
            actionInput.name = 'action';
            actionInput.value = 'loadSaveSlot';
            
            const playerIdInput = document.createElement('input');
            playerIdInput.type = 'hidden';
            playerIdInput.name = 'playerId';
            playerIdInput.value = playerId;
            
            const currentRoomInput = document.createElement('input');
            currentRoomInput.type = 'hidden';
            currentRoomInput.name = 'currentRoom';
            currentRoomInput.value = currentRoom;
            
            form.appendChild(actionInput);
            form.appendChild(playerIdInput);
            form.appendChild(currentRoomInput);
            
            document.body.appendChild(form);
            form.submit();
        }
        
        // Close modal if clicked outside
        window.onclick = function(event) {
            const modal = document.getElementById('newGameModal');
            if (event.target == modal) {
                closeNewGameModal();
            }
        }
    </script>
</body>
</html>
