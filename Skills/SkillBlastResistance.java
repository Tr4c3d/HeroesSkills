package com.herocraftonline.heroes.characters.skill.skills;


import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillBlastResistance extends PassiveSkill {
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");

    public SkillBlastResistance(Heroes plugin) {
        super(plugin, "BlastResistance");
        setDescription("Passive $1% reduction of all explosive damage.");
        setTypes(SkillType.BUFF);
        
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
    }

    @Override
    public String getDescription(Hero hero) {
        int level = hero.getSkillLevel(this);
        double amount = (SkillConfigManager.getUseSetting(hero, this, Setting.AMOUNT.node(), 0.25, false) + 
                (SkillConfigManager.getUseSetting(hero, this, "amount-increase", 0.0, false) * level)) * 100;
        if(hst != null) amount += (SkillConfigManager.getUseSetting(hero, this, "hst-amount", 0.0, false) * (hst.getSkillLevel(hero, this) - 1) * 100);
        amount = amount > 0 ? amount : 0;
        String description = getDescription().replace("$1", amount + "");
        
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.AMOUNT.node(), 0.25);
        node.set("amount-increase", 0.0);
        node.set("hst-amount", 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
        	if(!(event.isCancelled())&&((event.getCause().equals(DamageCause.ENTITY_EXPLOSION))||(event.getCause().equals(DamageCause.BLOCK_EXPLOSION)))&&(event.getEntity() instanceof Player)){
        		Player player = (Player) event.getEntity();
        		Hero hero = plugin.getCharacterManager().getHero(player);
           	  	if (hero.hasEffect("BlastResistance")) {
           	        int level = hero.getSkillLevel(this.skill);
           	        double amount = (SkillConfigManager.getUseSetting(hero, this.skill, Setting.AMOUNT.node(), 0.25, false) + 
           	                (SkillConfigManager.getUseSetting(hero, this.skill, "amount-increase", 0.0, false) * level));
           	     if(hst != null) amount += (SkillConfigManager.getUseSetting(hero, skill, "hst-amount", 0.0, false) * (hst.getSkillLevel(hero, skill) - 1));
           	        amount = amount > 0 ? amount : 0;
                    event.setDamage((int) (event.getDamage() * (1 - amount)));
           	  	}
        	}
        }
    }
}