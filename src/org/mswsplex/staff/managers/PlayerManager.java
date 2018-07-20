package org.mswsplex.staff.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;
import org.mswsplex.staff.msws.Main;
import org.mswsplex.staff.utils.MSG;
import org.mswsplex.staff.utils.Utils;

public class PlayerManager {
	public static void setInfo(OfflinePlayer player, String id, Object data) {
		if (!isSaveable(data)) {
			int currentLine = Thread.currentThread().getStackTrace()[2].getLineNumber();

			String fromClass = new Exception().getStackTrace()[1].getClassName();
			if (fromClass.contains("."))
				fromClass = fromClass.split("\\.")[fromClass.split("\\.").length - 1];
			MSG.log("WARNING!!! SAVING ODD DATA FROM " + fromClass + ":" + currentLine);
		}
		Main.plugin.data.set(player.getUniqueId() + "." + id, data);
	}

	public static void deleteInfo(OfflinePlayer player) {
		Main.plugin.data.set(player.getUniqueId() + "", null);
	}

	public static void removeInfo(OfflinePlayer player, String id) {
		Main.plugin.data.set(player.getUniqueId() + "." + id, null);
	}

	public static Object getInfo(OfflinePlayer player, String id) {
		return Main.plugin.data.get(player.getUniqueId() + "." + id);
	}

	public static String getString(OfflinePlayer player, String id) {
		return Main.plugin.data.getString(player.getUniqueId() + "." + id);
	}

	public static Double getDouble(OfflinePlayer player, String id) {
		return Main.plugin.data.getDouble(player.getUniqueId() + "." + id);
	}

	public static Boolean getBoolean(OfflinePlayer player, String id) {
		return Main.plugin.data.getBoolean(player.getUniqueId() + "." + id);
	}

	public static List<String> getStringList(OfflinePlayer player, String id) {
		return Main.plugin.data.getStringList(player.getUniqueId() + "." + id);
	}

