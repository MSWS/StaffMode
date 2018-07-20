package org.mswsplex.staff.events;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.mswsplex.staff.managers.PlayerManager;
import org.mswsplex.staff.msws.Main;
import org.mswsplex.staff.utils.MSG;
import org.mswsplex.staff.utils.Utils;

public class Events implements Listener {
	public Events() {
		Bukkit.getPluginManager().registerEvents(this, Main.plugin);
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getStaffMode(player)) {
			PlayerManager.disableStaffMode(player);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getStaffMode(player)) {
			event.setCancelled(true);
			MSG.noSpam(player, MSG.getString("Staff.Action", "you can't do that in staff mode."), 10000);
		}
		ItemStack item = player.getItemInHand();
		if (item == null || item.getType() == Material.AIR)
			return;
		if(item.getType()==Material.INK_SACK) {
			boolean vanished = PlayerManager.getBoolean(player, "vanished");
			PlayerManager.setInfo(player, "vanished", !vanished);
			String name = "";
			if(vanished) {
				item.setDurability((short) 8);
				name = "&cVanish Disabled";
				for(Player target:Bukkit.getOnlinePlayers())
					target.showPlayer(player);
			}else {
				int vLevel = PlayerManager.getVanishLevel(player);
				for (Player target : Bukkit.getOnlinePlayers()) {
					if (PlayerManager.getVanishLevel(target) < vLevel)
						target.hidePlayer(player);
				}
				item.setDurability((short) 10);
				name = "&aVanish Enabled";
			}
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MSG.color("&r"+name));
			item.setItemMeta(meta);
			player.setItemInHand(item);
		}
		String id = "";
		for (String res : Main.plugin.config.getConfigurationSection("Items").getKeys(false)) {
			if (Utils.parseItem(Main.plugin.config.getConfigurationSection("Items"), res, player).equals(item)) {
				id = res;
				break;
			}
		}
		switch (id) {
		case "Compass":
			Location loc = player.getTargetBlock((Set<Material>) null, 100).getLocation();
			player.teleport(loc);
			break;
		case "Head":
			PlayerManager.openStaffInventory(player, 0);
			break;
		}
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (PlayerManager.getStaffMode(player))
			event.setCancelled(true);
		if (PlayerManager.getInfo(player, "page") == null)
			return;
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
			return;
		if (event.getRawSlot() == event.getInventory().getSize() - 1) {
			PlayerManager.openStaffInventory(player, (int) Math.round(PlayerManager.getDouble(player, "page")+1));
		}

		if (event.getRawSlot() == event.getInventory().getSize() - 9) {
			PlayerManager.openStaffInventory(player, (int) Math.round(PlayerManager.getDouble(player, "page")-1));
		}

	}

	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getStaffMode(player)) {
			event.setCancelled(true);
			MSG.noSpam(player, MSG.getString("Staff.Action", "you can't do that in staff mode."));
		}
	}

	@EventHandler
	public void onPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getStaffMode(player)) {
			event.setCancelled(true);
			MSG.noSpam(player, MSG.getString("Staff.Action", "you can't do that in staff mode."));
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getStaffMode(player)) {
			event.setCancelled(true);
			MSG.noSpam(player, MSG.getString("Staff.Action", "you can't do that in staff mode."));
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getStaffMode(player)) {
			event.setCancelled(true);
			MSG.noSpam(player, MSG.getString("Staff.Action", "you can't do that in staff mode."));
		}
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getStaffMode(player)) {
			event.setCancelled(true);
			MSG.noSpam(player, MSG.getString("Staff.Action", "you can't do that in staff mode."));
		}
	}

	@EventHandler
	public void onBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getStaffMode(player)) {
			event.setCancelled(true);
			MSG.noSpam(player, MSG.getString("Staff.Action", "you can't do that in staff mode."));
		}
	}

	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if (item == null || item.getType() == Material.AIR)
			return;
		if(event.getRightClicked()==null||(!(event.getRightClicked() instanceof Player)))
			return;
		Player clicked = (Player) event.getRightClicked();
		String id = "";
		for (String res : Main.plugin.config.getConfigurationSection("Items").getKeys(false)) {
			if (Utils.parseItem(Main.plugin.config.getConfigurationSection("Items"), res, player).equals(item)) {
				id = res;
				break;
			}
		}
		switch (id) {
		case "Book":
			player.openInventory(clicked.getInventory());
			break;
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			if (PlayerManager.getStaffMode((Player) event.getEntity()))
				event.setCancelled(true);
		}
		if (event.getDamager() == null)
			return;
		if (event.getDamager() instanceof Player) {
			if (PlayerManager.getStaffMode((Player)event.getDamager())) {
				event.setCancelled(true);
				MSG.noSpam((Player)event.getDamager(), MSG.getString("Staff.Action", "you can't do that in staff mode."));
			}
		}
		if (event.getDamager() instanceof Projectile) {
			ProjectileSource src = ((Projectile) event.getDamager()).getShooter();
			if (src == null)
				return;
			if (src instanceof Player) {
				if (PlayerManager.getStaffMode((Player)src)) {
					event.setCancelled(true);
					MSG.noSpam((Player)src, MSG.getString("Staff.Action", "you can't do that in staff mode."));
				}
			}
		}
	}
}
