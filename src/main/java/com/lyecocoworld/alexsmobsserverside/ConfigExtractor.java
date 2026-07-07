package com.lyecocoworld.alexsmobsserverside;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Extracts all bundled MythicMobs, CraftEngine, and datapack
 * configurations from the plugin JAR to the respective plugin
 * directories.
 *
 * All file operations are pure I/O — no scheduler, no main thread,
 * fully Folia-safe.
 */
public class ConfigExtractor {

    private final AlexMobsServerSide plugin;
    private final Logger log;
    private final Path dataFolder;

    // Extraction statistics
    private int filesExtracted = 0;
    private int dirsCreated = 0;

    // Target directories
    private Path mythicMobsDir;
    private Path craftEngineDir;
    private Path datapackDir;
    private Path resourcePackDir;

    public ConfigExtractor(AlexMobsServerSide plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder().toPath();
    }

    /**
     * Main extraction method — copies all configs from the JAR
     * to the respective plugin folders.
     */
    public void extractAll() {
        filesExtracted = 0;
        dirsCreated = 0;
        long startTime = System.currentTimeMillis();

        log.info("Starting config extraction...");

        // Resolve target directories
        Path pluginsDir = dataFolder.getParent();
        mythicMobsDir = pluginsDir.resolve("MythicMobs");
        craftEngineDir = pluginsDir.resolve("CraftEngine");
        // Default Paper world folder is "world" relative to server root.
        Path serverRoot = pluginsDir.getParent();
        if (serverRoot == null) serverRoot = dataFolder; // fallback
        datapackDir = serverRoot.resolve("world").resolve("datapacks");
        // Create it if it doesn't exist
        try {
            Files.createDirectories(datapackDir);
        } catch (Exception ignored) {}

        // Create resource pack output dir
        resourcePackDir = dataFolder.resolve("resourcepack");

        // 1. Extract MythicMobs configs (mob, skills, ai, drops, spawn, sounds, variables)
        extractMythicMobsConfigs();

        // 2. Extract CraftEngine configs (items, blocks)
        extractCraftEngineConfigs();

        // 3. Extract datapack (worldgen: Larion + Nature's Spirit)
        extractDatapack();

        // 4. Extract resource pack (textures, sounds, models)
        extractResourcePack();

        // 5. Extract BetterModel configs
        extractBetterModelConfigs();

        long elapsed = System.currentTimeMillis() - startTime;
        log.info(String.format("Extraction complete: %d files, %d dirs (%dms)",
                filesExtracted, dirsCreated, elapsed));
    }

    // ═══════════════════════════════════════════════════════════
    // MythicMobs extraction
    // ═══════════════════════════════════════════════════════════

    private void extractMythicMobsConfigs() {
        if (!Files.isDirectory(mythicMobsDir)) {
            log.warning("MythicMobs directory not found: " + mythicMobsDir + " — skipping MM configs");
            return;
        }

        // Target: MythicMobs/Mobs/, MythicMobs/Skills/, etc.
        // MythicMobs 5.x uses lowercase directory names
        Path mmMobsDir = mythicMobsDir.resolve("mobs");
        Path mmSkillsDir = mythicMobsDir.resolve("skills");
        Path mmSpawnersDir = mythicMobsDir.resolve("spawners");
        Path mmDropsDir = mythicMobsDir.resolve("droptables");
        Path mmSoundsDir = mythicMobsDir.resolve("sounds");
        Path mmItemsDir = mythicMobsDir.resolve("items");
        Path mmAiDir = mythicMobsDir.resolve("skills"); // ai.yml goes in skills dir for MM5
        Path mmVarsDir = mythicMobsDir.resolve("skills"); // variables.yml too

        // Source in JAR: alexsmobs/<creature>/*.yml
        extractDirectoryFromJar("alexsmobs", mmMobsDir, ".yml", "mob.yml");
        extractDirectoryFromJar("alexsmobs", mmSkillsDir, ".yml", "skills.yml");
        extractDirectoryFromJar("alexsmobs", mmSkillsDir, ".yml", "ai.yml");
        extractDirectoryFromJar("alexsmobs", mmSkillsDir, ".yml", "variables.yml");
        extractDirectoryFromJar("alexsmobs", mmSpawnersDir, ".yml", "spawn.yml");
        extractDirectoryFromJar("alexsmobs", mmDropsDir, ".yml", "drops.yml");
        extractDirectoryFromJar("alexsmobs", mmSoundsDir, ".yml", "sounds.yml");

        // Core configs
        extractFileFromJar("alexsmobs/_core/globals.yml", mmSkillsDir.resolve("alexsmobs_globals.yml"));
        extractFileFromJar("alexsmobs/_core/biome_tags.yml", mmMobsDir.resolve("alexsmobs_biome_tags.yml"));
        extractFileFromJar("alexsmobs/_core/mm_items.yml", mmItemsDir.resolve("alexsmobs_items.yml"));

        log.info("MythicMobs configs extracted: " + filesExtracted + " files so far");
    }

