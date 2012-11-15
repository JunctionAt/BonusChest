package at.junction.bonuschest;

import java.util.logging.Level;
import java.util.Locale;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.inventory.*;
import org.bukkit.ChatColor;
import net.minecraft.server.StructurePieceTreasure;
import net.minecraft.server.WeightedRandom;
import net.minecraft.server.TileEntityChest;
import net.minecraft.server.ItemStack;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import de.bananaco.bpermissions.api.util.Permission;

public class BonusChest extends JavaPlugin implements Listener {

    public final Configuration config = new Configuration();

    private static final StructurePieceTreasure[] goods = new StructurePieceTreasure[] {
        new StructurePieceTreasure(Material.STICK.getId(), 0, 1, 3, 10),
        new StructurePieceTreasure(Material.WOOD.getId(), 0, 1, 3, 10),
        new StructurePieceTreasure(Material.LOG.getId(), 0, 1, 3, 10),
        new StructurePieceTreasure(Material.STONE_AXE.getId(), 0, 1, 1, 3),
        new StructurePieceTreasure(Material.WOOD_AXE.getId(), 0, 1, 1, 5),
        new StructurePieceTreasure(Material.STONE_PICKAXE.getId(), 0, 1, 1, 3),
        new StructurePieceTreasure(Material.WOOD_PICKAXE.getId(), 0, 1, 1, 5),
        new StructurePieceTreasure(Material.APPLE.getId(), 0, 2, 3, 5),
        new StructurePieceTreasure(Material.BREAD.getId(), 0, 2, 3, 3)
    };

    @Override
    public void onEnable() {
        config.load(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().log(Level.INFO, getDescription().getName() + " " + getDescription().getVersion() + " enabled.");
    }
    
    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, getDescription().getName() + " " + getDescription().getVersion() + " disabled.");
    }

    @EventHandler
    public void onSignChangeEvent(final SignChangeEvent event) {
        final Player p = event.getPlayer();
        if (event.getLine(0).equals("[BonusChest]") &&
            ApiLayer.hasPermission(event.getPlayer().getWorld().getName(),
                                   CalculableType.USER,
                                   event.getPlayer().getName(),
                                   "bonuschest.set")) {
            event.setLine(0, "§6[BonusChest]");
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Block block = event.getClickedBlock();
        final int mat = block.getTypeId();
        if (mat == Material.SIGN_POST.getId() || mat == Material.WALL_SIGN.getId()) {
            final Sign csign = (Sign)block.getState();
            if (csign.getLine(0).equalsIgnoreCase("§6[BonusChest]")) {
                switch (event.getAction()) {
                case LEFT_CLICK_BLOCK:
                    if (!ApiLayer.hasPermission(event.getPlayer().getWorld().getName(),
                                                CalculableType.USER,
                                                event.getPlayer().getName(),
                                                "bonuschest.set")) {
                        event.setCancelled(true);
                    }
                    return;
                case RIGHT_CLICK_BLOCK:
                    if (ApiLayer.hasPermission(event.getPlayer().getWorld().getName(),
                                               CalculableType.USER,
                                               event.getPlayer().getName(),
                                               "bonuschest.used")) {
                        event.getPlayer().sendMessage(config.REJECT_MESSAGE);
                        event.setCancelled(true);
                        return;
                    }
                    final Inventory inventory = event.getPlayer().getInventory();
                    final TileEntityChest chest = new TileEntityChest();
                    StructurePieceTreasure.a(new Random(event.getPlayer().getWorld().getSeed()), goods, chest, 10);
                    for (final ItemStack item : chest.getContents()) {
                        if (item != null) {
                            inventory.addItem(new org.bukkit.inventory.ItemStack(item.id, item.count));
                        }
                    }
                    inventory.addItem(new org.bukkit.inventory.ItemStack(Material.TORCH.getId(), 4));
                    inventory.addItem(new org.bukkit.inventory.ItemStack(Material.CHEST.getId(), 1));
                    event.getPlayer().updateInventory();
                    getServer().broadcastMessage(String.format(ChatColor.translateAlternateColorCodes('&', config.ANNOUNCE_MESSAGE), event.getPlayer().getDisplayName()));
                    ApiLayer.addPermission(event.getPlayer().getWorld().getName(),
                                           CalculableType.USER,
                                           event.getPlayer().getName(),
                                           Permission.loadFromString("bonuschest.used"));
                }
            }
        }
    }

}