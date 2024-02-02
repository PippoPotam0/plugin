package it.cerebotani.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CustomMap implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo i giocatori possono eseguire questo comando.");
            return true;
        }
        Player player = (Player) sender;

        if (label.equalsIgnoreCase("custommap")) {
            ItemStack mapItem = createCustomMap();
            player.getInventory().addItem(mapItem);
            player.sendMessage("Hai ricevuto una mappa personalizzata!");
            return true;
        }
        return false;
    }

    public static ItemStack createCustomMap() {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();

        // Verifica se l'oggetto MapMeta ha una MapView associata
        if (!mapMeta.hasMapView()) {
            // Se non c'è, crea una nuova MapView
            MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
            mapMeta.setMapView(mapView);
        }
        MapView mapView = mapMeta.getMapView();

        // Rimuovi i renderer esistenti, se presenti
        for (MapRenderer renderer : mapView.getRenderers()) {
            mapView.removeRenderer(renderer);
        }

        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                try {
                    //BufferedImage image = ImageIO.read(getClass().getResource("/image.png"));
                    mapCanvas.drawImage(0, 0, ImageIO.read(getClass().getResource("/image.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mapItem.setItemMeta(mapMeta);
        return mapItem;
    }
}
