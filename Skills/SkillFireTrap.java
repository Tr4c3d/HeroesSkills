package com.herocraftonline.heroes.characters.skill.skills;

import java.util.HashSet;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class SkillFireTrap extends ActiveSkill implements Listener{

	public HashSet<Block> getTraps = new HashSet<Block>();

    public SkillFireTrap(Heroes plugin) {
        super(plugin, "FireTrap");
        setDescription("Sets a fire trap on target block");
        setUsage("/skill FireTrap");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill FireTrap"});
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //MAX_DISTANCE
        int maxDistance = (int) ((SkillConfigManager.getUseSetting(hero, this, "max-distance", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "max-distance-increase", 0.0, false) * hero.getSkillLevel(this))));
        maxDistance = maxDistance > 0 ? maxDistance : 0;
        if (maxDistance > 0) {
            description += " D:" + maxDistance + "s";
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
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        int maxDistance = (int) ((SkillConfigManager.getUseSetting(hero, this, "max-distance", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "max-distance-increase", 0.0, false) * hero.getSkillLevel(this))));
        maxDistance = maxDistance > 0 ? maxDistance : 0;
    	Block block = hero.getPlayer().getTargetBlock(null, maxDistance);
    	getTraps.add(block);
    	return SkillResult.NORMAL;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
    	Player player = event.getPlayer();
    	Block block = player.getLocation().getBlock();
    	if(getTraps.contains(block.getRelative(0, -1, 0))){
    		block.setType(Material.FIRE);
    		block.getRelative(-1, 0, -1).setType(Material.FIRE);
    		block.getRelative(-1, 0, 0).setType(Material.FIRE);
    		block.getRelative(-1, 0, 1).setType(Material.FIRE);
    		block.getRelative(0, 0, -1).setType(Material.FIRE);
    		block.getRelative(0, 0, 1).setType(Material.FIRE);
    		block.getRelative(1, 0, -1).setType(Material.FIRE);
    		block.getRelative(1, 0, 0).setType(Material.FIRE);
    		block.getRelative(1, 0, 1).setType(Material.FIRE);
    		getTraps.remove(block.getRelative(0, -1, 0));
    	}
    }
}