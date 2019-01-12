package com.protein.factioncollector.tasks;

import com.protein.factioncollector.objs.Collector;
import org.bukkit.scheduler.BukkitRunnable;
import org.venompvp.venom.Venom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class SaveCollectorTask extends BukkitRunnable {

    private File dataFile;
    private HashMap<String, Collector> collectorHashMap;

    public SaveCollectorTask(File dataFile, HashMap<String, Collector> collectorHashMap) {
        this.dataFile = dataFile;
        this.collectorHashMap = collectorHashMap;
    }

    @Override
    public void run() {
        System.out.println("The Faction Collector Save Task is running now.");
        if (!collectorHashMap.isEmpty()) {
            try (FileWriter fileWriter = new FileWriter(dataFile)) {
                fileWriter.write(Venom.getInstance().getGson().toJson(collectorHashMap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
