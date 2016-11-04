package eu.crushedpixel.camerastudio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CamCommand implements CommandExecutor {

	private HashMap<UUID, List<Location>> points = new HashMap<UUID, List<Location>>();
	private HashSet<UUID> stopping = CameraStudio.stopping;
	private HashSet<UUID> travelling = CameraStudio.travelling;
	private static String prefix = CameraStudio.prefix;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (!player.hasPermission("camerastudio")) {
				player.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
				return true;
			}
			if (args.length > 0) {
				String subcmd = args[0];

				String[] newArgs = new String[args.length - 1];
				for (int p = 1; p < args.length; p++) {
					newArgs[(p - 1)] = args[p];
				}

				args = newArgs;

				if (subcmd.equalsIgnoreCase("p")) {
					List<Location> locs = (List<Location>) this.points.get(player.getUniqueId());
					if (locs == null) {
						locs = new ArrayList<Location>();
					}

					locs.add(player.getLocation());
					this.points.put(player.getUniqueId(), locs);

					player.sendMessage(prefix + "Point " + locs.size() + " has been set");

					return true;
				}

				if (subcmd.equalsIgnoreCase("r")) {
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
									player.sendMessage(
											prefix + ChatColor.RED + "You only have " + locs.size() + " points set");
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

				if (subcmd.equalsIgnoreCase("list")) {
					List<Location> locs = (List<Location>) this.points.get(player.getUniqueId());
					if ((locs == null) || (locs.size() == 0)) {
						player.sendMessage(prefix + ChatColor.RED + "You don't have any points set");
						return true;
					}

					int i = 1;
					for (Location loc : locs) {
						player.sendMessage(prefix + "Point " + i + ": " + CameraStudio.round(loc.getX(), 1) + ", "
								+ CameraStudio.round(loc.getY(), 1) + ", " + CameraStudio.round(loc.getZ(), 1) + " ("
								+ CameraStudio.round(loc.getYaw(), 1) + ", " + CameraStudio.round(loc.getPitch(), 1)
								+ ")");
						i++;
					}
					return true;
				}

				if (subcmd.equalsIgnoreCase("reset")) {
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

				if (subcmd.equalsIgnoreCase("goto")) {
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

					// Says it wasn't referenced, I just removed it: label1152:
					// return true;
					return true;
				}

				if (subcmd.equalsIgnoreCase("stop")) {
					this.stopping.add(player.getUniqueId());
					player.sendMessage(prefix + "Travelling has been cancelled");
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CameraStudio.instance, new Runnable() {
						public void run() {
							stopping.remove(player.getUniqueId());
						}
					}, 2L);

					return true;
				}

				if (subcmd.equalsIgnoreCase("help")) {
					if (args.length == 0) {
					player.performCommand("help CPCameraStudioReborn");
					return true;
					} else {
						if (args.length == 1) {
							try { 
								player.performCommand("help CPCameraStudioReborn " + Integer.parseInt(args[0]));
								return true;
						    } catch(NumberFormatException e) {
						    	player.sendMessage(prefix + ChatColor.YELLOW + args[0] + ChatColor.RED + "is not a number!");
						        return true;
						    }
						} else {
							player.sendMessage(prefix + ChatColor.RED + "Too many arguements! Usage: " + ChatColor.YELLOW + "/cam help <pagenumber>");
						}
					}
					return true;
				}

				if (subcmd.equalsIgnoreCase("start")) {
					if (travelling.contains(player.getUniqueId())) {
						player.sendMessage(prefix + ChatColor.RED + "You are already travelling");
						return true;
					}
					if (args.length == 1) {
						try {
							List<Location> locs = (List<Location>) this.points.get(player.getUniqueId());

							if ((locs == null) || (locs.size() <= 1)) {
								player.sendMessage(prefix + ChatColor.RED + "Not enough points set");
								return true;
							}

							CameraStudio.travel(player, locs, CameraStudio.parseTimeString(args[0]),
									prefix + ChatColor.RED + "An error occured during traveling",
									prefix + "Travelling finished");

						} catch (Exception e) {
							player.sendMessage(prefix + ChatColor.RED + "You must specify the travel duration. "
									+ ChatColor.YELLOW + "Example: /start 3m10s");
						}
					} else if (args.length == 0) {
						player.sendMessage(prefix + ChatColor.RED + "You must specify the travel duration. "
								+ ChatColor.YELLOW + "Example: /start 3m10s");
					} else {
						player.sendMessage(prefix + ChatColor.RED
								+ "Too many arguements! You must only specify the travel duration. " + ChatColor.YELLOW
								+ "Example: /start 3m10s");
					}
					return true;
				}
				if (subcmd.equalsIgnoreCase("save")) {
					if (this.travelling.contains(player.getUniqueId())) {
						player.sendMessage(prefix + ChatColor.RED + "You are currently travelling");
						return true;
					}
					if (args.length == 1) {
						if (points.get(player.getUniqueId()) != null) {
							List<String> ListOfLocations = new ArrayList<String>();
							for (Location loc : points.get(player.getUniqueId())) {
								ListOfLocations.add(Files.getSerializedLocation(loc));
							}
							Files.save(ListOfLocations, (new File(CameraStudio.instance.getDataFolder() + "/SavedPaths", args[0] + ".dat")));
							player.sendMessage(prefix + ChatColor.YELLOW + "Path: " + ChatColor.BLUE + args[0]
									+ ChatColor.YELLOW + " has been saved!");
							return true;
						} else {
							player.sendMessage(prefix + ChatColor.RED + "You do not have any points set to save!");
						}
					} else {
						if (args.length >= 2) {
							player.sendMessage(prefix + ChatColor.RED + "Too many arguements! Usage: "
									+ ChatColor.YELLOW + "/cam save <savename>");
							return true;
						} else {
							player.sendMessage(prefix + ChatColor.RED + "Too few arguements! Usage: " + ChatColor.YELLOW
									+ "/cam save <savename>");
							return true;
						}
					}
				}
				if (subcmd.equalsIgnoreCase("load")) {
					if (this.travelling.contains(player.getUniqueId())) {
						player.sendMessage(prefix + ChatColor.RED + "You are currently travelling");
						return true;
					}
					if (args.length == 1) {
							@SuppressWarnings("unchecked")
							List<String> ListOfLocationsStrings = (List<String>) Files
									.load(new File(CameraStudio.instance.getDataFolder() + "/SavedPaths", args[0] + ".dat"));
							if (ListOfLocationsStrings != null) {
								List<Location> ListOfLocations = new ArrayList<Location>();
								for (String string : ListOfLocationsStrings) {
									ListOfLocations.add(Files.getDeserializedLocation(string));
								}
								points.put(player.getUniqueId(), ListOfLocations);
								player.sendMessage(prefix + ChatColor.YELLOW + "Path: " + ChatColor.BLUE + args[0]
										+ ChatColor.YELLOW + " has been loaded!");
								return true;
							} else {
								player.sendMessage(prefix + ChatColor.RED + "Invalid file!");
								return true;
							}
					} else {
						if (args.length >= 1) {
							player.sendMessage(prefix + ChatColor.RED + "Too many arguements! Usage: "
									+ ChatColor.YELLOW + "/cam save <savename>");
							return true;
						} else {
							player.sendMessage(prefix + ChatColor.RED + "Too few arguements! Usage: " + ChatColor.YELLOW
									+ "/cam save <savename>");
							return true;
						}
					}
				}
			} else {
				player.sendMessage(prefix + ChatColor.RED + "Type " + ChatColor.WHITE + "/cam help" + ChatColor.RED
						+ " for details");
			}
		} else {
			sender.sendMessage("You must be a player to use this command!");
			return true;
		}

		return false;
	}

}
