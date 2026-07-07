package com.lyecocoworld.alexsmobsserverside;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Checks if dependent plugins (MythicMobs, BetterModel, CraftEngine, etc.)
 * are installed and running.
 */
public class DependencyChecker {

    private final AlexMobsServerSide plugin;

    public DependencyChecker(AlexMobsServerSide plugin) {
        this.plugin = plugin;
    }

    public Result checkAll() {
        Result result = new Result();
        result.mythicMobs = checkPlugin("MythicMobs");
        result.betterModel = checkPlugin("BetterModel");
        result.craftEngine = checkPlugin("CraftEngine");
        result.placeholderAPI = checkPlugin("PlaceholderAPI");
        result.worldGuard = checkPlugin("WorldGuard");
        result.protocolLib = checkPlugin("ProtocolLib");
        return result;
    }

    private PluginInfo checkPlugin(String name) {
        Plugin p = Bukkit.getPluginManager().getPlugin(name);
        if (p != null) {
            return new PluginInfo(name, true, p.getDescription().getVersion());
        }
        return new PluginInfo(name, false, null);
    }

    public static class Result {
        public PluginInfo mythicMobs;
        public PluginInfo betterModel;
        public PluginInfo craftEngine;
        public PluginInfo placeholderAPI;
        public PluginInfo worldGuard;
        public PluginInfo protocolLib;

        public boolean hasMinimumStack() {
            return mythicMobs.installed && betterModel.installed;
        }

        public void logResults(Logger log) {
            log.info("Dependencies:");
            log.info("  MythicMobs:     " + mythicMobs);
            log.info("  BetterModel:    " + betterModel);
            log.info("  CraftEngine:    " + craftEngine);
            log.info("  PlaceholderAPI: " + placeholderAPI);
            log.info("  WorldGuard:     " + worldGuard);
            log.info("  ProtocolLib:    " + protocolLib);
            if (hasMinimumStack()) {
                log.info("✓ Minimum stack OK (MythicMobs + BetterModel)");
            } else {
                log.warning("✗ Missing minimum dependencies!");
            }
        }
    }

    public static class PluginInfo {
        public final String name;
        public final boolean installed;
        public final String version;

        public PluginInfo(String name, boolean installed, String version) {
            this.name = name;
            this.installed = installed;
            this.version = version;
        }

        @Override
        public String toString() {
            if (installed) {
                return "✓ v" + version;
            }
            return "✗ NOT FOUND";
        }
    }
}
