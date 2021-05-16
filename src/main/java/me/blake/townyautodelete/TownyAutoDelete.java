package me.blake.townyautodelete;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TownyAutoDelete extends JavaPlugin {
    private final FileConfiguration config = this.getConfig();

    public void onEnable() {
        setupConfig();

        BukkitScheduler scheduler = getServer().getScheduler();
        long ticks = (long) config.getInt("interval") * 20 * 60;
        scheduler.scheduleSyncRepeatingTask(this, this::deleteTowns, 0L, ticks);
    }

    public void setupConfig() {
        this.saveDefaultConfig();
        config.addDefault("inactiveLength", 336);
        config.addDefault("interval", 60);
        config.set("version", "1.1-SNAPSHOT");
    }

    public void deleteTowns() {
        Collection<Town> towns = TownyUniverse.getInstance().getTowns();
        for (Town t : towns) {
            if (!config.getStringList("exemptions").contains(t.getName())) {

                List<Resident> residents = t.getResidents();
                int numInactive = 0;

                for (Resident r : residents) {
                    if (r.getPlayer() == null) {
                        Date lastJoinDate = new Date(this.getServer().getOfflinePlayer(r.getUUID()).getLastPlayed());
                        Date now = new Date();
                        long difference = now.getTime() - lastJoinDate.getTime();
                        long differenceInHours = (difference / (1000 * 60 * 60)) % 24;
                        if (differenceInHours >= config.getInt("inactiveLength")) {
                            numInactive++;
                        }
                    }
                }

                if (numInactive == residents.size()) {
                    getServer().broadcastMessage(ChatColor.RED + t.getFormattedName() + " has fallen!");
                    TownyUniverse.getInstance().getDataSource().removeTown(t);
                }
            }
        }
    }
}
