package com.protein.factioncollector;

import com.google.gson.reflect.TypeToken;
import com.protein.factioncollector.enums.Messages;
import com.protein.factioncollector.listeners.EntityListener;
import com.protein.factioncollector.listeners.FactionListener;
import com.protein.factioncollector.objs.Collector;
import com.protein.factioncollector.utils.GsonFactory;
import com.protein.factioncollector.utils.Utils;
import com.protein.lib.gui.GuiListener;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.dustplanet.util.SilkUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class FactionCollector extends JavaPlugin {

    private static FactionCollector instance = null;
    public final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final GsonFactory gsonFactory = new GsonFactory();
    public int[] maxGuiClicksRange;
    public int guiClickThreshold;
    private ArrayList<Collector> collectorLocations;
    private File dataFile;
    private ItemStack collectorItemStack;
    private ItemStack reaperWandItemStack;
    private ArrayList<String> whiteListedMobs;
    private EnumMap<EntityType, ItemStack> guiItemHashMap;
    private EnumMap<EntityType, Double> sellValues;
    private HashMap<UUID, Integer> guiClicks;
    private HashMap<UUID, Integer> guiClickWarnings;
    private SilkUtil silkUtil;
    private Economy econ;
    private WorldGuardPlugin worldGuardPlugin;

    public static FactionCollector getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Initializing FactionCollector properties....");
        final long startTime = System.currentTimeMillis();
        setupDependencies();
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();

        guiClickThreshold = getConfig().getInt("gui-click-threshold");
        String[] split = getConfig().getString("max-gui-clicks-range").split(":");
        maxGuiClicksRange = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])};

        reaperWandItemStack = Utils.configSectionToItemStack("reaper-wand-item");
        collectorItemStack = Utils.configSectionToItemStack("collector-item");

        guiClickWarnings = new HashMap<>();
        guiClicks = new HashMap<>();
        guiItemHashMap = new EnumMap<>(EntityType.class);
        sellValues = new EnumMap<>(EntityType.class);
        whiteListedMobs = new ArrayList<>(getConfig().getStringList("whitelisted-mobs"));


        whiteListedMobs.stream().map(EntityType::valueOf).forEach(entityType -> {
            ItemStack stack = silkUtil.newEggItem(entityType.getTypeId(), 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("gui.item-template.name")));
            meta.setLore(getConfig().getStringList("gui.item-template.lore"));
            stack.setItemMeta(meta);
            guiItemHashMap.put(entityType, stack);
        });

        ConfigurationSection sellSection = getConfig().getConfigurationSection("sell-values");

        sellSection.getKeys(false).forEach(key -> sellValues.put(EntityType.valueOf(key.toUpperCase()), sellSection.getDouble(key)));

        Arrays.stream(Messages.values()).forEach(message -> {
            if (getConfig().isSet("messages." + message.getKey()))
                message.setMessage(getConfig().getString("messages." + message.getKey()));
            else
                getConfig().set("messages." + message.getKey(), message.getMessage());
        });
        saveConfig();

        /*
         * Post Config Loading
         */

        this.dataFile = new File(getDataFolder(), "data.json");
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                collectorLocations = new ArrayList<>();
            } else try (FileReader reader = new FileReader(dataFile)) {
                ArrayList<Collector> arrayList = gsonFactory.getCompactGson().fromJson(reader, new TypeToken<ArrayList<Collector>>() {
                }.getType());
                if (arrayList == null) {
                    collectorLocations = new ArrayList<>();
                } else {
                    collectorLocations = arrayList;
                    arrayList.forEach(Collector::initIgnored);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * Post deserialization
         */

        getCommand("collector").setExecutor(this);

        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new FactionListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        getLogger().log(Level.INFO, "Finished initializing FactionCollector (" + (System.currentTimeMillis() - startTime) + ")");
    }

    private void setupDependencies() {
        String[] dependencies = {"SilkSpawners", "Factions", "Vault", "WorldGuard"};

        ArrayList<String> notEnabled = new ArrayList<>();
        for (String dependency : dependencies) {
            Plugin plugin = getServer().getPluginManager().getPlugin(dependency);
            if (plugin != null) {
                if (dependency.equals(dependencies[1]) && plugin.getDescription().getDepend().contains("MassiveCore")) {
                    getLogger().log(Level.SEVERE, "You must use FactionsUUID or a fork of it, not MassiveCraft Factions.");
                }
                if (dependency.equals(dependencies[0]) && !plugin.getDescription().getAuthors().contains("mushroomhostage")) {
                    getLogger().log(Level.SEVERE, "You have the wrong version of SilkSpawners installed.");
                }
            } else {
                notEnabled.add(dependency);
            }
        }
        if (notEnabled.isEmpty()) {
            silkUtil = SilkUtil.hookIntoSilkSpanwers();
            econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
            worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin(dependencies[3]);
        } else {
            getLogger().log(Level.SEVERE, "You must install " + Utils.toPrettyList(notEnabled) + "!");
            getPluginLoader().disablePlugin(this);
        }

    }

    public ArrayList<Collector> getCollectorLocations() {
        return collectorLocations;
    }

    @Override
    public void onDisable() {
        if (!collectorLocations.isEmpty()) {
            try (FileWriter fileWriter = new FileWriter(dataFile)) {
                fileWriter.write(gsonFactory.getPrettyGson().toJson(collectorLocations));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        instance = null;
    }

    public Collector findCollector(Chunk chunk) {
        return getCollectorLocations().stream().filter(collectorLocation -> chunk.getWorld().getUID().equals(collectorLocation.getLocation().getChunk().getWorld().getUID()) && chunk.getX() == collectorLocation.getLocation().getChunk().getX() && chunk.getZ() == collectorLocation.getLocation().getChunk().getZ()).findFirst().orElse(null);
    }

    public ItemStack getCollectorItemStack() {
        return collectorItemStack;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("collector")) {
            if (!sender.hasPermission("collector.admin")) {
                sender.sendMessage(Messages.NO_PERMISSION.toString());
                return false;
            }
            if (args.length == 0) {
                sender.sendMessage("&e&l[!] Faction Collector");
                sender.sendMessage("&b/collector give [player] [amount]");
                sender.sendMessage("&b/collector givewand [player] [amount]");
            }
            if (args.length >= 2) {
                if (args[0].equalsIgnoreCase("give")) {
                    Player player = getServer().getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(Messages.NOT_ONLINE.toString().replace("{player}", args[1]));
                        return false;
                    }

                    int amount = args.length == 3 ? Integer.parseInt(args[2]) : 1;

                    if (args.length == 3) {
                        IntStream.range(0, amount).forEach(i -> player.getInventory().addItem(collectorItemStack));
                    } else {
                        player.getInventory().addItem(collectorItemStack);
                    }

                    sender.sendMessage(Messages.GIVEN_COLLECTOR.toString().replace("{player}", player.getName()).replace("{amount}", String.valueOf(amount)));
                }
                if (args[0].equalsIgnoreCase("givewand")) {
                    Player player = getServer().getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(Messages.NOT_ONLINE.toString().replace("{player}", args[1]));
                        return false;
                    }

                    int amount = args.length == 3 ? Integer.parseInt(args[2]) : 1;

                    if (args.length == 3) {
                        IntStream.range(0, amount).forEach(i -> player.getInventory().addItem(reaperWandItemStack));
                    } else {
                        player.getInventory().addItem(reaperWandItemStack);
                    }

                    sender.sendMessage(Messages.GIVEN_REAPER_WAND.toString().replace("{player}", player.getName()).replace("{amount}", String.valueOf(amount)));
                }
            }
        }
        return true;
    }

    public ArrayList<String> getWhiteListedMobs() {
        return whiteListedMobs;
    }

    public EnumMap<EntityType, ItemStack> getGuiItemHashMap() {
        return guiItemHashMap;
    }

    public Economy getEcon() {
        return econ;
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }

    public SilkUtil getSilkUtil() {
        return silkUtil;
    }

    public GsonFactory getGsonFactory() {
        return gsonFactory;
    }

    public EnumMap<EntityType, Double> getSellValues() {
        return sellValues;
    }

    public HashMap<UUID, Integer> getGuiClicks() {
        return guiClicks;
    }

    public HashMap<UUID, Integer> getGuiClickWarnings() {
        return guiClickWarnings;
    }

    public ItemStack getReaperWandItemStack() {
        return reaperWandItemStack;
    }
}
