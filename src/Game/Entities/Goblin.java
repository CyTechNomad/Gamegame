package Game.Entities;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Goblin extends Enemy {
    private static final List<String> greetingMsgs = Arrays.asList(
            "Ooo, fresh meat!",
            "Got a death wish, do ya?",
            "You look like you got somethin' shiny!");
    private static final List<String> victoryMsgs = Arrays.asList(
            "I.. I did it! I'm rich! I'm really rich! Hehehe!",
            "You're not as tough as you thought, huh?",
            "Too easy! Now where's the gold?");
    private static final List<String> defeatMsgs = Arrays.asList(
            "AAAAAA! Too strong!",
            "You're tougher than you look...",
            "Owww! Okay, I give up!");
    private static final List<String> attackMsgs = Arrays.asList(
            "Hehehehe!",
            "Grrr...",
            "You're meat to my blade!");
    private final Random rand = new Random();

    public Goblin() {
        this.setName("Goblin");
        this.setMaxHealth(100);
        this.setCurrentHealth(100);
        this.setCoinReward(20);
        this.setSprite("src/Sprites/goblin.png");
        this.setStat("low");
        this.setAttack(this.getStat());
        this.setStat("low");
        this.setDefense(this.getStat());
        this.setStat("low");
        this.setMagicPoints(this.getStat());
    }

    public String getGreeting() {
        return (String)greetingMsgs.get(this.rand.nextInt(greetingMsgs.size()));
    }


    public String getVictoryMsg() {
        return (String)victoryMsgs.get(this.rand.nextInt(victoryMsgs.size()));
    }


    public String getDefeatMsg() {
        return (String)defeatMsgs.get(this.rand.nextInt(defeatMsgs.size()));
    }

    public String getAttackMsg() {
        return (String)attackMsgs.get(this.rand.nextInt(attackMsgs.size()));
    }
}
