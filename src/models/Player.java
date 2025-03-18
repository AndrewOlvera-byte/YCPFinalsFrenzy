public class Player extends Character {
   private int skillPoints;
   private int location;
   // private Room location;
   public Player(String name, int hp, int skillPoints, int location) {
       super(name, hp);
       this.skillPoints = skillPoints;
       this.location = location;
   }
   public void move(String direction) {
       System.out.println(name + " moves " + direction + " to a new location.");
       // Implement movement logic based on Room class
   }
   public int getLocation() {
   	return location;
   }
   public void useSkillPoints() {
       if (skillPoints > 0) {
           skillPoints--;
           System.out.println(name + " used a skill point. Remaining: " + skillPoints);
       } else {
           System.out.println(name + " has no skill points left.");
       }
   }
}
