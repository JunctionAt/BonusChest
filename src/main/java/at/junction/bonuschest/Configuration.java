package at.junction.bonuschest;

import org.bukkit.configuration.file.FileConfiguration;

public class Configuration {

    public static String KIT_NAME;
    public static String REJECT_MESSAGE;
    public static String ANNOUNCE_MESSAGE;

    public void load(final BonusChest plugin)
    {
        final FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        REJECT_MESSAGE = config.getString("reject_message");
        ANNOUNCE_MESSAGE = config.getString("announce_message");
    }

}