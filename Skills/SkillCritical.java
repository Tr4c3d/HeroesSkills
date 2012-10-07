package com.herocraftonline.heroes.characters.skill.skills;


import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkillCritical extends PassiveSkill {
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");

    public SkillCritical(Heroes plugin) {
        super(plugin, "Critical");
        setDescription("Passive $1% chance to do $2 times more damage.");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
    }

    @Override
    public String getDescription(Hero hero) {
        double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this))) * 100;
        if(hst != null) chance += (SkillConfigManager.getUseSetting(hero, this, "hst-chance", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        chance = chance > 0 ? chance : 0;
        double damageMod = (SkillConfigManager.getUseSetting(hero, this, "damage-multiplier", 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-multiplier-increase", 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) damageMod += (SkillConfigManager.getUseSetting(hero, this, "hst-multiplier", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        damageMod = damageMod > 0 ? damageMod : 0;
        String description = getDescription().replace("$1", chance + "").replace("$2", damageMod + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.CHANCE.node(), 0.2);
        node.set(Setting.CHANCE_LEVEL.node(), 0);
        node.set("hst-chance", 0);
        node.set("damage-multiplier", 2.0);
        node.set("damage-multiplier-increase", 0);
        node.set("hst-multiplier", 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler
        public void onEntityDamage(WeaponDamageEvent event) {
        	if(!(event.isCancelled())&&(event.getDamager() instanceof Hero)){
        		Hero hero = (Hero) event.getDamager();
           	  	if (hero.hasEffect("Critical")) {
                    double chance = (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) +
                            (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(skill)));
                    if(hst != null) chance += (SkillConfigManager.getUseSetting(hero, skill, "hst-chance", 0.0, false) * (hst.getSkillLevel(hero, skill) - 1));
                    chance = chance > 0 ? chance : 0;
                    if (Math.random() <= chance) {
                        double damageMod = (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier", 0.2, false) +
                                (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier-increase", 0.0, false) * hero.getSkillLevel(skill)));
                        if(hst != null) damageMod += (SkillConfigManager.getUseSetting(hero, skill, "hst-multiplier", 0.0, false) * (hst.getSkillLevel(hero, skill) - 1));
                        damageMod = damageMod > 0 ? damageMod : 0;
                        event.setDamage((int) (event.getDamage() * damageMod));
                    }
           	  	}
        	}
        }
    }
}