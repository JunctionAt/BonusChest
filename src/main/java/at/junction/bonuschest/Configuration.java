package at.junction.bonuschest;

import org.bukkit.configuration.ConfigurationSection;

public class Configuration {

    private final BonusChest plugin;

    public static String KIT_NAME;
    public static String REJECT_MESSAGE;
    public static String ANNOUNCE_MESSAGE;

    public Configuration(BonusChest plugin) {
        this.plugin = plugin;
    }

    public void save()
    {
        plugin.saveConfig();
    }

    public void load()
    {
        plugin.reloadConfig();
        ConfigurationSection config = plugin.getConfig();
        KIT_NAME = config.getString("kit_name");
        REJECT_MESSAGE = config.getString("reject_message");
        ANNOUNCE_MESSAGE = config.getString("announce_message");
    }

}