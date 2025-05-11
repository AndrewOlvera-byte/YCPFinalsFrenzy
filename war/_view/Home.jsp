<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head>
    <title>YCP Finals Frenzy - Home</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <style>
        .save-slots {
            display: flex;
            justify-content: space-around;
            margin: 50px auto;
            max-width: 900px;
        }
        
        .save-slot {
            border: 2px solid #ccc;
            border-radius: 10px;
            padding: 20px;
            width: 250px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s;
            background-color: #333;
            color: white;
        }
        
        .save-slot:hover {
            transform: scale(1.05);
            box-shadow: 0 0 15px rgba(255,255,255,0.3);
        }
        
        .save-slot h3 {
            margin-top: 0;
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
            background-color: #333;
            color: white;
            margin: 10% auto;
            padding: 30px;
            border-radius: 10px;
            width: 60%;
            max-width: 600px;
        }
        
        .close {
            color: #aaa;
            float: right;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
        }
        
        .close:hover {
            color: white;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 5px;
        }
        
        .form-group input, .form-group select, .form-group textarea {
            width: 100%;
            padding: 10px;
            border-radius: 5px;
            border: 1px solid #666;
            background-color: #444;
            color: white;
        }
        
        .class-buttons {
            display: flex;
            justify-content: space-between;
        }
        
        .class-button {
            padding: 10px;
            border: 1px solid #666;
            border-radius: 5px;
            cursor: pointer;
            width: 30%;
            text-align: center;
        }
        
        .class-button.selected {
            background-color: #555;
            border-color: #999;
        }
        
        button[type="submit"] {
            padding: 10px 20px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
        }
        
        button[type="submit"]:hover {
            background-color: #45a049;
        }
    </style>
</head>

<body>
    <header>
        <h1>Welcome to YCP Finals Frenzy</h1>
        <h2>Choose a save slot to begin</h2>
    </header>
    
    <main>
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
                        <label for="playerName">Character Name:</label>
                        <input type="text" id="playerName" name="playerName" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Choose a Class:</label>
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
                        <label for="playerDescription">Character Description:</label>
                        <textarea id="playerDescription" name="playerDescription" rows="4" required></textarea>
                    </div>
                    
                    <button type="submit">Create Character</button>
                </form>
            </div>
        </div>
    </main>
    
    <script>
        // Function to open new game modal
        function openNewGameModal(slotNumber) {
            document.getElementById('newGameModal').style.display = 'block';
            document.getElementById('slotNumber').value = slotNumber;
            selectClass('Warrior'); // Default class
        }
        
        // Function to close new game modal
        function closeNewGameModal() {
            document.getElementById('newGameModal').style.display = 'none';
        }
        
        // Function to select class
        function selectClass(className) {
            // Remove selected class from all buttons
            document.querySelectorAll('.class-button').forEach(button => {
                button.classList.remove('selected');
            });
            
            // Add selected class to clicked button
            document.getElementById('class-' + className).classList.add('selected');
            
            // Set hidden input value
            document.getElementById('playerClass').value = className;
        }
        
        // Function to load save slot
        function loadSaveSlot(playerId, currentRoom) {
            // Create form and submit
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
