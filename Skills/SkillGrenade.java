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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Listener;

public class SkillGrenade extends ActiveSkill implements Listener{
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");

    public SkillGrenade(Heroes plugin) {
        super(plugin, "Grenade");
        setDescription("Throws a tnt to the direction you are looking at");
        setUsage("/skill Grenade");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Grenade"});
        setTypes(SkillType.SILENCABLE, SkillType.HEAL);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //FORCE
        int force = (int) ((SkillConfigManager.getUseSetting(hero, this, "force", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "force-increase", 0.0, false) * hero.getSkillLevel(this))));
        if(hst != null) force += (SkillConfigManager.getUseSetting(hero, this, "hst-force", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        force = force > 0 ? force : 0;
        if (force > 0) {
            description += " F:" + force + "s";
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
        node.set(Setting.AMOUNT.node(), 1);
        node.set("amount-increase", 0);
        node.set("hst-force", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        double force = ((SkillConfigManager.getUseSetting(hero, this, "force", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "force-increase", 0.0, false) * hero.getSkillLevel(this))));
        if(hst != null) force += (SkillConfigManager.getUseSetting(hero, this, "hst-force", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        force = force > 0 ? force : 0;
        TNTPrimed tnt = hero.getPlayer().getWorld().spawn(hero.getPlayer().getEyeLocation(), TNTPrimed.class);
        tnt.setVelocity(hero.getPlayer().getLocation().getDirection().normalize().multiply(force));
    	return SkillResult.NORMAL;
    }
}