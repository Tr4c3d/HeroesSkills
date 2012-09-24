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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
    		  hero.removeEffect(hero.getEffect("SuperChargeBuff"));
    	  }
      }
    }
  }
}