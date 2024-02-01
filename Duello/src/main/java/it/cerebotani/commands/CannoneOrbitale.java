package it.cerebotani.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CannoneOrbitale implements CommandExecutor {

    private final Plugin plugin;

    public CannoneOrbitale(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("orbitalcannon")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can execute this command!");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("Usage: /orbitalcannon <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null || !target.isOnline()) {
                sender.sendMessage("Player not found or not online!");
                return true;
            }

            launchOrbitalCannon(target);
            return true;
        }

        return false;
    }

    private void launchOrbitalCannon(Player player) {
        BukkitRunnable particleTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                Location playerLocation = player.getLocation();
                World world = player.getWorld();

                if (ticks % 20 == 0) {
                    // Spawn flame particles at player's head
                    world.spawnParticle(Particle.FLAME, playerLocation.clone().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0);

                    // Play sound of water solidifying with lava at player's location
                    world.playSound(playerLocation, Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
                }

                ticks++;

                if (ticks >= 100) {
                    // Stop the task after 5 seconds
                    this.cancel();

                    // Trigger explosion at player's current location
                    triggerExplosion(playerLocation);
                }
            }
        };

        // Schedule the particle task to run every tick
        particleTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void triggerExplosion(Location location) {
        World world = location.getWorld();

        // Sound effect
        world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);

        // Spawn explosion particles at player's current location
        world.spawnParticle(Particle.EXPLOSION_LARGE, location, 1, 0, 0, 0, 0);

        // Create explosion at player's current location with a radius 10 times larger
        world.createExplosion(location, 40.0f, true);

        // Damage players in the explosion (including players in creative mode)
        for (Player nearbyPlayer : location.getWorld().getPlayers()) {
            if (nearbyPlayer.getLocation().distance(location) < 40.0) {
                nearbyPlayer.damage(5.0);
            }
        }
    }
}
