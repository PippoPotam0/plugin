package it.cerebotani.commands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import javax.sql.rowset.spi.SyncFactoryException;
import static javax.sql.rowset.spi.SyncFactory.getLogger;

public class Duello implements CommandExecutor, Listener {
    private Player p;
    private Player target;
    private Location locPrima1;
    private ItemStack[] itemP;
    private ItemStack[] armorP;
    private GameMode gm1;
    private Location locPrima2;
    private ItemStack[] itemTarget;
    private ItemStack[] armorTarget;
    private GameMode gm2;
    private boolean isDuelActive = false;
    Location duelLocation;
    Plugin plugin = Bukkit.getPluginManager().getPlugin("duello");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo i giocatori possono eseguire questo comando.");
            return true;
        }
        p = (Player) sender;

        if (command.getName().equalsIgnoreCase("duello")) {
            if (isDuelActive) {
                sender.sendMessage("Un duello e' già in corso.");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage("Devi specificare un giocatore con cui duellare.");
                return true;
            }
            String targetPlayerName = args[0];
            target = p.getServer().getPlayer(targetPlayerName);

            if (target == null) {
                sender.sendMessage("Il giocatore specificato non e' online.");
                return true;
            }

            p.sendMessage("Hai sfidato" + target.getName() + "... attendi per sapere se accetta il tuo affronto");
            target.sendMessage(p.getName() + " ti ha sfidato. Accetti l'affronto? *.accetta / .rifiuta*");
        }
        return true;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if(e.getPlayer().equals(target)){
            String message = e.getMessage();
            if(message.equalsIgnoreCase(".accetta")){
                e.setCancelled(true);
                p.sendMessage("Il tuo duello e' stato accetta");
                target.sendMessage("Hai accettato il duello");
                startDuel();

            } else if (message.equalsIgnoreCase(".rifiuta")) {
                e.setCancelled(true);
                p.sendMessage("Il tuo duello e' stato rifiutato");
                target.sendMessage("Hai rifiutato il duello");
            }
        }
    }


    @EventHandler
    public void onLastDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if(isDuelActive) {
                if (player.equals(p) || player.equals(target)) {
                    if (e.getFinalDamage() >= player.getHealth()) {
                        if(player.equals(p)) {
                            p.sendMessage("Hai vinto il duello");
                            target.sendMessage("Hai perso il duello");
                        }
                        if(player.equals(target)) {
                            target.sendMessage("Hai vinto il duello");
                            p.sendMessage("Hai perso il duello");
                        }
                        e.setCancelled(true);
                        onEnd(p);
                        onEnd(target);
                        isDuelActive = false;
                        generatePlatform(duelLocation, 300, Material.AIR, Material.AIR);
                    }
                }
            }
        }
    }

    public void registerEvents() {
        if (plugin != null) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } else {
            System.out.println("Impossibile trovare il plugin.");
        }
    }

    public void startDuel(){
        isDuelActive = true;
        itemP = p.getInventory().getContents();
        armorP = p.getInventory().getArmorContents();
        locPrima1 = p.getLocation();
        gm1 = p.getGameMode();

        itemTarget = target.getInventory().getContents();
        armorTarget = target.getInventory().getArmorContents();
        locPrima2 = target.getLocation();
        gm2 = target.getGameMode();

        setupDuel(p, 1, p.getLocation().getZ() - 1);
        setupDuel(target, -1, p.getLocation().getZ() - 10);
    }


    private void setupDuel(Player player, int direction, double zPosition) {
        player.setHealth(20);
        duelLocation = new Location(player.getWorld(), p.getLocation().getX(), 301, zPosition);
        duelLocation.setDirection(new Vector(0, 0, direction));
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                generatePlatform(duelLocation, 300, Material.QUARTZ_BRICKS, Material.BARRIER);  // Genera la piattaforma prima della teletrasportazione
                player.teleport(duelLocation);
                player.setGameMode(GameMode.ADVENTURE);
            }
        });
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.DIAMOND_BOOTS),
                new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_CHESTPLATE),
                new ItemStack(Material.DIAMOND_HELMET)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.DIAMOND_SWORD),
                new ItemStack(Material.BOW), new ItemStack(Material.GOLDEN_APPLE, 16),
                new ItemStack(Material.ARROW, 64)});
    }

    public void onEnd(Player player){
        player.getInventory().clear();
        if(player.equals(p)) {
            p.getInventory().setArmorContents(armorP);
            p.getInventory().setContents(itemP);
            p.teleport(locPrima1);
            p.setGameMode(gm1);
            p.setHealth(20);
        }
        if(player.equals(target)) {
            target.getInventory().setArmorContents(armorTarget);
            target.getInventory().setContents(itemTarget);
            target.teleport(locPrima2);
            target.setGameMode(gm2);
            target.setHealth(20);
        }
        player.sendMessage("E' stato uno scontro veramente valoroso");
    }

    private void generatePlatform(Location location, int platformHeight, Material pavimento, Material muro) {
        World world = p.getWorld();
        int playerX = location.getBlockX();
        int playerZ = location.getBlockZ();

        // Calcola le dimensioni della piattaforma
        int platformStartX = playerX - 3;  // Inizia 3 blocchi prima del giocatore sull'asse X
        int platformEndX = playerX + 3;    // Finisce 3 blocchi dopo il giocatore sull'asse X
        int platformStartZ = playerZ - 10; // Estendi la piattaforma di 10 blocchi nella direzione negativa sull'asse Z
        int platformEndZ = playerZ;        // La piattaforma termina alla posizione attuale del giocatore sull'asse Z


        for (int x = platformStartX; x <= platformEndX; x++) {
            for (int z = platformStartZ - 1; z <= platformEndZ; z++) {
                Block block = world.getBlockAt(x, platformHeight, z);
                block.setType(pavimento);  // Puoi modificare questo a tuo piacimento
            }
        }

        // Genera i muri invisibili
        for (int x = platformStartX; x <= platformEndX; x++) {
            for (int y = platformHeight; y <= platformHeight + 4; y++) {
                Block block1 = world.getBlockAt(x, y, platformStartZ - 2);
                Block block2 = world.getBlockAt(x, y, platformEndZ + 1);
                block1.setType(muro);  // Blocco invisibile
                block2.setType(muro);  // Blocco invisibile
            }
        }

        for (int z = platformStartZ - 1; z <= platformEndZ + 1; z++) {
            for (int y = platformHeight; y <= platformHeight + 4; y++) {
                Block block1 = world.getBlockAt(platformStartX - 1, y, z);
                Block block2 = world.getBlockAt(platformEndX + 1, y, z);
                block1.setType(muro);  // Blocco invisibile
                block2.setType(muro);  // Blocco invisibile
            }
        }

    }
}