    // ═══════════════════════════════════════════════════════════
    // CraftEngine extraction
    // ═══════════════════════════════════════════════════════════

    private void extractCraftEngineConfigs() {
        if (!Files.isDirectory(craftEngineDir)) {
            log.warning("CraftEngine directory not found: " + craftEngineDir + " — skipping CE configs");
            return;
        }

        // Items
        Path ceItemsDir = craftEngineDir.resolve("items");
        extractDirectoryFromJar("craftengine/items", ceItemsDir, ".yml", null);

        // Blocks
        Path ceBlocksDir = craftEngineDir.resolve("blocks");
        extractDirectoryFromJar("craftengine/blocks", ceBlocksDir, ".yml", null);

        log.info("CraftEngine configs extracted: " + filesExtracted + " files so far");
    }

    // ═══════════════════════════════════════════════════════════
    // Datapack extraction
    // ═══════════════════════════════════════════════════════════

    private void extractDatapack() {
        if (!Files.isDirectory(datapackDir)) {
            log.warning("Datapacks directory not found: " + datapackDir + " — skipping datapack");
            return;
        }

        Path amssDatapack = datapackDir.resolve("alexsmobs_serverside");
        extractDirectoryFromJar("worldgen_datapack", amssDatapack, ".json", null);
        extractDirectoryFromJar("worldgen_datapack", amssDatapack, ".mcmeta", null);

        log.info("Datapack extracted to: " + amssDatapack);
    }

    // ═══════════════════════════════════════════════════════════
    // Resource pack extraction
    // ═══════════════════════════════════════════════════════════

    private void extractResourcePack() {
        Path rpDir = resourcePackDir.resolve("alexsmobs_serverside");
        extractDirectoryFromJar("assets", rpDir.resolve("assets"), ".png", null);
        extractDirectoryFromJar("assets", rpDir.resolve("assets"), ".ogg", null);
        extractDirectoryFromJar("assets", rpDir.resolve("assets"), ".json", null);
        extractFileFromJar("assets/pack.mcmeta", rpDir.resolve("pack.mcmeta"));

        log.info("Resource pack extracted to: " + rpDir);
    }

    // ═══════════════════════════════════════════════════════════
    // BetterModel extraction
    // ═══════════════════════════════════════════════════════════

    private void extractBetterModelConfigs() {
        // BetterModel reads models from a specific directory
        Path bmDir = dataFolder.getParent().resolve("BetterModel");
        if (!Files.isDirectory(bmDir)) {
            // BetterModel not installed — skip silently
            return;
        }

        // Extract model configs from alexsmobs/*/model/model.yml
        extractDirectoryFromJar("alexsmobs", bmDir.resolve("alexsmobs_models"), "model.yml", null);
    }

    // ═══════════════════════════════════════════════════════════
    // Helper: extract a full directory tree from the JAR
    // ═══════════════════════════════════════════════════════════

