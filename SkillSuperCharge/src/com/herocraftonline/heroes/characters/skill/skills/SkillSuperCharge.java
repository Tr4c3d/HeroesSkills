package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.common.ImbueEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

public class SkillSuperCharge extends ActiveSkill
{
  public SkillSuperCharge(Heroes plugin)
  {
    super(plugin, "SuperCharge");
    setDescription("Shoots an extra charged arrow");
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
    node.set(Setting.DAMAGE.node(), 5);
    node.set(Setting.DAMAGE_INCREASE.node(), 0);
    node.set(Setting.DURATION.node(), 1000);
    node.set("mana-per-shot", 1);
    node.set("attacks", 1);
    return node;
  }

    @Override
  public SkillResult use(Hero hero, String[] args)
  {
    long duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 1000, false);
    int numAttacks = SkillConfigManager.getUseSetting(hero, this, "attacks", 1, false);
    hero.addEffect(new SuperChargeBuff(this, duration, numAttacks));
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
    public SuperChargeBuff(Skill skill, long duration, int numAttacks)
    {
      super(skill, "SuperChargeBuff");
      this.types.add(EffectType.HARMFUL);
      setDescription("Super Charge!");
    }
  }

  public class SkillEntityListener implements Listener {
    private final Skill skill;

    public SkillEntityListener(Skill skill) {
      this.skill = skill;
    }

    @EventHandler
    public void onEntityDamage(WeaponDamageEvent event) {

      if(!(event.isCancelled())&&(event.getDamager() instanceof Hero)){
    	  Hero hero = (Hero) event.getDamager();
    	  if (hero.hasEffect("SuperChargeBuff")) {
    		  int damage = (int) (SkillConfigManager.getUseSetting(hero, this.skill, Setting.DAMAGE.node(), 5.0, false) +
    	              (SkillConfigManager.getUseSetting(hero, this.skill, Setting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this.skill)));
    	      damage = damage > 0 ? damage : 0;
    		  event.setDamage(damage);
    		  hero.getPlayer().sendMessage("hello");
    		  hero.removeEffect(hero.getEffect("SuperChargeBuff"));
    	  }
      }
      /*if ((event.isCancelled()) || (!(event instanceof EntityDamageByEntityEvent))) {
        Heroes.debug.stopTask("HeroesSkillListener");
        return;
      }*/

      //Entity projectile = (Entity) ((WeaponDamageEvent)event).getDamager();

      /*if ((!(projectile instanceof Arrow)) || (!(((Projectile)projectile).getShooter() instanceof Player))) {
        Heroes.debug.stopTask("HeroesSkillListener");
        return;
      }*/

      /*Player player = (Player)((Projectile)projectile).getShooter();
      Hero hero = SkillSuperCharge.this.plugin.getCharacterManager().getHero(player);
      
      if (!hero.hasEffect("SuperChargeBuff")) {
        Heroes.debug.stopTask("HeroesSkillListener");
        return;
      }
      
      int damage = (int) (SkillConfigManager.getUseSetting(hero, this.skill, Setting.DAMAGE.node(), 5.0, false) +
              (SkillConfigManager.getUseSetting(hero, this.skill, Setting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this.skill)));
      damage = damage > 0 ? damage : 0;
      
      SkillSuperCharge.damageEntity((LivingEntity) event.getEntity(), player, damage, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
      event.setDamage(damage);
*/
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onEntityShootBow(EntityShootBowEvent event) {
      if ((event.isCancelled()) || (!(event.getEntity() instanceof Player)) || (!(event.getProjectile() instanceof Arrow))) {
        return;
      }
      Hero hero = SkillSuperCharge.this.plugin.getCharacterManager().getHero((Player)event.getEntity());
      if (hero.hasEffect("SuperChargeBuff")) {
        int mana = SkillConfigManager.getUseSetting(hero, this.skill, "mana-per-shot", 1, true);
        if (hero.getMana() < mana)
          hero.removeEffect(hero.getEffect("SuperChargeBuff"));
        else
          hero.setMana(hero.getMana() - mana);
      }
    }
  }
}