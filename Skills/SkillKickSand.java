package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.util.Setting;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.configuration.ConfigurationSection;

public class SkillKickSand extends TargettedSkill{
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");

    public SkillKickSand(Heroes plugin) {
        super(plugin, "KickSand");
        setDescription("Kicks surrounding sand at enemy, dealing Earth damage");
        setUsage("/skill KickSand");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill KickSand"});
        
        setTypes(SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.EARTH, SkillType.HARMFUL);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //DAMAGE
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) damage += (SkillConfigManager.getUseSetting(hero, this, "hst-damage", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
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
        if(hst != null) radius += (SkillConfigManager.getUseSetting(hero, this, "hst-radius", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
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
        node.set("hst-damage", 0);
        node.set(Setting.RADIUS.node(), 1);
        node.set(Setting.RADIUS_INCREASE.node(), 0);
        node.set("hst-radius", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target,String[] args) {
        Player player = hero.getPlayer();
        if (!damageCheck(player, target)) {
            Messaging.send(player, "You can't harm that target");
            return SkillResult.INVALID_TARGET;
        }
        
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) damage += (SkillConfigManager.getUseSetting(hero, this, "hst-damage", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        damage = damage > 0 ? damage : 0;
        int radius = SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 1, false) + 
        		(SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0, false)) * hero.getSkillLevel(this);
        if(hst != null) radius += (SkillConfigManager.getUseSetting(hero, this, "hst-radius", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        radius = radius > 1 ? radius : 1;
	    
        if(player.getWorld().getBlockTypeIdAt(player.getLocation().getBlockX(), player.getLocation().getBlockY()-1, player.getLocation().getBlockZ()) == 12){
	        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
	            if (entity == target) {
			        if(damage > 0) {
			        	damageEntity(target, player, damage, DamageCause.CUSTOM);
			        	player.getLocation().getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 12);
			            broadcastExecuteText(hero, target);
			        }
	            }
	        }
        }
        else {
        	Messaging.send(player, "You must be on sand to use this skill");
        	return SkillResult.CANCELLED;
        }
        return SkillResult.NORMAL;
    }
}