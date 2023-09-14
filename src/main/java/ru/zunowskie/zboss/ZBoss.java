package ru.zunowskie.zboss;


import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.zunowskie.zboss.commands.MainCMD;
import ru.zunowskie.zboss.events.BossSpawn;
import ru.zunowskie.zboss.manager.BossBarManager;

public final class ZBoss extends JavaPlugin {


    public static ZBoss instance;


    public static ZBoss getInstance() {
        return instance;
    }


    private int taskId;

    @Override
    public void onEnable() {

        instance = this;
        this.saveDefaultConfig();


        Bukkit.getPluginManager().registerEvents(new BossSpawn(), this);
        getCommand("zboss").setExecutor(new MainCMD());


        logo();

        onTimer();

    }

    public void logo() {
        Bukkit.getLogger().info("");
        Bukkit.getLogger().info("§e| §c");
        Bukkit.getLogger().info("§e| §c███████╗██████╗░░█████╗░░██████╗░██████╗");
        Bukkit.getLogger().info("§e| §c╚════██║██╔══██╗██╔══██╗██╔════╝██╔════╝");
        Bukkit.getLogger().info("§e| §c░░███╔═╝██████╦╝██║░░██║╚█████╗░╚█████╗░");
        Bukkit.getLogger().info("§e| §c██╔══╝░░██╔══██╗██║░░██║░╚═══██╗░╚═══██╗");
        Bukkit.getLogger().info("§e| §c███████╗██████╦╝╚█████╔╝██████╔╝██████╔╝");
        Bukkit.getLogger().info("");
        Bukkit.getLogger().info("§e| §fПлагин §6zBoss §7 §fВерсия плагина - §a1.0");
        Bukkit.getLogger().info("§e| §fРазработчик - §6bio.hushworld.fun §7(site)");
        Bukkit.getLogger().info("");
    }


    @Override
    public void onDisable() {


        if (BossSpawn.bossEntity != null) {
            BossSpawn.bossEntity.remove();
            BossBarManager.removeBossBar();

        }
        this.saveConfig();

    }

    public void onTimer() {
        int minutesInterval = this.getConfig().getInt("settings.interval");
        int ticksInterval = minutesInterval * 1200;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            BossSpawn bossSpawn = new BossSpawn();
            bossSpawn.onSpawn();
        }, 0, ticksInterval);
    }
}