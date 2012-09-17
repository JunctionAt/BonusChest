package at.junction.bonuschest;

import java.io.File;
import java.util.logging.Level;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.signs.EssentialsSign;
import static com.earth2me.essentials.signs.EssentialsSign.*;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import de.bananaco.bpermissions.api.util.Permission;

public class BonusChest extends JavaPlugin implements Listener {

    public final Configuration config = new Configuration(this);
    
    @Override
    public void onEnable() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        config.load();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().log(Level.INFO, getDescription().getName() + " " + getDescription().getVersion() + " enabled.");
    }
    
    @Override
    public void onDisable() {
        // tear down
        getLogger().log(Level.INFO, getDescription().getName() + " " + getDescription().getVersion() + " disabled.");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final IEssentials ess = (IEssentials)getServer().getPluginManager().getPlugin("Essentials");
        if (ess == null || !ess.isEnabled()) {
            return;
        }
        if (ess.getSettings().areSignsDisabled() || (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)) {
            return;
        }
        final Block block;
        if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
            Block targetBlock = null;
            try {
                targetBlock = event.getPlayer().getTargetBlock(null, 5);
            } catch (IllegalStateException ex) {
                if (ess.getSettings().isDebug()) {
                    ess.getLogger().log(Level.WARNING, ex.getMessage(), ex);
                }
            }
            block = targetBlock;
        } else {
            block = event.getClickedBlock();
        }
        if (block == null) {
            return;
        }
        final int mat = block.getTypeId();
        if (mat == Material.SIGN_POST.getId() || mat == Material.WALL_SIGN.getId()) {
            final Sign csign = (Sign)block.getState();
            for (EssentialsSign sign : ess.getSettings().enabledSigns()) {
                if (csign.getLine(0).equalsIgnoreCase(sign.getSuccessName())) {
                    //sign.onSignInteract(block, event.getPlayer(), ess);
                    final String signName = sign.getSuccessName();
                    final User user = ess.getUser(event.getPlayer());
                    if (!user.isAuthorized("essentials.signs.use.kit")) {
                        return;
                    }
                    if (!signName.toLowerCase(Locale.ENGLISH).equals("§1[kit]")) {
                        return;
                    }
                    if (!csign.getLine(1).equalsIgnoreCase(config.KIT_NAME)) {
                        return;
                    }
                    // if (user.checkSignThrottle()) {
                    //     return;
                    // }
                    if (!user.isAuthorized("essentials.kit." + config.KIT_NAME)) {
                        event.getPlayer().sendMessage(config.REJECT_MESSAGE);
                        event.setCancelled(true);
                        return;
                    }
                    final Plugin bPermPlugin = getServer().getPluginManager().getPlugin("bPermissions");
                    if (bPermPlugin != null && bPermPlugin.isEnabled()) {
                        getServer().broadcastMessage(String.format(config.ANNOUNCE_MESSAGE, event.getPlayer().getDisplayName()));
                        ApiLayer.addPermission(event.getPlayer().getWorld().getName(),
                                               CalculableType.USER,
                                               event.getPlayer().getName(),
                                               Permission.loadFromString("^essentials.kit." + config.KIT_NAME));
                    } else {
                        getLogger().log(Level.SEVERE, "bPermissions not found!");
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }

    static class BlockSign implements ISign {

        private final transient Sign sign;
        private final transient Block block;

        public BlockSign(final Block block) {
            this.block = block;
            this.sign = (Sign)block.getState();
        }

        @Override
        public final String getLine(final int index) {
            return sign.getLine(index);
        }

        @Override
        public final void setLine(final int index, final String text) {
            sign.setLine(index, text);
        }

        @Override
        public final Block getBlock() {
            return block;
        }

        @Override
        public final void updateSign() {
            sign.update();
        }
    }

}