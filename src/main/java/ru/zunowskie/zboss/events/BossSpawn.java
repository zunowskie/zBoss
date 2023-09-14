package ru.zunowskie.zboss.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.zunowskie.zboss.ZBoss;
import ru.zunowskie.zboss.manager.BossBarManager;

import java.util.*;

public class BossSpawn implements Listener {

    public static LivingEntity bossEntity;
    private static Map<Player, Double> damageMap = new HashMap<>();

    private static List<LivingEntity> bosses = new ArrayList<>();
    private static List<String> bossNames = new ArrayList<>();


    private static int currentBossIndex = 0;

    private static int taskId;


    public static String world;

    public static Player getFirstPlace() {
        List<Map.Entry<Player, Double>> sortedDamageList = new ArrayList<>(damageMap.entrySet());
        sortedDamageList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        if (!sortedDamageList.isEmpty()) {
            return sortedDamageList.get(0).getKey();
        }
        return null;
    }

    public static Player getSecondPlace() {
        List<Map.Entry<Player, Double>> sortedDamageList = new ArrayList<>(damageMap.entrySet());
        sortedDamageList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        if (sortedDamageList.size() >= 2) {
            return sortedDamageList.get(1).getKey();
        }
        return null;
    }

    public static Player getThirdPlace() {
        List<Map.Entry<Player, Double>> sortedDamageList = new ArrayList<>(damageMap.entrySet());
        sortedDamageList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        if (sortedDamageList.size() >= 3) {
            return sortedDamageList.get(2).getKey();
        }
        return null;
    }


    public void onSpawn() {
        String spawnLocationString = ZBoss.getInstance().getConfig().getString("locations.spawnLocation");
        if (spawnLocationString == null) {
            Bukkit.getLogger().warning("§6§lzBoss: §fМестоположение спавна босса не указано в конфигурации!");
            return;
        }

        String[] var = spawnLocationString.split(";");
        if (var.length < 4) {
            Bukkit.getLogger().warning("§6§lzBoss: §fНеверный формат местоположения спавна босса в конфигурации!");
            return;
        }



        String worldName = var[0];
        double x = Double.parseDouble(var[1]);
        double y = Double.parseDouble(var[2]);
        double z = Double.parseDouble(var[3]);

        world = worldName;

        Location location = new Location(Bukkit.getWorld(worldName), x, y, z);

        ConfigurationSection typesSection = ZBoss.getInstance().getConfig().getConfigurationSection("types");
        if (typesSection == null) {
            Bukkit.getLogger().warning("§6§lzBoss: §fТипы мобов не указаны в конфигурации!");
            return;
        }

        if (Bukkit.getWorld(worldName).getEntities().stream()
                .anyMatch(entity -> entity.hasMetadata("zboss"))) {
            Bukkit.getLogger().warning("Босс уже жив!");
            return;
        }

        bosses.clear();
        bossNames.clear();

        for (String mobName : typesSection.getKeys(false)) {
            ConfigurationSection mobSection = typesSection.getConfigurationSection(mobName);
            if (mobSection != null) {
                ConfigurationSection spawnSection = mobSection.getConfigurationSection("spawn");
                if (spawnSection != null) {
                    String entityType = spawnSection.getString("type");
                    double maxHealth = spawnSection.getDouble("health");
                    String name = spawnSection.getString("name");

                    if (entityType != null && name != null) {
                        EntityType type = EntityType.valueOf(entityType);
                        LivingEntity bossEntity = (LivingEntity) location.getWorld().spawnEntity(location, type);

                        name = name.replace("&", "§");
                        bossEntity.setCustomName(name);
                        bossEntity.setMetadata("zboss", new FixedMetadataValue(ZBoss.getInstance(), false));

                        PotionEffect strengthEffect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2);
                        bossEntity.addPotionEffect(strengthEffect);

                        AttributeInstance attribute = bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        if (attribute != null) {
                            attribute.setBaseValue(maxHealth);
                            bossEntity.setHealth(maxHealth);
                        } else {
                            Bukkit.getLogger().warning("§6§lzBoss: §fАтрибут здоровья не найден для моба " + mobName);
                            continue;
                        }

                        bosses.add(bossEntity);
                        bossNames.add(name);
                    } else {
                        Bukkit.getLogger().warning("§6§lzBoss: §fНе указан тип моба или имя для " + mobName);
                    }
                } else {
                    Bukkit.getLogger().warning("§6§lzBoss: §fСекция spawn не найдена для типа " + mobName);
                }
            }
        }

