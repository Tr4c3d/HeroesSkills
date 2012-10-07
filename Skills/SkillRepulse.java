package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.configuration.ConfigurationSection;

public class SkillRepulse extends ActiveSkill{
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");

    public SkillRepulse(Heroes plugin) {
        super(plugin, "Repulse");
        setDescription("Pushes target away");
        setUsage("/skill Repulse");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Repulse"});
        
        setTypes(SkillType.SILENCABLE, SkillType.FORCE, SkillType.MOVEMENT, SkillType.PHYSICAL);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //FORCE
        int force = (int) (SkillConfigManager.getUseSetting(hero, this, "force", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "force-increase", 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) force += (SkillConfigManager.getUseSetting(hero, this, "hst-force", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        force = force > 1 ? force : 1;
        if(force > 1) {
        	description += " F:" + force;
        }
        else force = 1;
        
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
        node.set("force", 5);
        node.set("force", 0);
        node.set("hst-force", 0);
        node.set(Setting.RADIUS.node(), 1);
        node.set(Setting.RADIUS_INCREASE.node(), 0);
        node.set("hst-radius", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        int force = (int) (SkillConfigManager.getUseSetting(hero, this, "force", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "force-increase", 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) force += (SkillConfigManager.getUseSetting(hero, this, "hst-force", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        force = force > 1 ? force : 1;
        int radius = SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 1, false) + 
        		(SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0, false)) * hero.getSkillLevel(this);
        if(hst != null) radius += (SkillConfigManager.getUseSetting(hero, this, "hst-force", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        radius = radius > 1 ? radius : 1;
	    
	    for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
			if(force > 0) {
			    player.getLocation().getWorld().playEffect(player.getLocation(), Effect.SMOKE, 1);
			    Vector v = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(force);
			    entity.setVelocity(v);
			    broadcastExecuteText(hero);
	        }
	    }
        return SkillResult.NORMAL;
    }
}