    private void extractDirectoryFromJar(String sourceDir, Path targetDir,
                                          String extension, String fileNameFilter) {
        try {
            // List all resources under sourceDir in the JAR
            var url = plugin.getClass().getClassLoader().getResource(sourceDir);
            if (url == null) {
                log.fine("Source directory not found in JAR: " + sourceDir);
                return;
            }

            // Walk the JAR entries
            var srcPath = Paths.get(plugin.getClass().getProtectionDomain()
                    .getCodeSource().getLocation().toURI());

            if (Files.isDirectory(srcPath)) {
                // Dev mode: reading from filesystem
                Path fullSource = srcPath.resolve(sourceDir);
                if (Files.isDirectory(fullSource)) {
                    try (Stream<Path> walk = Files.walk(fullSource)) {
                        walk.filter(Files::isRegularFile)
                            .filter(p -> matchesFilter(p, extension, fileNameFilter))
                            .forEach(p -> {
                                Path rel = fullSource.relativize(p);
                                Path target = targetDir.resolve(rel.toString());
                                copyFile(p, target);
                            });
                    }
                }
            } else {
                // Production: reading from JAR
                try (var fs = FileSystems.newFileSystem(srcPath, (ClassLoader) null)) {
                    Path jarSource = fs.getPath(sourceDir);
                    if (Files.exists(jarSource)) {
                        try (Stream<Path> walk = Files.walk(jarSource)) {
                            walk.filter(Files::isRegularFile)
                                .filter(p -> matchesFilter(p, extension, fileNameFilter))
                                .forEach(p -> {
                                    String rel = jarSource.relativize(p).toString().replace('\\', '/');
                                    Path target = targetDir.resolve(rel);
                                    copyFile(p, target);
                                });
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warning("Failed to extract " + sourceDir + ": " + e.getMessage());
        }
    }

    private boolean matchesFilter(Path path, String extension, String fileNameFilter) {
        String name = path.getFileName().toString();
        if (extension != null && !name.endsWith(extension)) {
            return false;
        }
        if (fileNameFilter != null && !name.equals(fileNameFilter)) {
            return false;
        }
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // Helper: extract a single file from the JAR
    // ═══════════════════════════════════════════════════════════

    private void extractFileFromJar(String sourcePath, Path targetPath) {
        try (InputStream is = plugin.getResource(sourcePath)) {
            if (is == null) {
                log.fine("Resource not found: " + sourcePath);
                return;
            }
            Files.createDirectories(targetPath.getParent());
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            filesExtracted++;
        } catch (IOException e) {
            log.warning("Failed to extract " + sourcePath + ": " + e.getMessage());
        }
    }

    private void copyFile(Path source, Path target) {
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            filesExtracted++;
        } catch (IOException e) {
            log.warning("Failed to copy " + source + " → " + target + ": " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Reload dependent plugins (via Bukkit commands — Folia-safe dispatch)
    // ═══════════════════════════════════════════════════════════

    public void reloadDependentPlugins(CommandSender sender) {
        // MythicMobs reload
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm reload");
            sender.sendMessage(ChatColor.GREEN + "[AMSS] MythicMobs reloaded.");
        }

        // CraftEngine reload
        if (Bukkit.getPluginManager().getPlugin("CraftEngine") != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ce reload");
            sender.sendMessage(ChatColor.GREEN + "[AMSS] CraftEngine reloaded.");
        }

        // BetterModel reload
        if (Bukkit.getPluginManager().getPlugin("BetterModel") != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bm reload");
            sender.sendMessage(ChatColor.GREEN + "[AMSS] BetterModel reloaded.");
        }

        sender.sendMessage(ChatColor.GREEN + "[AMSS] All plugins reloaded.");
    }

    // ═══════════════════════════════════════════════════════════
    // Status display
    // ═══════════════════════════════════════════════════════════

    public void printStatus(CommandSender sender) {
        // Count creatures
        int creatureCount = countCreatures();
        sender.sendMessage(ChatColor.YELLOW + "Creatures: " + ChatColor.WHITE + creatureCount);

        // Check MythicMobs
        PluginStatus mm = checkDirStatus("MythicMobs", mythicMobsDir);
        sender.sendMessage(ChatColor.YELLOW + "MythicMobs: " + mm.colored());

        // Check CraftEngine
        PluginStatus ce = checkDirStatus("CraftEngine", craftEngineDir);
        sender.sendMessage(ChatColor.YELLOW + "CraftEngine: " + ce.colored());

        // Check datapack
        PluginStatus dp = checkDirStatus("Datapack", datapackDir.resolve("alexsmobs_serverside"));
        sender.sendMessage(ChatColor.YELLOW + "Datapack: " + dp.colored());

        // Check resource pack
        long texCount = countFiles(resourcePackDir.resolve("alexsmobs_serverside/assets"), ".png");
        long soundCount = countFiles(resourcePackDir.resolve("alexsmobs_serverside/assets"), ".ogg");
        sender.sendMessage(ChatColor.YELLOW + "Resource Pack: "
                + ChatColor.WHITE + texCount + " textures, " + soundCount + " sounds");
    }

    private int countCreatures() {
        Path alexsmobsDir = dataFolder.getParent().resolve("MythicMobs/Mobs");
        if (!Files.isDirectory(alexsmobsDir)) {
            // Try from JAR
            return 89; // Known count
        }
        try (Stream<Path> dirs = Files.list(alexsmobsDir)) {
            return (int) dirs.filter(Files::isDirectory).count();
        } catch (IOException e) {
            return -1;
        }
    }

    private PluginStatus checkDirStatus(String name, Path dir) {
        if (dir == null || !Files.isDirectory(dir)) {
            return new PluginStatus(name, false, 0);
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            long count = walk.filter(Files::isRegularFile).count();
            return new PluginStatus(name, true, (int) count);
        } catch (IOException e) {
            return new PluginStatus(name, false, 0);
        }
    }

    private long countFiles(Path dir, String ext) {
        if (!Files.isDirectory(dir)) return 0;
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(ext))
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }

    private record PluginStatus(String name, boolean exists, int fileCount) {
        public String colored() {
            if (exists) {
                return ChatColor.GREEN + "✓ " + ChatColor.WHITE + fileCount + " files";
            }
            return ChatColor.RED + "✗ Not installed";
        }
    }
}
