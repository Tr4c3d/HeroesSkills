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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

public class SkillRetrieveArrow extends PassiveSkill {
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");

    public SkillRetrieveArrow(Heroes plugin) {
        super(plugin, "RetrieveArrow");
        setDescription("Passive $1% chance to retrieve killing arrow");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
    }

    @Override
    public String getDescription(Hero hero) {
        double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this))) * 100;
        if(hst != null) chance += (SkillConfigManager.getUseSetting(hero, this, "hst-chance", 0.0, false) * (hst.getSkillLevel(hero, this) - 1) * 100);
        chance = chance > 0 ? chance : 0;
        String description = getDescription().replace("$1", chance + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.CHANCE.node(), 0.2);
        node.set(Setting.CHANCE_LEVEL.node(), 0);
        node.set("hst-chance", 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler(priority=EventPriority.HIGH)
        public void onEntityDamage(WeaponDamageEvent event) {
        	if(!(event.isCancelled())&&(event.getDamager() instanceof Hero)&&(event.getCause().equals(DamageCause.PROJECTILE))){
        		Hero hero = (Hero) event.getDamager();
           	  	if (hero.hasEffect("RetrieveArrow")) {
                    double chance = (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) +
                            (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(skill)));
                    if(hst != null) chance += (SkillConfigManager.getUseSetting(hero, skill, "hst-amount", 0.0, false) * (hst.getSkillLevel(hero, skill) - 1));
                    chance = chance > 0 ? chance : 0;
                    if (Math.random() <= chance) {
                    	if(event.getEntity() instanceof Player){
                    		if(SkillRetrieveArrow.this.plugin.getCharacterManager().getHero((Player)event.getEntity()).getHealth() < event.getDamage()){
                    			event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(262, 1));
                    		}
                    	}
                    	else if(event.getEntity() instanceof LivingEntity){
	                    	if(SkillRetrieveArrow.this.plugin.getCharacterManager().getMonster((LivingEntity)event.getEntity()).getHealth() < event.getDamage()){
	                    		event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(262, 1));
	                    	}
                    	}
                    }
           	  	}
        	}
        }
    }
}