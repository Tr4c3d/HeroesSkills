package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.ImbueEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;

public class SkillSharpShot extends ActiveSkill{
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");
	
	public SkillSharpShot(Heroes plugin)
	{
		super(plugin, "SharpShot");
		setDescription("Shoots a sharp arrow that deals percentage damage and has chance to stun.");
		setUsage("/skill SharpShot");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill SharpShot" });
		setTypes(new SkillType[] { SkillType.HARMFUL, SkillType.BUFF });

		Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
	}

	@Override
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(Setting.AMOUNT.node(), 0.2);
		node.set("amount-increase", 0.0);
		node.set("hst-amount", 0);
		node.set(Setting.CHANCE.node(), 0.1);
		node.set(Setting.CHANCE_LEVEL.node(), 0.0);
		node.set("hst-chance", 0);
		node.set(Setting.DURATION.node(), 1000);
		node.set(Setting.DURATION_INCREASE.node(), 0);
		node.set("hst-duration", 0);
		return node;
	}

	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		hero.addEffect(new SharpShotBuff(this));
		broadcastExecuteText(hero);
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Hero hero)
	{
		return getDescription();
	}

	public class SharpShotBuff extends ImbueEffect
	{
		public SharpShotBuff(Skill skill)
		{
			super(skill, "SharpShotBuff");
			setDescription("SharpShot");
		}
	}

	public class SkillEntityListener implements Listener {
		private final Skill skill;
		public SkillEntityListener(Skill skill) {
			this.skill = skill;
		}
		
		@EventHandler(priority=EventPriority.HIGHEST)
		public void onEntityDamage(WeaponDamageEvent event){
			if((!(event.isCancelled()))&&(event.getCause().equals(DamageCause.PROJECTILE))){
				if(event.getDamager() instanceof Hero){
					Hero hero = (Hero) event.getDamager();
					float amount = (float) (SkillConfigManager.getUseSetting(hero, this.skill, Setting.AMOUNT.node(), 0.2, false) +
							(SkillConfigManager.getUseSetting(hero, this.skill, "amount-increase", 0.0, false) * hero.getSkillLevel(this.skill)));
					if(hst != null) amount += (SkillConfigManager.getUseSetting(hero, skill, "hst-amount", 0.0, false) * (hst.getSkillLevel(hero, skill) - 1));
					amount = amount > 0 ? amount : 0;
					float chance = (float) (SkillConfigManager.getUseSetting(hero, this.skill, Setting.CHANCE.node(), 0.1, false) +
							(SkillConfigManager.getUseSetting(hero, this.skill, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this.skill)));
					if(hst != null) chance += (SkillConfigManager.getUseSetting(hero, skill, "hst-chance", 0.0, false) * (hst.getSkillLevel(hero, skill) - 1));
					chance = chance > 0 ? chance : 0;
					if(hero.hasEffect("SharpShotBuff")){
						if(event.getEntity() instanceof LivingEntity){
							int duration = (SkillConfigManager.getUseSetting(hero, this.skill, Setting.DURATION.node(), 1000, false) +
									(SkillConfigManager.getUseSetting(hero, this.skill, Setting.DURATION_INCREASE.node(), 0, false) * hero.getSkillLevel(this.skill)));
							if(hst != null) duration += (SkillConfigManager.getUseSetting(hero, skill, "hst-duration", 0.0, false) * (hst.getSkillLevel(hero, skill) - 1));
							if(event.getEntity() instanceof Player){
								Hero thero = SkillSharpShot.this.plugin.getCharacterManager().getHero((Player)event.getEntity());
								if(Math.random() < chance){
									thero.addEffect(new StunEffect(this.skill, duration));
									event.setDamage((thero.getMaxHealth()*amount*2) > event.getDamage() ? ((int)(thero.getMaxHealth()*amount*2)) : event.getDamage());
								}
								else{
									event.setDamage((thero.getMaxHealth()*amount) > event.getDamage() ? ((int)(thero.getMaxHealth()*amount)) : event.getDamage());
								}
							}
							else{
								if(Math.random() < chance){
									event.setDamage((int) (SkillSharpShot.this.plugin.getCharacterManager().getMonster((LivingEntity)event.getEntity()).getMaxHealth()*amount*2) > event.getDamage()/5 ?
											((int) (SkillSharpShot.this.plugin.getCharacterManager().getMonster((LivingEntity)event.getEntity()).getMaxHealth()*amount*2)) : event.getDamage()/5);
								}
								else{
									event.setDamage((int) (SkillSharpShot.this.plugin.getCharacterManager().getMonster((LivingEntity)event.getEntity()).getMaxHealth()*amount) > event.getDamage()/5 ?
											((int) (SkillSharpShot.this.plugin.getCharacterManager().getMonster((LivingEntity)event.getEntity()).getMaxHealth()*amount)) : event.getDamage()/5);
								}
							}
							event.setDamage(event.getDamage()/5 > 1 ? event.getDamage()/5 : 1);
						}
						hero.removeEffect(hero.getEffect("SharpShotBuff"));
					}
				}
			}
		}

		@EventHandler(priority=EventPriority.MONITOR)
		public void onEntityShootBow(EntityShootBowEvent event) {
			if ((event.isCancelled()) || (!(event.getEntity() instanceof Player)) || (!(event.getProjectile() instanceof Arrow))) {return;}
			Hero hero = SkillSharpShot.this.plugin.getCharacterManager().getHero((Player)event.getEntity());
			if (hero.hasEffect("SharpShotBuff")) {
				event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(5));
			}
		}
	}
}