package com.camlacademy.cowarrows;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CowArrowsPlugin extends JavaPlugin implements Listener{

	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	


	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void playerHitsAnimalWithArrow(EntityDamageByEntityEvent event) {
		if (event.getCause() != null && event.getCause().equals(DamageCause.PROJECTILE)){
			//the mob was hit by a projectile.  Don't do anything
			return;
		}


		Player player = null;


		if (!(event.getDamager() instanceof Player)) {
			//The damager was not a player. Don't do anything
			return;
		} else {
			//cast the Damager to a Player
			player = (Player) event.getDamager();
		}
		ItemStack itemInHand = getItemInHand(player);


		if (itemInHand == null) {
			//no item in hand. Don't do anything
			return;
		}


		if (!itemInHand.getType().equals(Material.ARROW)) {
			//The item in hand does is not an arrow. Don't do anything.
			return;
		}


		if (!event.getEntity().getType().equals(EntityType.COW)) {
			//the mob that was hit is not a cow.  Don't do anything.
			return;
		}
		
		//If we got this far, replace the cow with an arrow named "Cow arrow"
		replaceCowWithCowArrow(event.getEntity(), itemInHand, player);
	}
	
	
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onPlayerShootEntityArrow(EntityShootBowEvent event) {


		if (event.getEntity() == null || !(event.getEntity() instanceof Player)) {
			//this was probably a skeleton.  Don't do anything (unless you want them to spawn cows too.
			return;
		}


		Player player = (Player) event.getEntity();


		ItemStack firstArrowStack = getFirstArrowStack(player);


		if (firstArrowStack == null || firstArrowStack.getItemMeta() == null
				|| firstArrowStack.getItemMeta().getDisplayName() == null
				|| !firstArrowStack.getItemMeta().getDisplayName().equals("Cow arrow")) {
			//There was either no arrow found (i.e. the player fired an empty bow),
			//or the arrow was not named "Cow arrow". Don't do anything
			return;
		}

		//rename the projectile entity "Cow arrow" so that when it lands we know we should spawn a cow
		event.getProjectile().setCustomName("Cow arrow");
	}
	
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onEntityArrowHit(ProjectileHitEvent event) {


		if (!(event.getEntity().getShooter() instanceof Player)){
			//this was probably a skeleton.  Don't do anything (unless you want them to spawn cows too.
			return;
		}
			
		if (event.getEntity() == null || event.getEntity().getCustomName() == null){
			//this arrow entity was did not have a custom name
			//Don't do anything
			return;
		}


		if (!event.getEntity().getCustomName().equals("Cow arrow")) {
			//this arrow entity was not named "Cow arrow"
			//Don't do anything
			return;
		}


		//spawn the cow
		event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.COW);


		//remove the arrow so players cant pick it up and spawn more cows for free
		event.getEntity().remove();
	}
	
	
	
	
	//private
	private ItemStack getFirstArrowStack(Player player) {


		if (player.getInventory() != null && player.getInventory().getStorageContents() != null) {
			
			for (ItemStack item : player.getInventory().getStorageContents()) {
				if (item == null){
					//this must be an empty slot in the inventory.  
					//We will ignore it and "continue" to the next iteration of the loop
					continue;
				}


				Material material = item.getType();


				if (material.equals(Material.ARROW)) {
					//this is the first arrow we encountered, so we will return it.
					//the first arrow stack encountered is always the arrow stack being fired.
					return item;
				}
			}
		}


		//if we got this far, the player must not have had an arrow in their inventory
		return null;
	}
	
	private ItemStack getItemInHand(Player player) {
		if (player == null)
			return null;

		//if this is not null, return it, otherwise, try another method.
		if(player.getItemInHand() != null){
			return player.getItemInHand();
		}
		
		//if this is not null, return it, otherwise, try another method.
		if(player.getEquipment() != null && player.getEquipment().getItemInHand() != null){
			return player.getEquipment().getItemInHand();
		}
		
		//if this is not null, return it, otherwise, try another method.
		if(player.getInventory() != null && player.getInventory().getItemInHand() != null){
			return player.getInventory().getItemInHand();
		}


		//if we got this far, the player either does not have anything in their hand, or our code needs to be updated/fixed
		return null;
	}
	
	
	private void replaceCowWithCowArrow(Entity entity, ItemStack itemInHand, Player player) {


		//create arrow for entity
		ItemStack arrow = new ItemStack(Material.ARROW);
		ItemMeta meta = arrow.getItemMeta();
		meta.setDisplayName("Cow arrow");
		arrow.setItemMeta(meta);
		
		//drop the arrow
		entity.getWorld().dropItem(entity.getLocation(), arrow);
		
		//remove the cow
		entity.remove();
		
		//remove one arrow from the players hand
		if(itemInHand.getAmount() == 1){
			//player only has one arrow in their hand, so we remove the entire "stack"
			player.getInventory().remove(itemInHand);
		}else{
			//player must have more than one arrow in their hand, so we set the amount equal to what it was minus one
			itemInHand.setAmount(itemInHand.getAmount() - 1);
		}
	}
}
