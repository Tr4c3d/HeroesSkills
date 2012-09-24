package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;

public class SkillSuperCharge extends ActiveSkill
{
	public SkillSuperCharge(Heroes plugin)
	{
		super(plugin, "SuperCharge");
		setDescription("Shoots an extra charged arrow");
		setUsage("/skill SuperCharge");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill SuperCharge" });
		setTypes(new SkillType[] { SkillType.HARMFUL });
	}
	
	@Override
    public String getDescription(Hero hero)
    {
    	return getDescription();
    }

    @Override
    public ConfigurationSection getDefaultConfig()
    {
    	ConfigurationSection node = super.getDefaultConfig();
    	node.set("multiplier", 2.0);
    	node.set("multiplier-increase", 0.0);
    	return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args)
    {
    	if(!hero.getPlayer().getInventory().contains(261)){
    		Messaging.send(hero.getPlayer(), "Must have a bow on inventory to use this skill!");
    		return SkillResult.CANCELLED;
    	}
    	else{
    		broadcastExecuteText(hero);
    		float multiplier = (float) (SkillConfigManager.getUseSetting(hero, this, "multiplier", 2.0, false) +
    				(SkillConfigManager.getUseSetting(hero, this, "multiplier-increase", 0.0, false) * hero.getSkillLevel(this)));
    		multiplier = multiplier > 0 ? multiplier : 0;
    		Arrow arrow = hero.getPlayer().launchProjectile(Arrow.class);
    		arrow.setVelocity(arrow.getVelocity().normalize().multiply(multiplier));
    		return SkillResult.NORMAL;
    	}
    }
}