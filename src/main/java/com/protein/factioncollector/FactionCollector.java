package com.protein.factioncollector;

import com.google.gson.reflect.TypeToken;
import com.protein.factioncollector.commands.FactionCollectorCommand;
import com.protein.factioncollector.enums.CollectionType;
import com.protein.factioncollector.enums.ItemType;
import com.protein.factioncollector.enums.Messages;
import com.protein.factioncollector.listeners.BlockListener;
import com.protein.factioncollector.listeners.EntityListener;
import com.protein.factioncollector.listeners.FactionListener;
import com.protein.factioncollector.objs.Collector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.venompvp.venom.module.Module;
import org.venompvp.venom.module.ModuleInfo;
import org.venompvp.venom.utils.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;

@ModuleInfo(name = "FactionCollector", author = "Simpleness", version = "2.0", description = "CropHopper and VoidChest that collects mobs, tnt, and crops")
public class FactionCollector extends Module {

    private static FactionCollector instance = null;
    private HashMap<String, Collector> collectorHashMap;
    private File dataFile;
    private ArrayList<String> whiteListedCollectionTypes;
    private EnumMap<CollectionType, ItemStack> guiItemHashMap;

    public static FactionCollector getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        setupModule(this);
        getLogger().info("Initializing FactionCollector properties....");
        final long startTime = System.currentTimeMillis();
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();

        ItemType.HARVESTER_HOE.setItemStack(Utils.configSectionToItemStack(getConfig(), "harvester-hoe-item"));
        ItemType.COLLECTOR.setItemStack(Utils.configSectionToItemStack(getConfig(), "collector-item"));
        ItemType.SELL_WAND.setItemStack(Utils.configSectionToItemStack(getConfig(), "sell-wand-item"));
        ItemType.TNT_WAND.setItemStack(Utils.configSectionToItemStack(getConfig(), "tnt-wand-item"));
        ItemType.GUI_BACKGROUND.setItemStack(Utils.configSectionToItemStack(getConfig(), "background-item"));
        ItemType.INFO.setItemStack(Utils.configSectionToItemStack(getConfig(), "help-item"));


        guiItemHashMap = new EnumMap<>(CollectionType.class);
        whiteListedCollectionTypes = new ArrayList<>(getConfig().getStringList("whitelisted-mobs"));

        whiteListedCollectionTypes.stream().map(whiteListedCollectionType -> CollectionType.valueOf(whiteListedCollectionType.replace("-", "_").toUpperCase())).forEach(collectionType -> {
            ItemStack stack = collectionType.parseEntityType().isPresent() ? getVenom().getSilkUtil().newEggItem(collectionType.parseEntityType().get().getTypeId(), 1) : new ItemStack(Material.matchMaterial(collectionType.name()), 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("gui.item-template.name")));
            meta.setLore(getConfig().getStringList("gui.item-template.lore"));
            stack.setItemMeta(meta);
            guiItemHashMap.put(collectionType, stack);
        });

        ConfigurationSection sellSection = getConfig().getConfigurationSection("sell-values");

        sellSection.getKeys(false).forEach(key -> CollectionType.valueOf(key.toUpperCase().replace("-", "_")).setValue(sellSection.getDouble(key)));

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
                collectorHashMap = new HashMap<>();
            } else try (FileReader reader = new FileReader(dataFile)) {
                HashMap<String, Collector> map = getGson().fromJson(reader, new TypeToken<HashMap<String, Collector>>() {
                }.getType());
                if (map == null || map.isEmpty()) {
                    collectorHashMap = new HashMap<>();
                } else {
                    collectorHashMap = new HashMap<>(map);
                    collectorHashMap.forEach((k, v) -> v.initIgnored());
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        /*
         * Post deserialization
         */

        getCommandHandler().register(this, new FactionCollectorCommand(this));
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new FactionListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);

        getLogger().info("Finished initializing FactionCollector (" + (System.currentTimeMillis() - startTime) + ")");
    }


    public HashMap<String, Collector> getCollectorHashMap() {
        return collectorHashMap;
    }

    @Override
    public void onDisable() {
        if (!collectorHashMap.isEmpty()) {
            try (FileWriter fileWriter = new FileWriter(dataFile)) {
                fileWriter.write(getGson().toJson(collectorHashMap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        disableCommands();
        instance = null;
    }

    public boolean containsCollector(Chunk chunk) {
        return collectorHashMap.containsKey(chunkToString(chunk));
    }

    public Collector findCollector(Chunk chunk) {
        return collectorHashMap.get(chunkToString(chunk));
    }

    public ArrayList<String> getWhiteListedCollectionTypes() {
        return whiteListedCollectionTypes;
    }

    public EnumMap<CollectionType, ItemStack> getGuiItemHashMap() {
        return guiItemHashMap;
    }

    public String chunkToString(Chunk chunk) {
        return chunk.getWorld().getName() + "::" + chunk.getX() + "::" + chunk.getZ();
    }

    public Chunk stringToChunk(String s) {
        String[] split = s.split("::");
        int x = Integer.parseInt(split[1]), z = Integer.parseInt(split[2]);
        return Bukkit.getWorld(split[0]).getChunkAt(x, z);
    }
}
