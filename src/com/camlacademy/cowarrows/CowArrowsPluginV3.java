package com.camlacademy.cowarrows;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.camlacademy.spigot.utils.PlayerHelper;

public class CowArrowsPluginV3 extends JavaPlugin implements Listener{

	public static final String CONFIG_KEY_ALLOW_COW_ARROW_RECIPE = "allowCowArrowRecipe";
	private boolean spawnMushroomCows = false;
	private PlayerHelper playerHelper;
	
	
	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		getConfig().addDefault(CONFIG_KEY_ALLOW_COW_ARROW_RECIPE, true);
		saveConfig();
		playerHelper = new PlayerHelper(this);
		registerRecipes();
		
		getCommand("toggleMushroomCows").setExecutor(this);
		
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		spawnMushroomCows = !spawnMushroomCows;
		return true;
	}
	private void registerRecipes() {
		boolean allowCowArrowRecipe = getConfig().getBoolean(CONFIG_KEY_ALLOW_COW_ARROW_RECIPE);
		
		if (allowCowArrowRecipe) {
			
			ItemStack itemStack = new ItemStack(Material.ARROW);
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName("Cow Arrow");
			itemStack.setItemMeta(itemMeta);
			
			ShapedRecipe recipe = new ShapedRecipe(itemStack);
			recipe.shape(" B ", " A ", " L ");
				
			recipe.setIngredient('L', Material.LEATHER);
			recipe.setIngredient('A', Material.ARROW);
			recipe.setIngredient('B', Material.RAW_BEEF);
			
			getServer().addRecipe(recipe);
			
		}
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
		ItemStack itemInHand = playerHelper.getItemInHand(player);


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
		playerHelper.replaceEntityWithNamedArrow(event.getEntity(), itemInHand, player, "Cow arrow");
	}
	
	
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onPlayerShootEntityArrow(EntityShootBowEvent event) {


		if (event.getEntity() == null || !(event.getEntity() instanceof Player)) {
			//this was probably a skeleton.  Don't do anything (unless you want them to spawn cows too.
			return;
		}


		Player player = (Player) event.getEntity();


		ItemStack firstArrowStack = playerHelper.getFirstArrowStack(player);


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
		if (spawnMushroomCows) {
			event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.MUSHROOM_COW);
		} else {
		event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.COW);

		}
		//remove the arrow so players cant pick it up and spawn more cows for free
		event.getEntity().remove();
	}
	
	
	
	
	//private
}
