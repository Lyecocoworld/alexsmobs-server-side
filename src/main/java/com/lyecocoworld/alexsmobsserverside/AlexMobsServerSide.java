package com.lyecocoworld.alexsmobsserverside;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;

/**
 * AlexMobsServerSide — Plugin wrapper for the complete Alex's Mobs
 * server-side reimplementation.
 *
 * On enable, extracts all bundled MythicMobs, CraftEngine, and
 * datapack configs to the respective plugin directories.
 * Works on Folia (region-threaded) without any scheduler.
 */
public class AlexMobsServerSide extends JavaPlugin {

    private static final String PREFIX = ChatColor.DARK_GREEN + "[AlexMobsSS] " + ChatColor.RESET;
    private Logger log;
    private ConfigExtractor extractor;

    @Override
    public void onEnable() {
        log = getLogger();
        extractor = new ConfigExtractor(this);

        log.info("╔══════════════════════════════════════════╗");
        log.info("║   Alex's Mobs Server-Side v" + getDescription().getVersion() + "        ║");
        log.info("║   Folia-native reimplementation          ║");
        log.info("╚══════════════════════════════════════════╝");

        // Check dependencies
        DependencyChecker checker = new DependencyChecker(this);
        DependencyChecker.Result deps = checker.checkAll();
        deps.logResults(log);

        if (!deps.hasMinimumStack()) {
            log.warning("Minimum dependencies not met!");
            log.warning("Required: MythicMobs + BetterModel");
            log.warning("Optional: CraftEngine, PlaceholderAPI, WorldGuard");
            log.warning("Config files are still extracted — install missing plugins to enable full functionality.");
        }

        // Extract all configs (Folia-safe: pure file I/O, no scheduler needed)
        extractor.extractAll();

        log.info(PREFIX + "All configs extracted. Run /amss reload to apply.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("alexmobsserverside.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "Reloading configs...");
                extractor.extractAll();
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Configs re-extracted.");
                // Reload dependent plugins
                extractor.reloadDependentPlugins(sender);
            }
            case "install" -> {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "Full install running...");
                extractor.extractAll();
                extractor.reloadDependentPlugins(sender);
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Full install complete!");
            }
            case "status" -> {
                sender.sendMessage(PREFIX + ChatColor.GOLD + "═══ Alex's Mobs Server-Side Status ═══");
                extractor.printStatus(sender);
            }
            case "version" -> {
                sender.sendMessage(PREFIX + ChatColor.AQUA + "Version: " + getDescription().getVersion());
                sender.sendMessage(PREFIX + ChatColor.AQUA + "Author: Lyecocoworld");
                sender.sendMessage(PREFIX + ChatColor.AQUA + "Repo: github.com/Lyecocoworld/alexsmobs-server-side");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(PREFIX + ChatColor.GOLD + "Commands:");
        sender.sendMessage(ChatColor.YELLOW + "  /amss install " + ChatColor.GRAY + "— Extract all configs + reload plugins");
        sender.sendMessage(ChatColor.YELLOW + "  /amss reload " + ChatColor.GRAY + "— Re-extract configs + reload MythicMobs");
        sender.sendMessage(ChatColor.YELLOW + "  /amss status " + ChatColor.GRAY + "— Show installation status");
        sender.sendMessage(ChatColor.YELLOW + "  /amss version " + ChatColor.GRAY + "— Show version info");
    }

    @Override
    public void onDisable() {
        log.info("AlexMobsServerSide disabled.");
    }
}
