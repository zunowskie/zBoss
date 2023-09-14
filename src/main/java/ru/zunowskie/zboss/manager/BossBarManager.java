package ru.zunowskie.zboss.manager;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BossBarManager {
    private static BossBar bossBar;
    private static Map<Player, Integer> playerTasks = new HashMap<>();

    public static void displayBossBar(String message, BarColor color, BarStyle style) {
        bossBar = Bukkit.createBossBar(message, color, style);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    public static void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        for (Map.Entry<Player, Integer> entry : playerTasks.entrySet()) {
            Player player = entry.getKey();
            int taskId = entry.getValue();
            Bukkit.getScheduler().cancelTask(taskId);
        }

        playerTasks.clear();
    }

}
