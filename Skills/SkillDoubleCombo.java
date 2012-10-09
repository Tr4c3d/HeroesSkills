package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.util.Setting;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.configuration.ConfigurationSection;

public class SkillDoubleCombo extends PassiveSkill implements Listener{
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");

    public SkillDoubleCombo(Heroes plugin) {
        super(plugin, "DoubleCombo");
        setDescription("Passive $1 chance of having $2 after attacking the enemy to deal a second stronger attack");
        setUsage("/skill DoubleCombo");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill DoubleCombo"});
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.PHYSICAL, SkillType.HARMFUL);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //MULTIPLIER
        int multiplier = (int) (SkillConfigManager.getUseSetting(hero, this, "multiplier", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "multiplier-increase", 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) multiplier += (SkillConfigManager.getUseSetting(hero, this, "hst-multiplier", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        multiplier = multiplier > 0 ? multiplier : 0;
        if(multiplier > 0) {
        	description += " D:" + multiplier;
        }
        
        //CHANCE
        double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this))) * 100;
        if(hst != null) chance += (SkillConfigManager.getUseSetting(hero, this, "hst-chance", 0.0, false) * (hst.getSkillLevel(hero, this) - 1) * 100);
        chance = chance > 0 ? chance : 0;
        if(chance > 0){
        	description = description.replace("$1", chance + "%");
        }
        
        //DURATION
        double duration = (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 1000, false) +
        		(SkillConfigManager.getUseSetting(hero, this, Setting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(this)));
        if(hst != null) duration += SkillConfigManager.getUseSetting(hero, this, "hst-duration", 0, false) * (hst.getSkillLevel(hero, this) - 1);
        duration = duration > 0 ? duration : 0;
        if(duration > 0){
        	description = description.replace("$2", duration + "ms");
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
        node.set("multiplier", 2);
        node.set("multiplier-increase", 0);
        node.set("hst-multiplier", 0);
        node.set(Setting.CHANCE.node(), 1);
        node.set(Setting.CHANCE_LEVEL.node(), 0);
        node.set("hst-chance", 0);
        node.set(Setting.DURATION.node(), 1000);
        node.set(Setting.DURATION_INCREASE.node(), 0);
        node.set("hst-duration", 0);
        return node;
    }
    
    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent event){
    	if(event.getDamager() instanceof Hero){
    		Hero hero = (Hero) event.getDamager();
    		if(hero.hasEffect("DCombo")){
    			int multiplier = (int) (SkillConfigManager.getUseSetting(hero, this, "multiplier", 1.0, false) +
                        (SkillConfigManager.getUseSetting(hero, this, "multiplier-increase", 0.0, false) * hero.getSkillLevel(this)));
                if(hst != null) multiplier += (SkillConfigManager.getUseSetting(hero, this, "hst-multiplier", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
                multiplier = multiplier > 0 ? multiplier : 0;
                event.setDamage((int) (event.getDamage() * multiplier));
                Messaging.broadcast(plugin, hero.getName() + " double combles!");
                if(hero.hasEffect("ComboChain")){
                	int duration = (int) (SkillConfigManager.getUseSetting(hero, plugin.getSkillManager().getSkill("ComboChain"), Setting.DURATION, 1000, false) +
	    	        		(SkillConfigManager.getUseSetting(hero, plugin.getSkillManager().getSkill("ComboChain"), Setting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(plugin.getSkillManager().getSkill("ComboChain"))));
	    	        if(hst != null) duration += (SkillConfigManager.getUseSetting(hero, plugin.getSkillManager().getSkill("ComboChain"), "hst-duration", 0, false) * (hst.getSkillLevel(hero, plugin.getSkillManager().getSkill("ComboChain")) - 1));
	    	        duration = duration > 0 ? duration : 0;
                	hero.addEffect(new ExpirableEffect(plugin.getSkillManager().getSkill("ComboChain"), "CChain", duration));
                }
                hero.removeEffect(hero.getEffect("Dcombo"));
    		}
    		if(hero.hasEffect("DoubleCombo") && !hero.hasEffect("DCombo") && !hero.hasEffect("CChain") && !hero.hasEffect("ComboC") && !hero.hasEffect("CFinal") && !hero.hasEffect("ComboF")){
    			double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.2, false) +
                        (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this)));
                if(hst != null) chance += (SkillConfigManager.getUseSetting(hero, this, "hst-chance", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
                chance = chance > 0 ? chance : 0;
    	        if(Math.random() <= chance){
	    			int duration = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 1000, false) +
	    	        		(SkillConfigManager.getUseSetting(hero, this, Setting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(this)));
	    	        if(hst != null) duration += (SkillConfigManager.getUseSetting(hero, this, "hst-duration", 0, false) * (hst.getSkillLevel(hero, this) - 1));
	    	        duration = duration > 0 ? duration : 0;
	    			hero.addEffect(new ExpirableEffect(this, "DCombo", duration));
    	        }
    		}
    	}
    }
}