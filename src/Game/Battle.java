package Game;

import GUI.Panels.BattlePanel;
import GUI.Panels.EntityPanel;
import Game.Entities.*;
import Game.Music.MusicPlayer;

import javax.swing.*;
import java.util.Random;

public class Battle
{
    private Player player;
    private Enemy enemy;
    private MusicPlayer soundPlayer;
    private BattlePanel battlePanel;
    private EntityPanel enemyPanel;


    public Battle(Player player, Enemy enemy)
    {
        this.player = player;
        this.enemy = enemy;
        this.soundPlayer = new MusicPlayer();
    }

    public static boolean isCriticalHit()
    {
        Random random = new Random();

        // Generate a number between 0 and 99
        int chance = random.nextInt(100);

        return chance < 15; // 15% chance for a critical hit
    }

    public static boolean isWastedTurn(int turnwastechance)
    {
        Random random = new Random();

        // Generate a number between 0 and 99
        int chance = random.nextInt(100);

        return chance < turnwastechance;
    }

   private void setAttackBattleText(Entity attacker, Attack attack) {
        String msg = attacker.getName() + " used " + attack.getName() + "!";

        if (attacker instanceof Enemy){
            msg = "\"" + ((Enemy) attacker).getAttackMsg()+ "\" " + msg;
        }

        battlePanel.getBattleLog().setText(msg);
    }

    private int calcDamage (Entity attacker, Entity defender, Attack attack) throws ExecutionControl.NotImplementedException {
        Random random = new Random();

        // Critical multiplier
        int critMultiplier = 1;

        boolean isCritical = isCriticalHit()
        // Set critMultiplier if the attack was a crit or charged
        if (isCritical || attacker.isCharging()) {
            critMultiplier = 2;
            attacker.setCharging(false);
        }
        if (isCritical){

            battlePanel.getBattleLog().setText(battlePanel.getBattleLog().getText() + " Critical Hit!");
        }

        // Random factor between ~86% - 100%
        double damageScalar = (220 + random.nextInt(36)) / 255.0;

        // Determine the stat to be used for damage/defense calculations
        int attackerAttack;
        int defenderDefense;
        String soundEffectFileName;

        switch (attack.getType()) {
            case "Physical":
                attackerAttack = attacker.getAttack();
                defenderDefense = defender.getDefense();
                soundEffectFileName = "src/Game/Music/physicalattack.wav";
                break;
            case "Magic":
                attackerAttack = attacker.getMagic();
                defenderDefense = defender.getMagic();
                soundEffectFileName = "src/Game/Music/magicattack.wav";
                break;
            default:
                // we Do this to avoid a divide by zero exception
                throw new ExecutionControl.NotImplementedException("UnExpected Attack Type");
        }

        Timer soundEffectTimer = new Timer(100, e ->
        {
            this.soundPlayer.play(soundEffectFileName);
        });
        soundEffectTimer.setRepeats(false);
        soundEffectTimer.start();


        // Damage formula
        int baseDamage =(2 * attacker.getLevel() * critMultiplier + 4);
        double defensiveFactor = (double)attackerAttack / defenderDefense;
        int damage = (int) (((double) (baseDamage * attack.getPower() * defensiveFactor) / 50) + 2);
        return (int)(damage * damageScalar);
    }

    public void attack(Entity attacker, Entity defender, Attack attack) throws ExecutionControl.NotImplementedException {
        setAttackBattleText(attacker, attack);
        JTextField battleLog = battlePanel.getBattleLog();

        // Reduce the use counter.
        attack.setUses(attack.getUses() - 1);
        if (attack.getUses() < 0) {
            attack.setUses(0);
        }

        // attacker must drop guard to attack
        if (attacker.isBlocking()) {
            battleLog.setText(attacker.getName() + " drops their guard.");
            attacker.setBlocking(false);
        }

        // if defender is blocking no damage is calculated
        if (defender.isBlocking()) {
            battleLog.setText(battleLog.getText() + " " + defender.getName() +" blocked the attack.");

            // once defender blocks the hit they are no longer blocking
            defender.setBlocking(false);
            return;
        }

        int damage = calcDamage(attacker, defender, attack);

        EntityPanel bp;
        if (attacker instanceof Player) {
            bp = battlePanel.getEnemyPanel();
        }
        else {
            bp = battlePanel.getPlayerPanel();
        }
        bp.dropHealthBar(defender.getCurrentHealth() - damage, defender);

        battleLog.setText(battleLog.getText() + " " + defender.getName() + " takes " + damage + " damage!");

        // Deal the damage
        defender.setCurrentHealth(defender.getCurrentHealth() - damage);
    }


