package GameEngineTest;
import GameEngine.GameEngine;
public class GameEngineMockRun
{
	public static void main(String[] args)
	{
		GameEngine gameEngine = new GameEngine();
		gameEngine.start();
		
		System.out.println(gameEngine.getCurrentRoomNum());
		gameEngine.updateCurrentRoom("East");
		System.out.println(gameEngine.getCurrentRoomNum());
		
		System.out.println(gameEngine.getPlayerInventoryString());
		gameEngine.pickupItem(0);
		System.out.println(gameEngine.getCurrentRoomItems());
		System.out.println(gameEngine.getPlayerInventoryString());
		
		System.out.println(gameEngine.getPlayerInfo());
		
		gameEngine.updateCurrentRoom("North");
		
		System.out.println(gameEngine.getCurrentRoomNum() + "\n");

		System.out.println(gameEngine.getRoomCharactersInfo());
		
		gameEngine.charAttackPlayer(0, 0, true);
		gameEngine.playerAttackChar(0, 0);
		gameEngine.dropItem(1);
		
		System.out.println(gameEngine.getPlayerInventoryString());
		
		System.out.println(gameEngine.getCurrentRoomItems());
		
		System.out.println("\n\n");
		System.out.println(gameEngine.getPlayerInfo());
		System.out.println(gameEngine.getRoomCharactersInfo());
	}
}