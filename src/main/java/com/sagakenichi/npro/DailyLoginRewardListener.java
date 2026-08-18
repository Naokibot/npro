package com.sagakenichi.npro;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.logging.Level;

public final class DailyLoginRewardListener implements Listener {

    private static final String DEFAULT_TIME_ZONE = "Asia/Tokyo";
    private static final int DEFAULT_REWARD_POINTS = 5;
    private static final String DEFAULT_BROADCAST = "%player%がログインしログイン報酬%points%ポイント受け取りました！！";
    private static final String DEFAULT_ALREADY_CLAIMED = "あなたはすでに報酬を受け取っています！";

    private final NproPlugin plugin;

    private DailyLoginRewardListener(NproPlugin plugin) {
        this.plugin = plugin;
    }

    public static void register(NproPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new DailyLoginRewardListener(plugin), plugin);
        plugin.getLogger().info("Daily login reward enabled: once per day, default +5 Npoint.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            reward(player);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Could not process daily login reward for " + player.getName() + ".",
                    exception
            );
            player.sendMessage("ログイン報酬の処理に失敗しました。管理者に連絡してください。");
        }
    }

    private void reward(Player player) {
        if (!plugin.getConfig().getBoolean("daily-login-reward.enabled", true)) {
            return;
        }

        YamlConfiguration data = plugin.dailyRewardData();
        File dataFile = plugin.dailyRewardDataFile();
        if (data == null || dataFile == null) {
            throw new IllegalStateException("Npro data.yml is not initialized");
        }

        UUID uuid = player.getUniqueId();
        String playerPath = "players." + uuid;
        String pointsPath = playerPath + ".points";
        String datePath = playerPath + ".daily-reward-date";
        String today = LocalDate.now(rewardZone()).toString();
        String lastRewardDate = data.getString(datePath);

        if (today.equals(lastRewardDate)) {
            player.sendMessage(message(
                    "daily-login-reward.already-claimed-message",
                    DEFAULT_ALREADY_CLAIMED,
                    player.getName(),
                    rewardPoints()
            ));
            return;
        }

        int rewardPoints = rewardPoints();
        int currentPoints = Math.max(0, data.getInt(pointsPath, 0));
        int updatedPoints = addWithoutOverflow(currentPoints, rewardPoints);
        Object previousPoints = data.get(pointsPath);
        Object previousDate = data.get(datePath);

        data.set(pointsPath, updatedPoints);
        data.set(datePath, today);

        if (!save(data, dataFile)) {
            data.set(pointsPath, previousPoints);
            data.set(datePath, previousDate);
            player.sendMessage("ログイン報酬の保存に失敗したため、今回は付与されませんでした。管理者に連絡してください。");
            return;
        }

        Bukkit.broadcastMessage(message(
                "daily-login-reward.broadcast-message",
                DEFAULT_BROADCAST,
                player.getName(),
                rewardPoints
        ));
    }

    private ZoneId rewardZone() {
        String configured = plugin.getConfig().getString("daily-login-reward.time-zone", DEFAULT_TIME_ZONE);
        if (configured == null || configured.isBlank()) {
            return ZoneId.of(DEFAULT_TIME_ZONE);
        }

        try {
            return ZoneId.of(configured.trim());
        } catch (DateTimeException exception) {
            plugin.getLogger().warning(
                    "Invalid daily-login-reward.time-zone '" + configured + "'; using " + DEFAULT_TIME_ZONE + "."
            );
            return ZoneId.of(DEFAULT_TIME_ZONE);
        }
    }

    private int rewardPoints() {
        return Math.max(0, plugin.getConfig().getInt("daily-login-reward.points", DEFAULT_REWARD_POINTS));
    }

    private String message(String path, String fallback, String playerName, int points) {
        String message = plugin.getConfig().getString(path, fallback);
        if (message == null) {
            message = fallback;
        }
        return message
                .replace("%player%", playerName)
                .replace("%points%", Integer.toString(points));
    }

    private int addWithoutOverflow(int current, int reward) {
        long result = (long) current + reward;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    private boolean save(YamlConfiguration data, File target) {
        Path targetPath = target.toPath();
        Path directory = targetPath.getParent();
        if (directory == null) {
            plugin.getLogger().severe("data.yml has no parent directory: " + targetPath);
            return false;
        }

        Path temporaryFile = null;
        try {
            Files.createDirectories(directory);
            temporaryFile = Files.createTempFile(directory, "data.yml.", ".tmp");
            data.save(temporaryFile.toFile());
            moveIntoPlace(temporaryFile, targetPath);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not persist daily login reward to data.yml.", exception);
            return false;
        } finally {
            deleteQuietly(temporaryFile);
        }
    }

    private void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
