package org.mswsplex.staff.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.mswsplex.staff.msws.Main;

public class Utils {
	public static int getArmorValue(Material mat) {
		switch (getArmorType(mat).toLowerCase()) {
		case "diamond":
			return 4;
		case "iron":
			return 3;
		case "chainmail":
			return 2;
		case "gold":
			return 1;
		case "leather":
			return 0;
		default:
			return 0;
		}
	}

	public static int getSlot(Material type) {
		if (!type.name().contains("_"))
			return 0;
		switch (type.name().split("_")[1]) {
		case "HELMET":
			return 3;
		case "CHESTPLATE":
			return 2;
		case "LEGGINGS":
			return 1;
		case "BOOTS":
			return 0;
		}
		return 0;
	}

	public static String getArmorType(Material mat) {
		if (!mat.name().contains("_")) {
			return "";
		}

		String name = mat.name().split("_")[0];
		return name;
	}

	public static boolean isArmor(Material mat) {
		return mat.name().contains("CHESTPLATE") || mat.name().contains("LEGGINGS") || mat.name().contains("HELMET")
				|| mat.name().contains("BOOTS");
	}

	public static Sound getBreakSound(Material mat) {
		if (mat.name().contains("GLOW") || mat.name().contains("GLASS"))
			return Sound.GLASS;
		if (mat.name().contains("STONE"))
			return Sound.DIG_STONE;
		if (mat.name().contains("SAND"))
			return Sound.DIG_SAND;
		if (mat.name().contains("SNOW"))
			return Sound.DIG_SNOW;
		if (mat.name().contains("WOOD") || mat.name().contains("LOG"))
			return Sound.DIG_WOOD;
		switch (mat.name()) {
		case "GRAVEL":
			return Sound.DIG_GRAVEL;
		case "GRASS":
		case "DIRT":
			return Sound.DIG_GRASS;
		case "WOOL":
			return Sound.DIG_WOOL;
		default:
			return Sound.DIG_GRASS;
		}
	}

	public static Block getBlockFromFace(Block block, BlockFace face) {
		switch (face.toString()) {
		case "UP":
			return block.getLocation().add(0, 1, 0).getBlock();
		case "DOWN":
			return block.getLocation().subtract(0, 1, 0).getBlock();
		case "NORTH":
			return block.getLocation().subtract(0, 0, 1).getBlock();
		case "SOUTH":
			return block.getLocation().add(0, 0, 1).getBlock();
		case "EAST":
			return block.getLocation().add(1, 0, 0).getBlock();
		case "WEST":
			return block.getLocation().subtract(1, 0, 0).getBlock();
		default:
			return null;
		}
	}

