package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicHealEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SkillSpiritRecovery extends ActiveSkill implements Listener{
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");
    private String applyText;
    private String expireText;

    public SkillSpiritRecovery(Heroes plugin) {
        super(plugin, "SpiritRecovery");
        setDescription("Toggle-able passive. Heals $1 HP and recovers $2 mana but leaves you $3 times more vulnerable while active.");
        setUsage("/skill SpiritRecovery");
        setArgumentRange(0, 0);
        setIdentifiers("skill SpiritRecovery");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.SILENCABLE, SkillType.HEAL, SkillType.BUFF);
    }

    @Override
    public String getDescription(Hero hero) {
        int health = (int) (SkillConfigManager.getUseSetting(hero, this, "tick-health", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "tick-health-increase", 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) health += (SkillConfigManager.getUseSetting(hero, this, "hst-tick-health", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        health = health > 0 ? health : 0;
        int mana = (int) (SkillConfigManager.getUseSetting(hero, this, "tick-mana", 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "tick-mana-increase", 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) mana += (SkillConfigManager.getUseSetting(hero, this, "hst-tick-mana", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        mana = mana > 0 ? mana : 0;
        float multiplier = (float) (SkillConfigManager.getUseSetting(hero, this, "multiplier", 10.0, false) -
                (SkillConfigManager.getUseSetting(hero, this, "multiplier-decrease", 0.0, false) * hero.getSkillLevel(this)));
        if(hst != null) multiplier -= (SkillConfigManager.getUseSetting(hero, this, "hst-multiplier", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
        multiplier = multiplier > 0 ? multiplier : 0;
        String description = getDescription().replace("$1", health + "").replace("$2", mana + "").replace("$3", multiplier + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("on-text", "%hero% starts to get his %skill%!");
        node.set("off-text", "%hero% stops his %skill%!");
        node.set("tick-health", 3);
        node.set("tick-health-increase", 0);
        node.set("hst-tick-health", 0);
        node.set("tick-mana", 1);
        node.set("tick-mana-increase", 0);
        node.set("hst-tick-mana", 0);
        node.set("multiplier", 2);
        node.set("multiplier-decrease", 0);
        node.set("hst-multiplier", 0);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getRaw(this, "on-text", "%hero% starts to get his %skill%!").replace("%hero%", "$1").replace("%skill%", "$2");
        expireText = SkillConfigManager.getRaw(this, "off-text", "%hero% stops his %skill%!").replace("%hero%", "$1").replace("%skill%", "$2");
    }
    
    @Override
    public SkillResult use(Hero hero, String args[]) {
        if (hero.hasEffect("SpiritRecovery")) {
            hero.removeEffect(hero.getEffect("SpiritRecovery"));
        } 
        else {
        	int health = (int) (SkillConfigManager.getUseSetting(hero, this, "tick-health", 1.0, false) +
                    (SkillConfigManager.getUseSetting(hero, this, "tick-health-increase", 0.0, false) * hero.getSkillLevel(this)));
        	if(hst != null) health += (SkillConfigManager.getUseSetting(hero, this, "hst-tick-health", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
            health = health > 0 ? health : 0;
            int mana = (int) (SkillConfigManager.getUseSetting(hero, this, "tick-mana", 10, false) +
                    (SkillConfigManager.getUseSetting(hero, this, "tick-mana-decrease", 0.0, false) * hero.getSkillLevel(this)));
            if(hst != null) mana += (SkillConfigManager.getUseSetting(hero, this, "hst-tick-mana", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
            mana = mana > 0 ? mana : 0;
            hero.addEffect(new RecoveryEffect(this, health, mana, hero.getPlayer()));
        }
        return SkillResult.NORMAL;
    }
    
    public class RecoveryEffect extends PeriodicHealEffect implements Listener{

        private int tickMana;
        private boolean firstTime = true;

        public RecoveryEffect(SkillSpiritRecovery skill, int tickHealth, int tickMana, Player player) {
            super(skill, "SpiritRecovery", 1000, 99999999, tickHealth, player);
            this.tickMana = tickMana;
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.HEAL);
        }

        @Override
        public void applyToHero(Hero hero) {
            firstTime = true;
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName(), "SpiritRecovery");
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName(), "SpiritRecovery");
        }

        @Override
        public void tickHero(Hero hero) {
            super.tickHero(hero);
            if (tickMana > 0 && !firstTime) {
                if (hero.getMana() + tickMana > hero.getMaxMana()) {
                    hero.setMana(hero.getMaxMana());
                } else {
                    hero.setMana(hero.getMana() + tickMana);
                }
            } else if (firstTime) {
                firstTime = false;
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
    	Hero hero = plugin.getCharacterManager().getHero(event.getPlayer());
    	if(hero.hasEffect("SpiritRecovery")){
    		hero.removeEffect(hero.getEffect("SpiritRecovery"));
    	}
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
    	if(event.getEntity() instanceof Player){
    		Hero hero = plugin.getCharacterManager().getHero((Player) event.getEntity());
    		if(hero.hasEffect("SpiritRecovery")){
                float multiplier = (float) (SkillConfigManager.getUseSetting(hero, this, "multiplier", 2.0, false) +
                        (SkillConfigManager.getUseSetting(hero, this, "multiplier-increase", 0.0, false) * hero.getSkillLevel(this)));
                if(hst != null) multiplier += (SkillConfigManager.getUseSetting(hero, this, "hst-multiplier", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
                multiplier = multiplier > 0 ? multiplier : 0;
    			event.setDamage((int) (event.getDamage() * multiplier));
    			hero.removeEffect(hero.getEffect("SpiritRecovery"));
    		}
    	}
    }
}