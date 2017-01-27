package eu.crushedpixel.camerastudio;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CamCommand implements CommandExecutor {

	private HashMap<UUID, List<Location>> points = new HashMap<UUID, List<Location>>();
	private static String prefix = CameraStudio.prefix;
	final static String previewTime = CameraStudio.instance.getConfig().getString("preview-time");

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use this command!");
			return true;
		}
		final Player player = (Player) sender;
		if (!player.hasPermission("camerastudio")) {
			player.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
			return true;
		}
		if (args.length == 0) {
			player.sendMessage(
					prefix + ChatColor.RED + "Type " + ChatColor.WHITE + "/cam help" + ChatColor.RED + " for details");
			return true;
		}
		String subcmd = args[0];

		String[] newArgs = new String[args.length - 1];
		for (int p = 1; p < args.length; p++) {
			newArgs[(p - 1)] = args[p];
		}

		args = newArgs;

		if (subcmd.equalsIgnoreCase("p") && (player.hasPermission("camerastudio.point"))) {
			List<Location> locs = (List<Location>) this.points.get(player.getUniqueId());
			if (locs == null) {
				locs = new ArrayList<Location>();
			}

			locs.add(player.getLocation());
			this.points.put(player.getUniqueId(), locs);

			player.sendMessage(prefix + "Point " + locs.size() + " has been set");

			return true;
		}

		if (subcmd.equalsIgnoreCase("r") && (player.hasPermission("camerastudio.remove"))) {
			List<Location> locs = (List<Location>) this.points.get(player.getUniqueId());
			if (locs == null) {
				locs = new ArrayList<Location>();
			}

			if (args.length == 0) {
				if (locs.size() > 0) {
					locs.remove(locs.size() - 1);
					player.sendMessage(prefix + "Point " + (locs.size() + 1) + " has been removed");
				} else {
					player.sendMessage(prefix + ChatColor.RED + "You don't have any points set");
					return true;
				}
			} else if (args.length == 1) {
				try {
					int pos = Integer.valueOf(args[0]).intValue();
					if (locs.size() >= pos) {
						locs.remove(pos - 1);
						player.sendMessage(prefix + "Point " + pos + " has been removed");
					} else {
						if (locs.size() == 1) {
							player.sendMessage(prefix + ChatColor.RED + "You only have 1 point set");
							return true;
						} else {
							player.sendMessage(prefix + ChatColor.RED + "You only have " + locs.size() + " points set");
							return true;
						}
					}
				} catch (Exception e) {
					player.sendMessage(prefix + ChatColor.RED + args[0] + " is not a valid number");
					return true;
				}
			}

			this.points.put(player.getUniqueId(), locs);
			return true;
		}

		if (subcmd.equalsIgnoreCase("list") && (player.hasPermission("camerastudio.list"))) {
			List<Location> locs = (List<Location>) this.points.get(player.getUniqueId());
			if ((locs == null) || (locs.size() == 0)) {
				player.sendMessage(prefix + ChatColor.RED + "You don't have any points set");
				return true;
			}

			int i = 1;
			for (Location loc : locs) {
				player.sendMessage(prefix + "Point " + i + ": " + CameraStudio.round(loc.getX(), 1) + ", "
						+ CameraStudio.round(loc.getY(), 1) + ", " + CameraStudio.round(loc.getZ(), 1) + " ("
						+ CameraStudio.round(loc.getYaw(), 1) + ", " + CameraStudio.round(loc.getPitch(), 1) + ")");
				i++;
			}
			return true;
		}

		if (subcmd.equalsIgnoreCase("reset") && (player.hasPermission("camerastudio.reset"))) {
			// Made an error, replaced it with the line below:
			// this.points.put(player, new ArrayList<Object>());
			this.points.remove(player.getUniqueId());
			player.sendMessage(prefix + "Successfully removed all points");
			return true;
		}

		if (subcmd.equalsIgnoreCase("reload")) {
			if (sender.hasPermission("camerastudio.admin")) {
				CameraStudio.instance.reloadConfig();
				sender.sendMessage(prefix + ChatColor.YELLOW + "The configuration files have been reloaded!");
				return true;
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
				return true;
			}
		}

		if (subcmd.equalsIgnoreCase("goto") && (player.hasPermission("camerastudio.goto"))) {
			if (args.length == 1) {
				try {
					int pos = Integer.valueOf(args[0]).intValue();
					List<Location> locs = (List<Location>) this.points.get(player.getUniqueId());
					if ((locs != null) && (locs.size() >= pos)) {
						player.teleport((Location) locs.get(pos - 1));
						player.sendMessage(prefix + "Teleported to Point " + pos);
						return true; // This was "break label1152;"
					}
					if (locs == null) {
						player.sendMessage(prefix + ChatColor.RED + "You don't have any points set");
						return true;
					}
					player.sendMessage(prefix + ChatColor.RED + "You only have " + locs.size() + " points set");
					return true;
				} catch (Exception e) {
					player.sendMessage(prefix + ChatColor.RED + args[0] + " is not a valid number");
					return true;
				}
			}
			player.sendMessage(prefix + ChatColor.RED + "You must specify the point you want to teleport to");

			// Says it wasn't referenced, I just removed it:
			// label1152:
			// return true;
			return true;
		}

		if (subcmd.equalsIgnoreCase("stop") && (player.hasPermission("camerastudio.stop"))) {
			CameraStudio.stop(player.getUniqueId());
			player.sendMessage(prefix + "Travelling has been cancelled");
			return true;
		}

		if (subcmd.equalsIgnoreCase("help") && (player.hasPermission("camerastudio.help"))) {
			if (args.length == 0) {
				player.performCommand("help CPCameraStudioReborn");
				return true;
			} else {
				if (args.length == 1) {
					try {
						player.performCommand("help CPCameraStudioReborn " + Integer.parseInt(args[0]));
						return true;
					} catch (NumberFormatException e) {
						player.sendMessage(prefix + ChatColor.YELLOW + args[0] + ChatColor.RED + "is not a number!");
						return true;
					}
				} else {
					player.sendMessage(prefix + ChatColor.RED + "Too many arguements! Usage: " + ChatColor.YELLOW
							+ "/cam help <pagenumber>");
				}
			}
			return true;
		}

		if ((subcmd.equalsIgnoreCase("start") && (player.hasPermission("camerastudio.start")))
				|| (subcmd.equalsIgnoreCase("preview") && (player.hasPermission("camerastudio.preview")))) {

			if (args.length == 0 && !subcmd.equalsIgnoreCase("preview")) {
				player.sendMessage(prefix + ChatColor.RED + "You must specify the travel duration. " + ChatColor.YELLOW
						+ "Example: /start 3m10s");
			} else {

				List<String> argsList = new ArrayList<String>();
				List<Location> listOfLocs = new ArrayList<Location>();
				if (this.points.get(player.getUniqueId()) != null)
					listOfLocs.addAll(this.points.get(player.getUniqueId()));
				boolean fileLoaded = false;
				Player currentPlayer = player;
				for (String string : args) {
					argsList.add(string);

					File file = new File(CameraStudio.instance.getDataFolder() + "/SavedPaths", string + ".yml");
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
					List<String> ListOfLocationsStrings = yaml.getStringList("Locations");
					if (!ListOfLocationsStrings.isEmpty()) {
						listOfLocs.clear();
						for (String string2 : ListOfLocationsStrings) {
							listOfLocs.add(Files.getDeserializedLocation(string2));
						}
						fileLoaded = true;
					}

					if (Bukkit.getPlayer(string) != null)
						currentPlayer = Bukkit.getPlayer(string);
				}

				if (CameraStudio.isTravelling(currentPlayer.getUniqueId())) {
					currentPlayer.sendMessage(prefix + ChatColor.RED + "You are already travelling");
					return true;
				}
				
				if (listOfLocs.isEmpty()) {
					if (fileLoaded)
						player.sendMessage(prefix + ChatColor.RED + "File specified was either invalid or empty.");
					else
						player.sendMessage(prefix + ChatColor.RED + "Not enough points set.");
					return true;
				}

				if (listOfLocs.size() <= 1) {
					if (fileLoaded)
						player.sendMessage(prefix + ChatColor.RED + "Not enough points set in file.");
					else
						player.sendMessage(prefix + ChatColor.RED + "Not enough points set.");
					return true;
				}
				try {
					int time = CameraStudio.parseTimeString(previewTime) * (listOfLocs.size() - 1);
					if (subcmd.equalsIgnoreCase("start")) {
						time = CameraStudio.parseTimeString(args[0]);
					}
					if (argsList.contains("silent")) {

						CameraStudio.travel(currentPlayer, listOfLocs, time, null, null);
					} else {
						CameraStudio.travel(currentPlayer, listOfLocs, time,
								prefix + ChatColor.RED + "An error occured during traveling",
								prefix + "Travelling finished");
					}
				} catch (ParseException e) {
					player.sendMessage(prefix + ChatColor.RED + "You must specify the travel duration. "
							+ ChatColor.YELLOW + "Example: /start 3m10s");
				}
				return true;
			}
			if (subcmd.equalsIgnoreCase("save") && (player.hasPermission("camerastudio.save"))) {
				if (args.length == 1) {
					if (points.get(player.getUniqueId()) != null) {

						File file = new File(CameraStudio.instance.getDataFolder() + "/SavedPaths", args[0] + ".yml");

						try {
							file.getParentFile().mkdirs();
							file.createNewFile();
						} catch (IOException e) {
							player.sendMessage(prefix + ChatColor.RED
									+ "An error occured while creating the file! Is there enough space left?");
							return true;
						}

						YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

						Collection<String> ListOfLocations = new ArrayList<String>();
						for (Location loc : points.get(player.getUniqueId())) {
							ListOfLocations.add(Files.getSerializedLocation(loc));
						}

						yaml.set("Locations", ListOfLocations);

						try {
							yaml.save(file);
						} catch (IOException e) {
							player.sendMessage(prefix + ChatColor.RED
									+ "An error occured while creating the file! Is there enough space left?");
							return true;
						}

						player.sendMessage(prefix + ChatColor.YELLOW + "Path: " + ChatColor.BLUE + args[0]
								+ ChatColor.YELLOW + " has been saved!");
						return true;
					} else {
						player.sendMessage(prefix + ChatColor.RED + "You do not have any points set to save!");
					}
				} else {
					if (args.length >= 2) {
						player.sendMessage(prefix + ChatColor.RED + "Too many arguements! Usage: " + ChatColor.YELLOW
								+ "/cam save <savename>");
						return true;
					} else {
						player.sendMessage(prefix + ChatColor.RED + "Too few arguements! Usage: " + ChatColor.YELLOW
								+ "/cam save <savename>");
						return true;
					}
				}
			}

			if (subcmd.equalsIgnoreCase("load") && (player.hasPermission("camerastudio.load"))) {
				if (CameraStudio.isTravelling(player.getUniqueId())) {
					player.sendMessage(prefix + ChatColor.RED + "You are currently travelling");
					return true;
				}
				if (args.length == 1) {
					File file = new File(CameraStudio.instance.getDataFolder() + "/SavedPaths", args[0] + ".yml");
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
					List<String> ListOfLocationsStrings = yaml.getStringList("Locations");
					List<Location> ListOfLocations = new ArrayList<Location>();
					for (String string : ListOfLocationsStrings) {
						ListOfLocations.add(Files.getDeserializedLocation(string));
					}
					if (ListOfLocations.isEmpty()) {
						player.sendMessage(prefix + ChatColor.RED + "File " + ChatColor.GREEN + args[1] + ChatColor.RED
								+ " was either invalid or empty. No points have been loaded!");
						return true;
					}
					points.put(player.getUniqueId(), ListOfLocations);
					player.sendMessage(prefix + ChatColor.YELLOW + "Path: " + ChatColor.BLUE + args[0]
							+ ChatColor.YELLOW + " has been loaded!");
					return true;
				} else {
					if (args.length >= 1) {
						player.sendMessage(prefix + ChatColor.RED + "Too many arguements! Usage: " + ChatColor.YELLOW
								+ "/cam save <savename>");
						return true;
					} else {
						player.sendMessage(prefix + ChatColor.RED + "Too few arguements! Usage: " + ChatColor.YELLOW
								+ "/cam save <savename>");
						return true;
					}
				}
			}
		}
		return false;
	}
}
