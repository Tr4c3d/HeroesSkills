package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.ImbueEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class SkillSuperCharge extends ActiveSkill
{
	public SkillSuperCharge(Heroes plugin)
	{
		super(plugin, "SuperCharge");
		setDescription("Shoots an extra-charged arrow");
		setUsage("/skill SuperCharge");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill SuperCharge" });
		setTypes(new SkillType[] { SkillType.HARMFUL, SkillType.BUFF });

		Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
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
		hero.addEffect(new SuperChargeBuff(this));
		broadcastExecuteText(hero);
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Hero hero)
	{
		return getDescription();
	}

	public class SuperChargeBuff extends ImbueEffect
	{
		public SuperChargeBuff(Skill skill)
		{
			super(skill, "SuperChargeBuff");
			setDescription("ChargedArrow");
		}
	}

	public class SkillEntityListener implements Listener {
		private final Skill skill;
		public SkillEntityListener(Skill skill) {
			this.skill = skill;
		}

		@EventHandler(priority=EventPriority.MONITOR)
		public void onEntityShootBow(EntityShootBowEvent event) {
			if ((event.isCancelled()) || (!(event.getEntity() instanceof Player)) || (!(event.getProjectile() instanceof Arrow))) {
				return;
			}
			Hero hero = SkillSuperCharge.this.plugin.getCharacterManager().getHero((Player)event.getEntity());
			if (hero.hasEffect("SuperChargeBuff")) {        
				float multiplier = (float) (SkillConfigManager.getUseSetting(hero, this.skill, "multiplier", 2.0, false) +
						(SkillConfigManager.getUseSetting(hero, this.skill, "multiplier-increase", 0.0, false) * hero.getSkillLevel(this.skill)));
				multiplier = multiplier > 0 ? multiplier : 0;
				event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(multiplier));
				hero.removeEffect(hero.getEffect("SuperChargeBuff"));
			}
		}
	}
}