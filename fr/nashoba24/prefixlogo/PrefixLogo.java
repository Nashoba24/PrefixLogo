package fr.nashoba24.prefixlogo;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class PrefixLogo extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		File file = new File(this.getDataFolder() + "/");
		if(!file.exists()) {
			file.mkdir();
		}
		file = new File(this.getDataFolder() + "/saves/");
		if(!file.exists()) {
			file.mkdir();
		}
		getCommand("logo").setExecutor(new PrefixLogoCommand());
		Bukkit.getPluginManager().registerEvents(new PrefixLogoCommand(), this);
		PrefixLogoCommand.reloadLogo();
		Bukkit.getLogger().fine("PrefixLogo Enabled!");
	}
	
	public static void save(Player p) {
		File file = new File(PrefixLogo.getPlugin(PrefixLogo.class).getDataFolder() + "/saves/" + p.getUniqueId().toString() + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
		conf.set("logo", PrefixLogoCommand.getPlayerLogo(p.getName()));
		try {
			conf.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void load(Player p) {
		File file = new File(PrefixLogo.getPlugin(PrefixLogo.class).getDataFolder() + "/saves/" + p.getUniqueId().toString() + ".yml");
		if(file.exists()) {
			FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
			if(conf.isSet("logo")) {
				PrefixLogoCommand.setPlayerLogo(p.getName(), conf.getString("logo"));
			}
		}
	}
}
