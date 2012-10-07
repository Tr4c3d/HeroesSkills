package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

public class SkillFireWalk extends ActiveSkill implements Listener{
	
	public HeroesSkillTree hst = (HeroesSkillTree)Bukkit.getServer().getPluginManager().getPlugin("HeroesSkillTree");
    private String applyText;
    private String expireText;

    public SkillFireWalk(Heroes plugin) {
        super(plugin, "FireWalk");
        setDescription("Sets all blocks the player walks on fire for $1");
        setUsage("/skill FireWalk");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill FireWalk"});
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.SILENCABLE, SkillType.FIRE);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //DURATION
        float duration = (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 2000, false) +
                SkillConfigManager.getUseSetting(hero, this, Setting.DURATION_INCREASE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if(hst != null) duration += (SkillConfigManager.getUseSetting(hero, this, "hst-duration", 0.0, false) * (hst.getSkillLevel(hero, this) - 1) / 1000);
        if (duration > 0) {
            description = getDescription().replace("$1", duration + "s");
        }
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 1000, false)
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
        node.set("on-text", "%hero% starts to %skill%!");
        node.set("off-text", "%hero% stops to %skill%!");
        node.set(Setting.DURATION.node(), 2000);
        node.set(Setting.DURATION_INCREASE.node(), 0);
        node.set("hst-duration", 0);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getRaw(this, "on-text", "%hero% starts to %skill%!").replace("%hero%", "$1").replace("%skill%", "$2");
        expireText = SkillConfigManager.getRaw(this, "off-text", "%hero% stops to %skill%!").replace("%hero%", "$1").replace("%skill%", "$2");
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
    	if (hero.hasEffect("FireWalk")) {
    		hero.removeEffect(hero.getEffect("FireWalk"));
    	}
    	else{
	        int duration = (int) ((SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 2000, false) +
	                (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(this))));
	        if(hst != null) duration += (SkillConfigManager.getUseSetting(hero, this, "hst-duration", 0.0, false) * (hst.getSkillLevel(hero, this) - 1));
	        duration = duration > 0 ? duration : 0;
	        hero.addEffect(new FireWalkingEffect(this, duration));
    	}
    	return SkillResult.NORMAL;
    }
    
    public class FireWalkingEffect extends ExpirableEffect{
    	public FireWalkingEffect(SkillFireWalk skill, long duration){
    		super(skill, "FireWalk", duration);
    		this.types.add(EffectType.FIRE);
    		this.types.add(EffectType.DISPELLABLE);
    	}
    	
    	@Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName(), "FireWalk");
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName(), "FireWalk");
            player.setFireTicks(0);
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
    	Hero hero = plugin.getCharacterManager().getHero(event.getPlayer());
    	if(hero.hasEffect("FireWalk")){
	    	Block blockTo = event.getTo().getBlock();
	    	Block blockFrom = event.getFrom().getBlock();
	    	if(blockTo != blockFrom){
	    		Block block = event.getFrom().subtract((event.getTo().subtract(event.getFrom())).multiply(10)).getBlock();
	    		if(block.getType() == Material.AIR) block.setType(Material.FIRE);
	    	}
    	}
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
    	if(event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK){
    		if(event.getEntity() instanceof Player){
    			Player player = (Player)event.getEntity();
    			Hero hero = plugin.getCharacterManager().getHero(player);
    			if(hero.hasEffect("FireWalk")){
    				event.setCancelled(true);
    			}
    		}
    	}
    }
}