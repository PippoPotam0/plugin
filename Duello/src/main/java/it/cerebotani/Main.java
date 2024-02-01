package it.cerebotani;

import it.cerebotani.commands.CustomMap;
import it.cerebotani.commands.Duello;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private Duello duello; // Istanza della classe Duello

    @Override
    public void onEnable() {
        duello = new Duello();
        duello.registerEvents();
        Logger logger = this.getLogger();
        logger.info("QUALCUNO HA RICHIESTO UN DUELLO LANCIANDO UN GUANTO");
        getCommand("duello").setExecutor(duello);
        getCommand("custommap").setExecutor(new CustomMap());
    }

    @Override
    public void onDisable() {
        // Non è necessario fare nulla qui, il plugin si disattiva quando viene chiamato onLastDamage
    }

    public Duello getDuello() {
        return duello;
    }

}