package ru.zunowskie.zboss.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.zunowskie.zboss.events.BossSpawn;
import ru.zunowskie.zboss.manager.BossBarManager;
import ru.zunowskie.zboss.ZBoss;

public class MainCMD implements CommandExecutor {
    public static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(c("&6&lzBoss: &fДанную команду можно выполнять только игрокам!"));
            return true;

        } else {
            if (cmd.getName().equalsIgnoreCase("zboss")) {

                if (args.length == 0) {
                    String message = "§6╔\n" +
                            "§6║ &6zBoss\n" +
                            "§6║\n" +
                            "§6║ &fDeveloper: §6bio.hushworld.fun &7(site)\n" +
                            "§6║\n" +
                            "§6║ &fКоманды &6&lzBoss\n" +
                            "§6║\n" +
                            "§6║ &f/zboss &espawn&f - &fЗаспавнить босса\n" +
                            "§6║ &f/zboss &ekill&f - &fУбит босса\n" +
                            "§6║ &f/zboss &esetspawn&f - &fУстановить точку босса\n" +
                            "§6║ &f/zboss &etp&f - &fТелепортироваться к боссу\n" +
                            "§6╚";
                    sender.sendMessage(c(message));
                    return true;

                }
                if (sender.hasPermission("zboss.*")) {

                    switch (args[0].toLowerCase()) {
                        case "spawn":

                            String spawnLocationString = ZBoss.getInstance().getConfig().getString("locations.spawnLocation");
                            if (spawnLocationString == null) {
                                sender.sendMessage("§6§lzBoss: §fМестоположение спавна босса не указано в конфигурации!");
                                return true;
                            }

                            String[] var = spawnLocationString.split(";");
                            if (var.length < 4) {
                                sender.sendMessage("§6§lzBoss: §fНеверный формат местоположения спавна босса в конфигурации!");
                                return true;
                            }

                            if (Bukkit.getWorld(BossSpawn.world).getEntities().stream()
                                    .anyMatch(entity -> entity.hasMetadata("zboss"))) {
                                sender.sendMessage("§6§lzBoss: §fБосс уже жив! Убейте его для появление нового");
                                return true;
                            }

                            BossSpawn bossSpawn = new BossSpawn();
                            bossSpawn.onSpawn();
                            break;
                        case "tp":

                            if (!Bukkit.getWorld(BossSpawn.world).getEntities().stream()
                                    .anyMatch(entity -> entity.hasMetadata("zboss"))) {
                                sender.sendMessage("§6§lzBoss: §fБосса не жив!");
                                break;
                            }

                            ((Player) sender).teleport(BossSpawn.bossEntity);
                            sender.sendMessage("§6§lzBoss: §fВы были телепортированы к §aбоссу");
                            break;
                        case "setspawn":
                            String world = ((Player) sender).getLocation().getWorld().getName();
                            double x = ((Player) sender).getLocation().getBlockX();
                            double y = ((Player) sender).getLocation().getBlockY();
                            double z = ((Player) sender).getLocation().getBlockZ();

                            String locationString = world + ";" + x + ";" + y + ";" + z;
                            ZBoss.getInstance().getConfig().set("locations.spawnLocation", locationString);
                            ZBoss.getInstance().saveConfig();

                            sender.sendMessage("§6§lzBoss: §fВаши координаты: " + locationString + ". Были сохранены в конфигурации плагина §7(vk.com/zunowi)");
                            break;
                        case "kill":
                            if (!Bukkit.getWorld(BossSpawn.world).getEntities().stream()
                                    .anyMatch(entity -> entity.hasMetadata("zboss"))) {

                                sender.sendMessage("§6§lzBoss: §fБосс не жив! ");
                                return true;
                            }
                            sender.sendMessage("§6§lzBoss: §fБосс был §aуспешно §fубит.");
                            BossSpawn.bossEntity.remove();
                            BossBarManager.removeBossBar();
                            break;
                    }
                }
            }
        }
        return true;
    }
}