	public static ItemStack parseItem(ConfigurationSection section, String path, OfflinePlayer player) {
		ConfigurationSection gui = section.getConfigurationSection(path);
		ItemStack item = new ItemStack(Material.valueOf(gui.getString("Icon")));
		List<String> lore = new ArrayList<String>();
		if (gui.contains("Amount"))
			item.setAmount(gui.getInt("Amount"));
		if (gui.contains("Data"))
			item.setDurability((short) gui.getInt("Data"));
		ItemMeta meta = item.getItemMeta();
		if (gui.contains("Name"))
			meta.setDisplayName(MSG.color("&r" + gui.getString("Name")));
		if (gui.contains("Lore")) {
			for (String temp : gui.getStringList("Lore"))
				lore.add(MSG.color("&r" + temp));
		}
		if (gui.getBoolean("Unbreakable")) {
			meta.spigot().setUnbreakable(true);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		if (gui.contains("Cost")) {
			HashMap<Material, Integer> mats = new HashMap<>();
			ConfigurationSection costs = gui.getConfigurationSection("Cost");
			for (String material : costs.getKeys(false))
				mats.put(Material.valueOf(material), costs.getInt(material));
			lore.add("");
			if (mats.size() == 1) {
				lore.add(MSG.color("&aCost: &c" + mats.values().toArray()[0] + " "
						+ MSG.camelCase(mats.keySet().toArray()[0] + "")));
			} else {
				lore.add(MSG.color("&aCost:"));
				for (Material mat : mats.keySet()) {
					lore.add(MSG.color("&c* " + mats.get(mat) + " "
							+ MSG.camelCase(mat.name() + (mats.get(mat) == 1 ? "" : "s"))));
				}
			}
		}
		if (gui.contains("Enchantments")) {
			ConfigurationSection enchs = gui.getConfigurationSection("Enchantments");
			for (String enchant : enchs.getKeys(false)) {
				int level = 1;
				if (enchs.contains(enchant + ".Level"))
					level = enchs.getInt(enchant + ".Level");
				if (enchs.contains(enchant + ".Visible") && !enchs.getBoolean(enchant + ".Visible"))
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				item.setItemMeta(meta);
				item.addUnsafeEnchantment(Enchantment.getByName(enchant.toUpperCase()), level);
				meta = item.getItemMeta();
			}
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Get whether an object is saveable in YAML
	 * 
	 * @param obj Object type to test
	 * @return True if saveable, false otherwise
	 */
	public static boolean isSaveable(Object obj) {
		return (obj instanceof String || obj instanceof Integer || obj instanceof ArrayList || obj instanceof Boolean
				|| obj == null || obj instanceof Double || obj instanceof Short || obj instanceof Long
				|| obj instanceof Character);
	}

	public static boolean getStaffMode(OfflinePlayer player) {
		return getBoolean(player, "staffmode");
	}

	public static int getVanishLevel(Player player) {
		for (int i = 100; i > 0; i--) {
			if (player.hasPermission("staffmode.vanish." + i))
				return i;
		}
		return 0;
	}

	public static void enableStaffMode(Player player) {
		PlayerManager.setInfo(player, "staffmode", true);
		PlayerManager.setInfo(player, "vanished", true);
		Main.plugin.data.createSection("Player." + player.getUniqueId() + ".staffmode.inventory");
		ConfigurationSection invItems = Main.plugin.data
				.getConfigurationSection("Player." + player.getUniqueId() + ".staffmode.inventory");
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack item = player.getInventory().getItem(i);
			if (item == null || item.getType() == Material.AIR)
				continue;
			invItems.set(i + ".Icon", item.getType());
			invItems.set(i + ".Amount", item.getAmount());
			invItems.set(i + ".Data", item.getDurability());
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasDisplayName())
					invItems.set(i + ".Name", item.getItemMeta().getDisplayName());

			}
			if (item.getItemMeta().hasEnchants() && item.getEnchantments() != null && !item.getEnchantments().isEmpty())
				for (Enchantment ench : item.getEnchantments().keySet())
					invItems.set(i + ".Enchantments." + ench.getName() + ".Level", item.getEnchantments().get(ench));
		}
		invItems.set("ArmorContents", player.getInventory().getArmorContents());

		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);

		ConfigurationSection section = Main.plugin.data
				.getConfigurationSection("Player." + player.getUniqueId() + ".staffmode");

		section.set("health", player.getHealth());
		section.set("food", player.getFoodLevel());
		section.set("saturation", player.getSaturation());
		section.set("fireticks", player.getFireTicks());
		section.set("velocity", player.getVelocity());
		section.set("location", player.getLocation());
		section.set("isFlying", player.isFlying());
		section.set("allowFlight", player.getAllowFlight());
		section.set("fallDistance", player.getFallDistance());
		section.set("gamemode", player.getGameMode().toString());

		player.getInventory().clear();
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setSaturation(2);
		player.setFireTicks(-20);

