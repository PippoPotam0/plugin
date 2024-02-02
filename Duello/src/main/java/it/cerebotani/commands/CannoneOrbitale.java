package it.cerebotani.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CannoneOrbitale implements CommandExecutor {

    private final Plugin plugin = Bukkit.getPluginManager().getPlugin("duello");
    Player target;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("orbitalcannon")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can execute this command!");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("Usage: /orbitalcannon <player>");
                return true;
            }

            target = Bukkit.getPlayer(args[0]);

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
                    world.spawnParticle(Particle.FLAME, playerLocation.clone().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0);
                    world.playSound(playerLocation, Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
                }
                ticks++;
                if (ticks >= 100) {
                    this.cancel();
                    triggerExplosion(playerLocation);
                }
            }
        };
        particleTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void triggerExplosion(Location location) {
        World world = location.getWorld();

        world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
        world.spawnParticle(Particle.EXPLOSION_LARGE, location, 3, 0, 0, 0, 0);
        target.setHealth(0);
        world.createExplosion(location, 40.0f, true);
    }
}
