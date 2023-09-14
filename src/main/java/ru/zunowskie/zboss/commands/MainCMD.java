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
                    sender.sendMessage(c("&6╔"));
                    sender.sendMessage(c("§6║ &6zBoss"));
                    sender.sendMessage(c("§6║"));
                    sender.sendMessage(c("§6║ &fDeveloper: §6bio.hushworld.fun &7(site)"));
                    sender.sendMessage(c("§6║"));
                    sender.sendMessage(c("§6║ &fКоманды &6&lzBoss"));
                    sender.sendMessage(c("§6║"));
                    sender.sendMessage(c("§6║ &f/zboss &espawn&f - &fЗаспавнить босса"));
                    sender.sendMessage(c("§6║ &f/zboss &ekill&f - &fУбит босса"));
                    sender.sendMessage(c("§6║ &f/zboss &esetspawn&f - &fУстановить точку босса"));
                    sender.sendMessage(c("§6║ &f/zboss &etp&f - &fТелепортироваться к боссу"));
                    sender.sendMessage(c("§6╚"));
                    return true;

                }
                if (sender.hasPermission("zboss.*")) {

                    if (args[0].equalsIgnoreCase("tp")) {
                        ((Player) sender).teleport(BossSpawn.bossEntity);
                        sender.sendMessage("§6§lzBoss: §fВы были телепортированы к §aбоссу");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("spawn")) {
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
                    }


                    if (args[0].equalsIgnoreCase("setspawn")) {
                        String world = ((Player) sender).getLocation().getWorld().getName();
                        int x = ((Player) sender).getLocation().getBlockX();
                        int y = ((Player) sender).getLocation().getBlockY();
                        int z = ((Player) sender).getLocation().getBlockZ();

                        String locationString = world + ";" + x + ";" + y + ";" + z;
                        ZBoss.getInstance().getConfig().set("locations.spawnLocation", locationString);
                        ZBoss.getInstance().saveConfig();

                        sender.sendMessage("§6§lzBoss: §fВаши координаты: " + locationString + ". Были сохранены в конфигурации плагина §7(vk.com/zunowi)");
                        return true;

                    }


                    if (args[0].equalsIgnoreCase("kill")) {
                        if (BossSpawn.bossEntity == null) {
                            sender.sendMessage("§6§lzBoss: §fБосса нету тупи европа кидс ");
                            return true;
                        }
                        sender.sendMessage("§6§lzBoss: §fБосс был §aуспешно §fубит.");
                        BossSpawn.bossEntity.remove();
                        BossBarManager.removeBossBar();
                        return true;


                    }
                }
            }
            return true;
        }
    }
}


