package eu.crushedpixel.camerastudio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CameraStudio extends org.bukkit.plugin.java.JavaPlugin implements Listener {
	private HashMap<Player, List<Location>> points = new HashMap<Player, List<Location>>();
	private HashSet<Player> stopping = new HashSet<Player>();
	private HashSet<Player> travelling = new HashSet<Player>();
	private static String prefix = ChatColor.AQUA + "[" + ChatColor.DARK_AQUA + "CP" + ChatColor.AQUA + "CameraStudio] "
			+ ChatColor.GREEN;

	public void onDisable() {
		getLogger().info("CameraStudio disabled");
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		getLogger().info("CameraStudio enabled");
	}

	public static double round(double unrounded, int precision) {
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, 4);
		return rounded.doubleValue();
	}

	@EventHandler
	public void onPlayerJoined(final PlayerJoinEvent event) {
		if (getConfig().getBoolean("show-join-message")) {
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					event.getPlayer().sendMessage(
							CameraStudio.prefix + "This server is running the Camera Studio Plugin v1.0 by "
									+ ChatColor.AQUA + "CrushedPixel");
					event.getPlayer()
							.sendMessage(CameraStudio.prefix + ChatColor.YELLOW + "http://youtube.com/CrushedPixel");
				}
			}, 10L);
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (((sender instanceof Player)) && (cmd.getName().equalsIgnoreCase("cam"))) {
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
					List<Location> locs = (List<Location>) this.points.get(player);
					if (locs == null) {
						locs = new ArrayList<Location>();
					}

					locs.add(player.getLocation());
					this.points.put(player, locs);

					player.sendMessage(prefix + "Point " + locs.size() + " has been set");

					return true;
				}

				if (subcmd.equalsIgnoreCase("r")) {
					List<Location> locs = (List<Location>) this.points.get(player);
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
								player.sendMessage(
										prefix + ChatColor.RED + "You only have 1 point set");
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

					this.points.put(player, locs);
					return true;
				}

				if (subcmd.equalsIgnoreCase("list")) {
					List<Location> locs = (List<Location>) this.points.get(player);
					if ((locs == null) || (locs.size() == 0)) {
						player.sendMessage(prefix + ChatColor.RED + "You don't have any points set");
						return true;
					}

					int i = 1;
					for (Location loc : locs) {
						player.sendMessage(prefix + "Point " + i + ": " + round(loc.getX(), 1) + ", "
								+ round(loc.getY(), 1) + ", " + round(loc.getZ(), 1) + " (" + round(loc.getYaw(), 1)
								+ ", " + round(loc.getPitch(), 1) + ")");
						i++;
					}
					return true;
				}

				if (subcmd.equalsIgnoreCase("reset")) {
					// Made an error, replaced it with the line below: this.points.put(player, new ArrayList<Object>());
					this.points.remove(player);
					player.sendMessage(prefix + "Successfully removed all points");
					return true;
				}
				
				if (subcmd.equalsIgnoreCase("reload")) {
					if (sender.hasPermission("camerastudio.admin")) {
						this.reloadConfig();
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
							List<Location> locs = (List<Location>) this.points.get(player);
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

					// Says it wasn't reffrenced, I just removed it: label1152: return true;
					return true;
				}

				if (subcmd.equalsIgnoreCase("stop")) {
					this.stopping.add(player);
					player.sendMessage(prefix + "Travelling has been cancelled");
					getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							CameraStudio.this.stopping.remove(player);
						}
					}, 2L);

					return true;
				}

				if (subcmd.equalsIgnoreCase("help")) {
					player.performCommand("help CPCameraStudioReborn");
					return true;
				}

				if (subcmd.equalsIgnoreCase("start")) {
					if (this.travelling.contains(player)) {
						player.sendMessage(prefix + ChatColor.RED + "You are already travelling");
						return true;
					}
					if (args.length == 1) {
						try {
							String timeString = args[0];

							Date length = parseTimeString(timeString);

							Calendar cal = GregorianCalendar.getInstance();
							cal.setTime(length);

							int time = (cal.get(12) * 60 + cal.get(13)) * 20;

							List<Location> locs = (List<Location>) this.points.get(player);

							if ((locs == null) || (locs.size() <= 1)) {
								player.sendMessage(prefix + ChatColor.RED + "Not enough points set");
								return true;
							}

							List<Double> diffs = new ArrayList<Double>();
							List<Integer> travelTimes = new ArrayList<Integer>();

							double totalDiff = 0.0D;

							for (int i = 0; i < locs.size() - 1; i++) {
								Location s = (Location) locs.get(i);
								Location n = (Location) locs.get(i + 1);
								double diff = positionDifference(s, n);
								totalDiff += diff;
								diffs.add(Double.valueOf(diff));
							}

							for (Iterator<Double> n = diffs.iterator(); n.hasNext();) {
								double d = ((Double) n.next()).doubleValue();
								travelTimes.add(Integer.valueOf((int) (d / totalDiff * time)));
							}

							final List<Location> tps = new ArrayList<Location>();

							org.bukkit.World w = player.getWorld();

							for (int i = 0; i < locs.size() - 1; i++) {
								Location s = (Location) locs.get(i);
								Location n = (Location) locs.get(i + 1);
								int t = ((Integer) travelTimes.get(i)).intValue();

								double moveX = n.getX() - s.getX();
								double moveY = n.getY() - s.getY();
								double moveZ = n.getZ() - s.getZ();
								double movePitch = n.getPitch() - s.getPitch();

								double yawDiff = Math.abs(n.getYaw() - s.getYaw());
								double c = 0.0D;

								if (yawDiff <= 180.0D) {
									if (s.getYaw() < n.getYaw()) {
										c = yawDiff;
									} else {
										c = -yawDiff;
									}
								} else if (s.getYaw() < n.getYaw()) {
									c = -(360.0D - yawDiff);
								} else {
									c = 360.0D - yawDiff;
								}

								double d = c / t;

								for (int x = 0; x < t; x++) {
									Location l = new Location(w, s.getX() + moveX / t * x, s.getY() + moveY / t * x,
											s.getZ() + moveZ / t * x, (float) (s.getYaw() + d * x),
											(float) (s.getPitch() + movePitch / t * x));
									tps.add(l);
								}
							}
							try {
								player.setAllowFlight(true);
								player.teleport((Location) tps.get(0));
								player.setFlying(true);
								this.travelling.add(player);
								getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									private int ticks = 0;

									public void run() {
										if (this.ticks < tps.size()) {

											player.teleport((Location) tps.get(this.ticks));

											if (!CameraStudio.this.stopping.contains(player)) {
												CameraStudio.this.getServer().getScheduler()
														.scheduleSyncDelayedTask(CameraStudio.this, this, 1L);
											} else {
												CameraStudio.this.stopping.remove(player);
												CameraStudio.this.travelling.remove(player);
											}

											this.ticks += 1;
										} else {
											player.sendMessage(CameraStudio.prefix + "Travelling finished");
											CameraStudio.this.travelling.remove(player);
										}
									}
								});
							} catch (Exception e) {
								player.sendMessage(prefix + ChatColor.RED + "An error occured during traveling");
							}
						} catch (Exception e) {
							player.sendMessage(prefix + ChatColor.RED + "You must specify the travel duration. "
									+ ChatColor.YELLOW + "Example: /start 3m10s");
						}
					} else if (args.length == 0){
						player.sendMessage(prefix + ChatColor.RED + "You must specify the travel duration. "
								+ ChatColor.YELLOW + "Example: /start 3m10s");
					} else {
						player.sendMessage(prefix + ChatColor.RED + "Too many arguements! You must only specify the travel duration. "
								+ ChatColor.YELLOW + "Example: /start 3m10s");
					}
					return true;
				}
			} else {
				player.sendMessage(prefix + ChatColor.RED + "Type " + ChatColor.WHITE + "/cam help" + ChatColor.RED
						+ " for details");
			}
		}

		return false;
	}

	public Date parseTimeString(String timeString) throws java.text.ParseException {
		Date length;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("mm'm'ss's'");
			length = formatter.parse(timeString);
		} catch (Exception e) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("m'm'ss's'");
				length = formatter.parse(timeString);
			} catch (Exception e1) {
				try {
					SimpleDateFormat formatter = new SimpleDateFormat("m'm's's'");
					length = formatter.parse(timeString);
				} catch (Exception e2) {
					try {
						SimpleDateFormat formatter = new SimpleDateFormat("mm'm's's'");
						length = formatter.parse(timeString);
					} catch (Exception e3) {
						try {
							SimpleDateFormat formatter = new SimpleDateFormat("mm'm'");
							length = formatter.parse(timeString);
						} catch (Exception e4) {
							try {
								SimpleDateFormat formatter = new SimpleDateFormat("m'm'");
								length = formatter.parse(timeString);
							} catch (Exception e5) {
								try {
									SimpleDateFormat formatter = new SimpleDateFormat("s's'");
									length = formatter.parse(timeString);
								} catch (Exception e6) {
									SimpleDateFormat formatter = new SimpleDateFormat("ss's'");
									length = formatter.parse(timeString);
								}
							}
						}
					}
				}
			}
		}

		return length;
	}

	public double positionDifference(Location cLoc, Location eLoc) {
		double cX = cLoc.getX();
		double cY = cLoc.getY();
		double cZ = cLoc.getZ();

		double eX = eLoc.getX();
		double eY = eLoc.getY();
		double eZ = eLoc.getZ();

		double dX = eX - cX;
		if (dX < 0.0D) {
			dX = -dX;
		}
		double dZ = eZ - cZ;
		if (dZ < 0.0D) {
			dZ = -dZ;
		}
		double dXZ = Math.hypot(dX, dZ);

		double dY = eY - cY;
		if (dY < 0.0D) {
			dY = -dY;
		}
		double dXYZ = Math.hypot(dXZ, dY);

		return dXYZ;
	}
}