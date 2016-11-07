package eu.crushedpixel.camerastudio;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Files {

	public static String getSerializedLocation(Location loc) {
        return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getUID() + ";" + loc.getPitch() + ";" + loc.getYaw();
    }
 
    public static Location getDeserializedLocation(String s) {
            String [] parts = s.split(";");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            UUID u = UUID.fromString(parts[3]);
            float pitch = Float.parseFloat(parts[4]);
            float yaw = Float.parseFloat(parts[5]);
            World w = Bukkit.getServer().getWorld(u);
            return new Location(w, x, y, z, yaw, pitch);
    }
	
}
