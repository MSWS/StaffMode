package org.mswsplex.staff.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mswsplex.staff.managers.PlayerManager;
import org.mswsplex.staff.msws.Main;

public class MSG {
	public static String color(String msg) {
		if (msg == null||msg.isEmpty())
			return null;
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static String camelCase(String string) {
		String prevChar = " ";
		String res = "";
		for (int i = 0; i < string.length(); i++) {
			if (i > 0)
				prevChar = string.charAt(i - 1) + "";
			if (!prevChar.matches("[a-zA-Z]")) {
				res = res + ((string.charAt(i) + "").toUpperCase());
			} else {
				res = res + ((string.charAt(i) + "").toLowerCase());
			}
		}
		return res.replace("_", " ");
	}

	public static String getString(String id, String def) {
		return Main.plugin.lang.contains(id) ? Main.plugin.lang.getString(id) : "["+id+"] " + def;
	}

	public static void tell(CommandSender sender, String msg) {
		if (msg != null&&!msg.isEmpty())
			sender.sendMessage(color(msg.replace("%prefix%", prefix())));
	}

	public static void tell(World world, String msg) {
		if (world != null && msg != null) {
			for (Player target : world.getPlayers()) {
				tell(target, msg);
			}
		}
	}

	public static void tell(String perm, String msg) {
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (target.hasPermission(perm))
				tell(target, msg);
		}
	}

	public static String prefix() {
		return Main.plugin.config.contains("Prefix") ? Main.plugin.config.getString("Prefix") : "&9Plugin>&7";
	}

	public static void noPerm(CommandSender sender) {
		tell(sender, getString("NoPermission", "Insufficient Permissions"));
	}

	public static void log(String msg) {
		tell(Bukkit.getConsoleSender(), "[" + Main.plugin.getDescription().getName() + "] " + msg);
	}

	public static String TorF(Boolean bool) {
		if (bool) {
			return "&aTrue&r";
		} else {
			return "&cFalse&r";
		}
	}
	
	public static void noSpam(Player player, String msg) {
		noSpam(player, msg, 1000);
	}
	
	public static void noSpam(Player player, String msg, double delay) {
		if(PlayerManager.getInfo(player, msg+"msg")==null) {
			tell(player, msg);
			PlayerManager.setInfo(player, msg+"msg", System.currentTimeMillis());
			return;
		}
		if(System.currentTimeMillis() - PlayerManager.getDouble(player, msg+"msg")>delay) {
			tell(player, msg);
			PlayerManager.setInfo(player, msg+"msg", System.currentTimeMillis());
		}
	}
	
	public static void sendHelp(CommandSender sender, int page, String command) {
		if (!Main.plugin.lang.contains("Help." + command.toLowerCase())) {
			tell(sender, getString("UnknownCommand", "There is no help available for this command."));
			return;
		}
		int length = Main.plugin.config.getInt("HelpLength");
		List<String> help = Main.plugin.lang.getStringList("Help." + command.toLowerCase()),
				list = new ArrayList<String>();
		for (String res : help) {
			if (res.startsWith("perm:")) {
				String perm = "";
				res = res.substring(5, res.length());
				for (char a : res.toCharArray()) {
					if (a == ' ')
						break;
					perm = perm + a;
				}
				if (!sender.hasPermission("crystal." + perm))
					continue;
				res = res.replace(perm + " ", "");
			}
			list.add(res);
		}
		if (help.size() > length)
			tell(sender, "Page: " + (page + 1) + " of " + (int) Math.ceil((list.size() / length) + 1));
		for (int i = page * length; i < list.size() && i < page * length + length; i++) {
			String res = list.get(i);
			tell(sender, res);
		}
		if (command.equals("default"))
			tell(sender, "&d&lPlugin &ev" + Main.plugin.getDescription().getVersion() + " &7created by &bMSWS");
	}
	
	public static String progressBar(double prog, double total, int length) {
		return progressBar("&a\u258D", "&c\u258D", prog, total, length);
	}

	public static String progressBar(String progChar, String incomplete, double prog, double total, int length) {
		String disp = "";
		double progress = Math.abs(prog / total);
		int len = length;
		for (double i = 0; i < len; i++) {
			if (i / len < progress) {
				disp = disp + progChar;
			} else {
				disp = disp + incomplete;
			}
		}
		return color(disp);
	}

	/**
	 * if oldVer is < newVer, both versions can only have numbers and .'s Outputs:
	 * 5.5, 10.3 | true 2.3.1, 3.1.4.6 | true 1.2, 1.1 | false
	 **/
	public static Boolean outdated(String oldVer, String newVer) {
		oldVer = oldVer.replace(".", "");
		newVer = newVer.replace(".", "");
		Double oldV = null, newV = null;
		try {
			oldV = Double.valueOf(oldVer);
			newV = Double.valueOf(newVer);
		} catch (Exception e) {
			log("&cError! &7Versions incompatible.");
			return false;
		}
		if (oldVer.length() > newVer.length()) {
			newV = newV * (10 * (oldVer.length() - newVer.length()));
		} else if (oldVer.length() < newVer.length()) {
			oldV = oldV * (10 * (newVer.length() - oldVer.length()));
		}
		return oldV < newV;
	}
}