    public void determineEnemyAction()
    {
        Random random = new Random();
        
        if(!isWastedTurn(enemy.getTurnWasteChance()))
        {
            // Generate a number between 0 and 99
            int chance = random.nextInt(100);

            //30% chance to use an action. I'm not making this AI very smart. It just does shit randomly.
            if (chance < 30)
            {
                int actionindex = random.nextInt(enemy.getActionCount());
                enemyAction(enemy.getAction(actionindex));
            }
            
            //Attack otherwise.
            else
            {
                int attackindex = random.nextInt(enemy.getAttackCount());
                attack(enemy,player,enemy.getAttackMove(attackindex));
            }
        }

        else
        {
            stall();
        }
    }

    public void stall()
    {
        battlePanel.getBattleLog().setText("The " + enemy.getName() +" taunts you. \"" + enemy.getTauntMsg()+ "\"");
    }

    public void playerAction(Action action)
    {

        if (action.getAction().equals("Guard"))
        {
            player.setBlocking(true);
            battlePanel.getBattleLog().setText(player.getName() + " raises their guard.");

        }
        else if (action.getAction().equals("Healing Potion"))
        {
            if (action.getUses() > 0)
            {
                //Determine the difference between the player's current health and their max health.
                int difference = player.getMaxHealth() - player.getCurrentHealth();

                //If you have taken damage...
                if (difference > 0) {
                    //Determine if recovering 50 health will make you go over your max hp.
                    if (difference > 50) {
                        difference = 50;
                    }

                    //Then heal at most 50 health.

                    battlePanel.getPlayerPanel().dropHealthBar(player.getCurrentHealth() + difference, player);
                    battlePanel.getBattleLog().setText(player.getName() + " chugs a potion... and heals " + difference + " health.");
                } else {
                    battlePanel.getBattleLog().setText(player.getName() + " chugs a potion... but nothing happens.");
                }

                action.setUses(action.getUses() - 1);
                if (action.getUses() < 0)
                {
                    action.setUses(0);
                }
            }
            else
            {
                battlePanel.getBattleLog().setText(player.getName() + " reaches for a potion, but turns up empty.");
            }
        }
    }

    public void enemyAction(Action action)
    {
        if (action.getAction().equals("Charge"))
        {
            if (enemy.isCharging())
            {
                Random random = new Random();
                int attackindex = random.nextInt(enemy.getAttackCount());
                
                attack(enemy, player, enemy.getAttackMove(attackindex));
            }
            else
            {
                enemy.setCharging(true);
                battlePanel.getBattleLog().setText(enemy.getName() +" draws back, and appears to begin preparing a powerful attack.");


                action.setUses(action.getUses() - 1);
                if (action.getUses() < 0)
                {
                    action.setUses(0);
                }
            }
        }
        else if (action.getAction().equals("Block"))
        {
            enemy.setBlocking(true);
            battlePanel.getBattleLog().setText("The " + enemy.getName() +" raises their guard.");

        }
        else if (action.getAction().equals("Stall"))
        {
            stall();
        }
        else if (action.getAction().equals("Heal"))
        {
            if (action.getUses() > 0) {
                //Determine the difference between the enemy's current health and their max health.
                int difference = enemy.getMaxHealth() - enemy.getCurrentHealth();

                //If they have taken damage...
                if (difference > 0) {
                    //Determine if recovering 50 health will make them go over their max hp.
                    if (difference > 50) {
                        difference = 50;
                    }

                    //Then heal at most 50 health.

                    battlePanel.getEnemyPanel().dropHealthBar(enemy.getCurrentHealth() + difference, enemy);
                    battlePanel.getBattleLog().setText("The " + enemy.getName() + " casts a healing spell... and recovers " + difference + " health.");

                    action.setUses(action.getUses() - 1);
                    if (action.getUses() < 0)
                    {
                        action.setUses(0);
                    }
                } else {
                    //Otherwise, why heal? Do something else.
                    determineEnemyAction();
                }
            }
        }
    }

    public boolean winConditionMet()
    {
        if (enemy.getCurrentHealth() <= 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean loseConditionMet()
    {
        if (player.getCurrentHealth() <= 0)
        {
            for (Gear gear : player.getGear())
            {
                if (gear.getName().equals("Mysterious Liquid"))
                {
                    player.removeGear(gear.getName());

                    int difference = player.getMaxHealth() - player.getCurrentHealth();

                    battlePanel.getPlayerPanel().dropHealthBar(player.getCurrentHealth() + difference, player);
                    battlePanel.getBattleLog().setText(player.getName() + " drops... but is brought back by a Mysterious Liquid.");
                    return false;
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }


    public Enemy getEnemy()
    {
        return this.enemy;
    }

    public Player getPlayer()
    {
        return player;
    }

    public void setBattlePanel(BattlePanel battlePanel)
    {
        this.battlePanel = battlePanel;
    }
}
