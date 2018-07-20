package org.mswsplex.staff.msws;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mswsplex.staff.commands.StaffCommand;
import org.mswsplex.staff.events.Events;
import org.mswsplex.staff.managers.PlayerManager;
import org.mswsplex.staff.utils.MSG;

public class Main extends JavaPlugin {
	public static Main plugin;

	public FileConfiguration config, data, lang, gui;
	public File configYml = new File(getDataFolder(), "config.yml"), dataYml = new File(getDataFolder(), "data.yml"),
			langYml = new File(getDataFolder(), "lang.yml"), guiYml = new File(getDataFolder(), "guis.yml");

	/**
	 * Permissions:
	 * staffmode.toggle - Usage of /staff
	 * staffmode.vanish.[tier] (Higher numbers can see lower and equal numbers)
	 */
	
	
	public void onEnable() {
		plugin = this;
		if (!configYml.exists())
			saveResource("config.yml", true);
		if (!langYml.exists())
			saveResource("lang.yml", true);
		if (!guiYml.exists())
			saveResource("guis.yml", true);
		config = YamlConfiguration.loadConfiguration(configYml);
		data = YamlConfiguration.loadConfiguration(dataYml);
		lang = YamlConfiguration.loadConfiguration(langYml);
		gui = YamlConfiguration.loadConfiguration(guiYml);

		new StaffCommand();
		new Events();
		MSG.log("&aSuccessfully Enabled!");
	}

	public void onDisable() {
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (PlayerManager.getStaffMode(target)) {
				PlayerManager.disableStaffMode(target);
				MSG.tell(target, MSG.getString("Staff.Reload", "your status has been disabled")
						.replace("%status%", MSG.getString("Disabled", "disabled")));
			}
		}
		Main.plugin.saveData();
		plugin = null;
	}

	public void saveData() {
		try {
			data.save(dataYml);
		} catch (Exception e) {
			MSG.log("&cError saving data file");
			MSG.log("&a----------Start of Stack Trace----------");
			e.printStackTrace();
			MSG.log("&a----------End of Stack Trace----------");
		}
	}

	public void saveConfig() {
		try {
			config.save(configYml);
		} catch (Exception e) {
			MSG.log("&cError saving data file");
			MSG.log("&a----------Start of Stack Trace----------");
			e.printStackTrace();
			MSG.log("&a----------End of Stack Trace----------");
		}
	}
}