	public static Inventory getGui(OfflinePlayer player, String id, int page) {
		if (!Main.plugin.gui.contains(id))
			return null;
		ConfigurationSection gui = Main.plugin.gui.getConfigurationSection(id);
		if (!gui.contains("Size") || !gui.contains("Title"))
			return null;
		String title = gui.getString("Title").replace("%player%", player.getName());
		if (player.isOnline())
			title = title.replace("%world%", ((Player) player).getWorld().getName());
		title = title.replace("%world%", "");
		Inventory inv = Bukkit.createInventory(null, gui.getInt("Size"), MSG.color(title));
		ItemStack bg = null;
		boolean empty = true;
		for (String res : gui.getKeys(false)) {
			if (!gui.contains(res + ".Icon"))
				continue;
			empty = false;
			if (gui.contains(res + ".Page")) {
				if (page != gui.getInt(res + ".Page"))
					continue;
			}else if(page!=0)
				continue;
			if (player.isOnline()) {
				if (gui.contains(res + ".Permission")
						&& !((Player) player).hasPermission(gui.getString(res + ".Permission"))) {
					continue;
				}
			}
			ItemStack item = parseItem(Main.plugin.gui, id + "." + res, player);
			if (res.equals("BACKGROUND_ITEM")) {
				bg = item;
				continue;
			}
			int slot = 0;
			if (!gui.contains(res + ".Slot")) {
				while (inv.getItem(slot) != null)
					slot++;
				inv.setItem(slot, item);
			} else {
				inv.setItem(gui.getInt(res + ".Slot"), item);
			}
		}
		if (empty)
			return null;
		if (bg != null) {
			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
					inv.setItem(i, bg);
				}
			}
		}
		return inv;
	}

	public static ItemStack parseItem(ConfigurationSection section, String path, OfflinePlayer player) {
		ConfigurationSection gui = section.getConfigurationSection(path);
		ItemStack item = new ItemStack(Material.valueOf(gui.getString("Icon")));
		List<String> lore = new ArrayList<String>();
		if (gui.contains("Amount"))
			item.setAmount(gui.getInt("Amount"));
		if (gui.contains("Data"))
			item.setDurability((short) gui.getInt("Data"));
		if (gui.contains("Owner")) {
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			meta.setOwner(gui.getString("Owner"));
			item.setItemMeta(meta);
		}
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
			ConfigurationSection costs = gui.getConfigurationSection("Cost");
			lore.add("");
			if (costs.getKeys(false).size() == 1) {
				String id = costs.getKeys(false).toArray()[0].toString();
				int cost = (costs.getInt(costs.getKeys(false).toArray()[0].toString()));
				lore.add(MSG.color("&c* " + cost + " " + MSG.camelCase(id))+ ((cost == 1||id.toLowerCase().endsWith("s")) ? "" : "s"));
			} else {
				lore.add(MSG.color("&aCost:"));
				for (String mat : costs.getKeys(false)) {
					if (mat.equals("XP") || mat.equals("COINS")) {
						lore.add(MSG.color("&c* " + costs.getInt(mat) + " " + MSG.camelCase(mat)));
					} else {
						lore.add(MSG.color("&c* " + costs.getInt(mat) + " " + MSG.camelCase(mat))+ ((costs.getInt(mat) == 1||mat.toLowerCase().endsWith("s")) ? "" : "s"));
					}
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
	 * Calculates a player's total exp based on level and progress to next.
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 * @param player the Player
	 * 
	 * @return the amount of exp the Player has
	 */
	public static int getExp(Player player) {
		return getExpFromLevel(player.getLevel())
				+ Math.round(getExpToNext(player.getLevel()) * player.getExp());
	}

	/**
	 * Calculates total experience based on level.
	 * 
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 * "One can determine how much experience has been collected to reach a level using the equations:
	 * 
	 *  Total Experience = [Level]2 + 6[Level] (at levels 0-15)
	 *                     2.5[Level]2 - 40.5[Level] + 360 (at levels 16-30)
	 *                     4.5[Level]2 - 162.5[Level] + 2220 (at level 31+)"
	 * 
	 * @param level the level
	 * 
	 * @return the total experience calculated
	 */
	public static int getExpFromLevel(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}

	/**
	 * Calculates level based on total experience.
	 * 
	 * @param exp the total experience
	 * 
	 * @return the level calculated
	 */
	public static double getLevelFromExp(long exp) {
		if (exp > 1395) {
			return (Math.sqrt(72 * exp - 54215) + 325) / 18;
		}
		if (exp > 315) {
			return Math.sqrt(40 * exp - 7839) / 10 + 8.1;
		}
		if (exp > 0) {
			return Math.sqrt(exp + 9) - 3;
		}
		return 0;
	}

	/**
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 * "The formulas for figuring out how many experience orbs you need to get to the next level are as follows:
	 *  Experience Required = 2[Current Level] + 7 (at levels 0-15)
	 *                        5[Current Level] - 38 (at levels 16-30)
	 *                        9[Current Level] - 158 (at level 31+)"
	 */
	private static int getExpToNext(int level) {
		if (level > 30) {
			return 9 * level - 158;
		}
		if (level > 15) {
			return 5 * level - 38;
		}
		return 2 * level + 7;
	}

	/**
	 * Change a Player's exp.
	 * <p>
	 * This method should be used in place of {@link Player#giveExp(int)}, which does not properly
	 * account for different levels requiring different amounts of experience.
	 * 
	 * @param player the Player affected
	 * @param exp the amount of experience to add or remove
	 */
	public static void changeExp(Player player, int exp) {
		exp += getExp(player);

		if (exp < 0) {
			exp = 0;
		}

		double levelAndExp = getLevelFromExp(exp);

		int level = (int) levelAndExp;
		player.setLevel(level);
		player.setExp((float) (levelAndExp - level));
	}
	


	public static String parseDecimal(String name, int length) {
		if (name.contains(".")) {
			if (name.split("\\.")[1].length() > 2) {
				name = name.split("\\.")[0] + "."
						+ name.split("\\.")[1].substring(0, Math.min(name.split("\\.")[1].length(), length));
			}
		}
		return name;
	}

}
