package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.util.Setting;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.configuration.ConfigurationSection;

public class SkillLunge extends TargettedSkill{

    public SkillLunge(Heroes plugin) {
        super(plugin, "Lunge");
        setDescription("Lunges the enemy with a spade and pushes it a few blocks behind");
        setUsage("/skill Lunge");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Lunge"});
        
        setTypes(SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.PHYSICAL, SkillType.HARMFUL);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //DAMAGE
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        if(damage > 0) {
        	description += " D:" + damage;
        }
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //RADIUS
        int radius = SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 1, false) + 
        		(SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0, false)) * hero.getSkillLevel(this);
        radius = radius > 1 ? radius : 1;
        description += " R:" + radius;
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.DAMAGE.node(), 5);
        node.set(Setting.DAMAGE_INCREASE.node(), 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (!damageCheck(player, target)) {
            Messaging.send(player, "You can't harm that target");
            return SkillResult.INVALID_TARGET;
        }
        
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
	    
        if((player.getItemInHand().getType().equals(Material.WOOD_SPADE)) ||
        		(player.getItemInHand().getType().equals(Material.STONE_SPADE)) ||
        		(player.getItemInHand().getType().equals(Material.IRON_SPADE)) ||
        		(player.getItemInHand().getType().equals(Material.GOLD_SPADE)) ||
        		(player.getItemInHand().getType().equals(Material.DIAMOND_SPADE))){
        	if(damage > 0){
        		damageEntity(target, player, damage, DamageCause.ENTITY_ATTACK);
        		knockBack(target, player, 0);
        		broadcastExecuteText(hero, target);
        	}
        }
        else {
        	Messaging.send(player, "You must be holding a spade to use this skill");
        	return SkillResult.CANCELLED;
        }
        return SkillResult.NORMAL;
    }
}