package fr.nashoba24.prefixlogo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PrefixLogoCommand implements CommandExecutor, Listener {
	
	static HashMap<String, String> logo = new HashMap<String, String>();
	static HashMap<String, String> players = new HashMap<String, String>();
	static ArrayList<String> premiumLogo = new ArrayList<String>();
	static ArrayList<Inventory> pagesDefault = new ArrayList<Inventory>();
	static ArrayList<Inventory> pagesPremium = new ArrayList<Inventory>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(!p.hasPermission("wolvmc.logo.choose")) {
				p.sendMessage(ChatColor.RED + "You don't have the permission to execute this command!");
				return true;
			}
			if(args.length==0) {
				if(!p.hasPermission("wolvmc.logo.change")) {
					if(PrefixLogoCommand.getPlayerLogo(p.getName())!=null) {
						p.sendMessage(ChatColor.RED + "You can't change your logo!");
						return true;
					}
				}
				PrefixLogoCommand.openLogoPage(p, 1);
				return true;
			}
			else if(args.length==1){
				if(!logo.containsKey(args[0].toLowerCase())) {
					p.sendMessage(ChatColor.RED + "Unknown logo!");
					return true;
				}
				else {
					if(!p.hasPermission("wolvmc.logo.change")) {
						if(PrefixLogoCommand.getPlayerLogo(p.getName())!=null) {
							p.sendMessage(ChatColor.RED + "You can't change your logo!");
							return true;
						}
					}
					if(!p.hasPermission("wolvmc.logo.premium")) {
						if(PrefixLogoCommand.isPremium(args[0].toLowerCase())) {
							p.sendMessage(ChatColor.RED + "This logo is a premium logo!");
							return true;
						}
					}
					PrefixLogoCommand.setPlayerLogo(p, PrefixLogoCommand.getLogo(args[0].toLowerCase()), true);
					p.sendMessage(ChatColor.GREEN + "Your logo is now " + ChatColor.RESET + PrefixLogoCommand.getLogo(args[0].toLowerCase()));
					return true;
				}
			}
			else {
				return false;
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Only a player can execute that command!");
			return true;
		}
	}
	
	public static String getLogo(String id) {
		if(logo.containsKey(id)) {
			return logo.get(id);
		}
		else {
			return null;
		}
	}
	
	public static boolean isPremium(String id) {
		if(premiumLogo.contains(id)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static String getPlayerLogo(String name) {
		if(players.containsKey(name)) {
			return players.get(name);
		}
		else {
			return null;
		}
	}
	
	public static void setPlayerLogo(String name, String logo) {
		players.put(name, logo);

	}
	
	public static void setPlayerLogo(Player p, String logo, boolean save) {
		players.put(p.getName(), logo);
		if(save) {
			PrefixLogo.save(p);
		}
	}
	
	public static void openLogoPage(Player p, Integer page) {
		if(p.hasPermission("wolvmc.logo.premium")) {
			if(pagesPremium.size()>=page) {
				p.openInventory(pagesPremium.get(page - 1));
			}
			else {
				return;
			}
		}
		else {
			if(pagesDefault.size()<=page) {
				p.openInventory(pagesDefault.get(page - 1));
			}
			else {
				return;
			}
		}
	}
	
	public static void reloadLogo() {
		File file = new File(PrefixLogo.getPlugin(PrefixLogo.class).getDataFolder() + "/logo.yml");
		if(!file.exists()) {
			  InputStream stream = PrefixLogo.class.getClassLoader().getResourceAsStream("fr/nashoba24/prefixlogo/logo.yml");
			  FileOutputStream fos = null;
			  try {
			      fos = new FileOutputStream(PrefixLogo.getPlugin(PrefixLogo.class).getDataFolder() + "/logo.yml");
			      byte[] buf = new byte[2048];
			      int r = stream.read(buf);
			      while(r != -1) {
			          fos.write(buf, 0, r);
			          r = stream.read(buf);
			      }
			      fos.close();
			  } catch (IOException e1) {
				e1.printStackTrace();
			  }
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		Set<String> keys = config.getKeys(false);
		Integer left = keys.size();
		Inventory invD = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Choose your logo!");
		Inventory invP = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Choose your logo!");
		Integer pageCountD = 1;
		Integer pageCountP = 1;
		Integer sizeD = 0;
		Integer sizeP = 0;
		for(String key : keys) {
			ConfigurationSection sect = config.getConfigurationSection(key);
			if(sect.isSet("logo")) {
				PrefixLogoCommand.logo.put(key, ChatColor.translateAlternateColorCodes('&', sect.getString("logo")));
				ItemStack is = new ItemStack(Material.GOLD_BLOCK, 1);
				ItemMeta meta = is.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', sect.getString("logo")));
				is.setItemMeta(meta);
				--left;
				if(sect.isSet("premium")) {
					if(sect.getBoolean("premium")) {
						PrefixLogoCommand.premiumLogo.add(sect.getString("id"));
						invP.addItem(is);
						++sizeP;
						if(sizeP==45 && pageCountP>1) {
							ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
							ItemMeta pageMeta = pageItem.getItemMeta();
							pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountP - 1) + " ←");
							pageItem.setItemMeta(pageMeta);
							invP.addItem(pageItem);
							++sizeP;
						}
						if(sizeP==53 && left>1) {
							pagesPremium.add(invP);
							ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
							ItemMeta pageMeta = pageItem.getItemMeta();
							pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountP + 1) + " →");
							pageItem.setItemMeta(pageMeta);
							invP.addItem(pageItem);
							invP = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Choose your logo!");
							sizeP = 0;
							++pageCountP;
						}
					}
					else {
						invD.addItem(is);
						++sizeD;
						if(sizeD==45 && pageCountD>1) {
							ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
							ItemMeta pageMeta = pageItem.getItemMeta();
							pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountD - 1) + " ←");
							pageItem.setItemMeta(pageMeta);
							invD.addItem(pageItem);
							++sizeD;
						}
						if(sizeD==53 && left>1) {
							pagesPremium.add(invD);
							ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
							ItemMeta pageMeta = pageItem.getItemMeta();
							pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountD + 1) + " →");
							pageItem.setItemMeta(pageMeta);
							invD.addItem(pageItem);
							invD = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Choose your logo!");
							sizeD = 0;
							++pageCountD;
						}
						invP.addItem(is);
						++sizeP;
						if(sizeP==45 && pageCountP>1) {
							ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
							ItemMeta pageMeta = pageItem.getItemMeta();
							pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountP - 1) + " ←");
							pageItem.setItemMeta(pageMeta);
							invP.addItem(pageItem);
							++sizeP;
						}
						if(sizeP==53 && left>1) {
							pagesPremium.add(invP);
							ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
							ItemMeta pageMeta = pageItem.getItemMeta();
							pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountP + 1) + " →");
							pageItem.setItemMeta(pageMeta);
							invP.addItem(pageItem);
							invP = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Choose your logo!");
							sizeP = 0;
							++pageCountP;
						}
					}
				}
				else {
					invD.addItem(is);
					++sizeD;
					if(sizeD==45 && pageCountD>1) {
						ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
						ItemMeta pageMeta = pageItem.getItemMeta();
						pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountD - 1) + " ←");
						pageItem.setItemMeta(pageMeta);
						invD.addItem(pageItem);
						++sizeD;
					}
					if(sizeD==53 && left>1) {
						pagesPremium.add(invD);
						ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
						ItemMeta pageMeta = pageItem.getItemMeta();
						pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountD + 1) + " →");
						pageItem.setItemMeta(pageMeta);
						invD.addItem(pageItem);
						invD = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Choose your logo!");
						sizeD = 0;
						++pageCountD;
					}
					invP.addItem(is);
					++sizeP;
					if(sizeP==45 && pageCountP>1) {
						ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
						ItemMeta pageMeta = pageItem.getItemMeta();
						pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountP - 1) + " ←");
						pageItem.setItemMeta(pageMeta);
						invP.addItem(pageItem);
						++sizeP;
					}
					if(sizeP==53 && left>1) {
						pagesPremium.add(invP);
						ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
						ItemMeta pageMeta = pageItem.getItemMeta();
						pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountP + 1) + " →");
						pageItem.setItemMeta(pageMeta);
						invP.addItem(pageItem);
						invP = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Choose your logo!");
						sizeP = 0;
						++pageCountP;
					}
				}
			}
		}
		if(!invP.contains(Material.DIAMOND_BLOCK) && pageCountP>1) {
			ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
			ItemMeta pageMeta = pageItem.getItemMeta();
			pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountP - 1) + " ←");
			pageItem.setItemMeta(pageMeta);
			invP.setItem(45, pageItem);
		}
		if(!invD.contains(Material.DIAMOND_BLOCK) && pageCountD>1) {
			ItemStack pageItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
			ItemMeta pageMeta = pageItem.getItemMeta();
			pageMeta.setDisplayName(ChatColor.BLUE + "Page " + (pageCountD - 1) + " ←");
			pageItem.setItemMeta(pageMeta);
			invD.setItem(45, pageItem);
		}
		pagesPremium.add(invP);
		pagesDefault.add(invD);
	}
	
	  @EventHandler
	  public void onInventoryLogoClick(InventoryClickEvent e) {
		  if(e.getInventory().getName().equals(ChatColor.BLUE + "Choose your logo!")) {
			  e.setCancelled(true);
			  ItemStack is = e.getInventory().getItem(e.getSlot());
			  if(is!=null) {
				  if(is.getType()==Material.DIAMOND_BLOCK) {
					  String[] list = is.getItemMeta().getDisplayName().split(" ");
					  Integer page = Integer.parseInt(list[1]);
					  PrefixLogoCommand.openLogoPage((Player) e.getWhoClicked(), page);
				  }
				  else if(is.getType()==Material.GOLD_BLOCK) {
					  for (Entry<String, String> entry : logo.entrySet())
					  {
						  if(entry.getValue().equals(is.getItemMeta().getDisplayName())) {
							  PrefixLogoCommand.setPlayerLogo((Player) e.getWhoClicked(), entry.getValue(), true);
							  e.getWhoClicked().sendMessage(ChatColor.GREEN + "Your logo is now " + entry.getValue());
							  e.getWhoClicked().closeInventory();
							  return;
						  }
					  }
				  }
			  }
		  }
	  }
	  
	  @EventHandler
	  public void onJoin(PlayerJoinEvent e) {
		  PrefixLogo.load(e.getPlayer());
	  }
}
