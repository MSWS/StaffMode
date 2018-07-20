package org.mswsplex.staff.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mswsplex.staff.managers.PlayerManager;
import org.mswsplex.staff.msws.Main;
import org.mswsplex.staff.utils.MSG;

public class StaffCommand implements CommandExecutor {
	public StaffCommand() {
		Main.plugin.getCommand("staff").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("staffmode.toggle")) {
			MSG.noPerm(sender);
			return true;
		}

		if (!(sender instanceof Player)) {
			MSG.tell(sender, MSG.getString("MustBePlayer", "You must be a player"));
			return true;
		}

		Player player = (Player) sender;
		if (args.length == 0) {
			PlayerManager.setInfo(player, "staffmode", !PlayerManager.getStaffMode(player));
			if (PlayerManager.getStaffMode(player)) {
				PlayerManager.enableStaffMode(player);
			} else {
				PlayerManager.disableStaffMode(player);
			}
			return true;
		}
		switch (args[0].toLowerCase()) {
		case "reload":
			Main.plugin.configYml = new File(Main.plugin.getDataFolder(), "config.yml");
			Main.plugin.config = YamlConfiguration.loadConfiguration(Main.plugin.configYml);
			Main.plugin.langYml = new File(Main.plugin.getDataFolder(), "lang.yml");
			Main.plugin.lang = YamlConfiguration.loadConfiguration(Main.plugin.langYml);
			Main.plugin.guiYml = new File(Main.plugin.getDataFolder(), "guis.yml");
			Main.plugin.gui = YamlConfiguration.loadConfiguration(Main.plugin.guiYml);
			MSG.tell(sender, MSG.getString("Reloaded", "Successfully reloaded."));
			break;
		case "reset":
			Main.plugin.saveResource("config.yml", true);
			Main.plugin.saveResource("lang.yml", true);
			Main.plugin.saveResource("guis.yml", true);
			Main.plugin.configYml = new File(Main.plugin.getDataFolder(), "config.yml");
			Main.plugin.langYml = new File(Main.plugin.getDataFolder(), "lang.yml");
			Main.plugin.config = YamlConfiguration.loadConfiguration(Main.plugin.configYml);
			Main.plugin.lang = YamlConfiguration.loadConfiguration(Main.plugin.langYml);
			Main.plugin.guiYml = new File(Main.plugin.getDataFolder(), "guis.yml");
			Main.plugin.gui = YamlConfiguration.loadConfiguration(Main.plugin.guiYml);
			MSG.tell(sender, MSG.prefix() + " Succesfully reset.");
			break;
		default:
			return false;
		}
		return true;
	}
}