        if (!bosses.isEmpty()) {
            LivingEntity currentBoss = bosses.get(currentBossIndex);
            String currentBossName = bossNames.get(currentBossIndex);
            bossEntity = currentBoss;

            List<String> statsList = ZBoss.getInstance().getConfig().getStringList("message.spawn");
            String statsMessage = listToString(statsList);
            statsMessage = statsMessage.replaceAll("%name%", currentBossName);
            statsMessage = statsMessage.replaceAll("&", "§");

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(statsMessage);
            }

            BossBarManager.displayBossBar(ZBoss.getInstance().getConfig().getString("settings.bossbar.name")
                            .replace("%x%", String.valueOf(location.getBlockX()))
                            .replace("%y%", String.valueOf(location.getBlockY()))
                            .replace("%z%", String.valueOf(location.getBlockZ()))
                            .replace("&", "§")
                            .replace("%name%", currentBossName),
                    BarColor.valueOf(ZBoss.getInstance().getConfig().getString("settings.bossbar.color")),
                    BarStyle.valueOf(ZBoss.getInstance().getConfig().getString("settings.bossbar.style")));

            currentBossIndex++;
            if (currentBossIndex >= bosses.size()) {
                currentBossIndex = 0;
            }

            for (LivingEntity boss : bosses) {
                if (boss != currentBoss) {
                    boss.remove();
                }
            }


            if (taskId != 0) {
                Bukkit.getScheduler().cancelTask(taskId);
            }

            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZBoss.getInstance(), () -> {
                if (currentBoss.isDead()) {
                    BossBarManager.removeBossBar();
                    Player firstPlace = getFirstPlace();
                    Player secondPlace = getSecondPlace();
                    Player thirdPlace = getThirdPlace();

                    List<String> deathList = ZBoss.getInstance().getConfig().getStringList("message.death");
                    String deathMessage = listToString(deathList);
                    deathMessage = deathMessage.replaceAll("&", "§");
                    deathMessage = deathMessage.replaceAll("%name%", currentBossName);

                    if (firstPlace != null) {
                        deathMessage = deathMessage.replaceAll("%player1%", firstPlace.getDisplayName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ZBoss.getInstance().getConfig().getString("commands.player1")
                                .replace("%player%", firstPlace.getDisplayName()));
                    } else {
                        deathMessage = deathMessage.replaceAll("%player1%", "§eНе найден");
                    }

                    if (secondPlace != null) {
                        deathMessage = deathMessage.replaceAll("%player2%", secondPlace.getDisplayName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ZBoss.getInstance().getConfig().getString("commands.player2")
                                .replace("%player%", secondPlace.getDisplayName()));
                    } else {
                        deathMessage = deathMessage.replaceAll("%player2%", "§eНе найден");
                    }

                    if (thirdPlace != null) {
                        deathMessage = deathMessage.replaceAll("%player3%", thirdPlace.getDisplayName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ZBoss.getInstance().getConfig().getString("commands.player3")
                                .replace("%player%", thirdPlace.getDisplayName()));
                    } else {
                        deathMessage = deathMessage.replaceAll("%player3%", "§eНе найден");
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(deathMessage);
                    }

                    damageMap.clear();
                    Bukkit.getScheduler().cancelTask(taskId);
                    taskId = 0;
                }
            }, 0, 10);
        } else {
            Bukkit.getLogger().warning("§6§lzBoss: §fНе удалось создать ни одного босса.");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() == bossEntity) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                double damage = event.getDamage();
                damageMap.put(player, damage);
            }
        }
    }


    private static String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
}
