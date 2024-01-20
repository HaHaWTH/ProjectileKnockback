package io.wdsj.projkb;

import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Objects;

public final class ProjectileKnockback extends JavaPlugin implements Listener {
    public YamlConfiguration config;

    @Override
    public void onEnable() {
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        int pluginId = 20777;
        Metrics metrics = new Metrics(this, pluginId);
        getLogger().info("ProjectileKnockback is enabled!");
        Objects.requireNonNull(getCommand("projectileknockback")).setExecutor(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll();
        Objects.requireNonNull(getCommand("projectileknockback")).setExecutor(null);
        getLogger().info("ProjectileKnockback is disabled!");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§f[§bProjectile§eKB§f] §cUsage: /projectileknockback reload");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            try {
                loadConfig();
                sender.sendMessage("§f[§bProjectile§eKB§f] §aConfig has been reloaded!");
            } catch (NullPointerException e) {
                sender.sendMessage("§f[§bProjectile§eKB§f] §cError! Check console for more details.");
            }
        } else {
            sender.sendMessage("§f[§bProjectile§eKB§f] §cUsage: /projectileknockback reload");
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        Entity entity = event.getHitEntity();
        ProjectileSource projectileSource = projectile.getShooter();
        if (entity instanceof Player && shouldKb(projectile)){
            Player playerEntity = (Player) entity;
            if (projectileSource instanceof Player) {
                Player playerSource = (Player) projectileSource;
                if (playerSource.hasPermission("projectilekb.use")) {
                    applyKnockback(playerEntity);
                }
            } else {
                if (config.getBoolean("kbOnNonPlayer")) {
                    applyKnockback(playerEntity);
                }
            }
        }
    }

    private void applyKnockback(Player player) {
        Vector playerVelocity = player.getVelocity();
        Vector knockbackVelocity = new Vector(playerVelocity.getX() * config.getDouble("horizontalVelocity"), playerVelocity.getY() * config.getDouble("verticalVelocity"), playerVelocity.getZ() * config.getDouble("horizontalVelocity"));
        player.damage(0.0000001D);
        player.setVelocity(knockbackVelocity);
    }

    private void loadConfig(){
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private boolean shouldKb(Projectile projectile){
        if (config.getBoolean("kbOnFishingRod") && projectile instanceof FishHook) {
            return true;
        }
        if (config.getBoolean("kbOnEnderPearl") && projectile instanceof EnderPearl) {
            return true;
        }
        if (config.getBoolean("kbOnEgg") && projectile instanceof Egg) {
            return true;
        }
        return config.getBoolean("kbOnSnowball") && projectile instanceof Snowball;
    }
}