		for (String res : Main.plugin.config.getConfigurationSection("Items").getKeys(false)) {
			ItemStack item = Utils.parseItem(Main.plugin.config.getConfigurationSection("Items"), res, player);
			if (Main.plugin.config.contains("Items." + item + ".Permission")) {
				if (!player.hasPermission(Main.plugin.config.getString("Items." + item + ".Permission")))
					continue;
			}
			player.getInventory().setItem(Main.plugin.config.getInt("Items." + res + ".HotBar"), item);
		}
		int vLevel = PlayerManager.getVanishLevel(player);
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (PlayerManager.getVanishLevel(target) < vLevel)
				target.hidePlayer(player);
		}
		Main.plugin.saveData();
		MSG.tell(player,
				MSG.getString("Staff.Toggle", "you %status% staff mode").replace("%status%",
						PlayerManager.getStaffMode(player) ? MSG.getString("Enabled", "enabled")
								: MSG.getString("Disabled", "disabled")));
	}

	public static void disableStaffMode(Player player) {
		PlayerManager.setInfo(player, "staffmode", false);
		PlayerManager.setInfo(player, "vanished", false);
		Location loc = (Location) Main.plugin.data.get("Player." + player.getUniqueId() + ".staffmode.location");
		if (loc != null)
			player.teleport(loc);

		ConfigurationSection section = Main.plugin.data
				.getConfigurationSection("Player." + player.getUniqueId() + ".staffmode");

		player.setAllowFlight(section.getBoolean("allowFlight"));
		player.setFlying(section.getBoolean("isFlying"));
		player.setHealth(section.getDouble("health"));
		player.setFoodLevel(section.getInt("food"));
		player.setSaturation((float) section.getDouble("saturation"));
		player.setFireTicks(section.getInt("fireticks"));
		player.setVelocity((Vector) section.get("velocity"));
		player.setFallDistance((float) section.getDouble("fallDistance"));
		player.setGameMode(GameMode.valueOf(section.getString("gamemode")));
		Main.plugin.data.set("Player." + player.getUniqueId() + ".staffmode.food", player.getFoodLevel());
		Main.plugin.data.set("Player." + player.getUniqueId() + ".staffmode.saturation", player.getSaturation());
		Main.plugin.data.set("Player." + player.getUniqueId() + ".staffmode.fireticks", player.getFireTicks());

		ConfigurationSection invItems = Main.plugin.data
				.getConfigurationSection("Player." + player.getUniqueId() + ".staffmode.inventory");
		if (invItems == null)
			return;

		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);
		player.getInventory().setArmorContents((ItemStack[]) invItems.get("ArmorContents"));

		for (String res : invItems.getKeys(false)) {
			int slot;
			try {
				slot = Integer.parseInt(res);
			} catch (Exception e) {
				continue;
			}
			ItemStack item = Utils.parseItem(invItems, res, player);
			player.getInventory().setItem(slot, item);
		}

		MSG.tell(player,
				MSG.getString("Staff.Toggle", "you %status% staff mode").replace("%status%",
						PlayerManager.getStaffMode(player) ? MSG.getString("Enabled", "enabled")
								: MSG.getString("Disabled", "disabled")));
		Main.plugin.data.set("Player."+player.getUniqueId(), null);
	}

	public static void openStaffInventory(Player player, int page) {
		PlayerManager.setInfo(player, "page", page);
		List<Player> staff = new ArrayList<Player>();
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (target.hasPermission("staffmode.staff"))
				staff.add(target);
		}
		int size = 54;
		for (int i = 9; i <= 45; i++) {
			if (i > staff.size()) {
				size = i + 9;
				break;
			}
		}

		Inventory inv = Bukkit.createInventory(null, size, Main.plugin.config.getString("StaffGUI.Title"));
		int id = 0, slot = 0;
		for (Player target : staff) {
			if (id < (inv.getSize() - 9) * page || id > (inv.getSize() - 9 * page) + inv.getSize() - 9) {
				id++;
				continue;
			}
			ItemStack head = new ItemStack(Material.SKULL_ITEM);
			head.setDurability((short) 3);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwner(target.getName());
			meta.setDisplayName(MSG
					.color("&r" + Main.plugin.config.getString("StaffGUI.Name").replace("%name%", target.getName())));
			List<String> lore = new ArrayList<String>();
			Main.plugin.config.getStringList("StaffGUI.Lore").forEach((line) -> lore.add(MSG.color("&r" + line)
					.replace("%time%", TimeManager.getTime((double) System.currentTimeMillis()-target.getLastPlayed()))
					.replace("%name%", target.getName())
					.replace("%world%", target.getWorld().getName())
					.replace("%staff%", MSG.color(MSG.TorF(PlayerManager.getStaffMode(target))))
					));
			meta.setLore(lore);
			head.setItemMeta(meta);
			inv.setItem(slot, head);
			slot++;
			id++;
		}
		if (slot < staff.size() - 1) {
			inv.setItem(inv.getSize() - 1, Utils.parseItem(Main.plugin.config, "NextItem", player));
		}
		if (page > 0) {
			inv.setItem(inv.getSize() - 9, Utils.parseItem(Main.plugin.config, "BackItem", player));
		}
		player.openInventory(inv);
	}
}
