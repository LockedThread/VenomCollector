package com.protein.factioncollector.queue;

import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Set;

public class FindCollectorTask extends BukkitRunnable {

    private ArrayList<Chunk> chunks;
    private ArrayDeque<PS> psArrayDeque;
    private int speed;
    private CallBack<Chunk> callBack;

    public FindCollectorTask(Set<PS> psList, int speed, CallBack<Chunk> callBack) {
        this.psArrayDeque = new ArrayDeque<>(psList);
        this.chunks = new ArrayList<>();
        this.speed = speed;
        this.callBack = callBack;
    }


    @Override
    public void run() {
        for (int i = 0; i < speed; i++) {
            if (psArrayDeque.isEmpty()) {
                callBack.call(this.chunks);
                this.cancel();
                return;
            }
            chunks.add(psArrayDeque.poll().asBukkitChunk());
        }
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public interface CallBack<T> {

        void call(ArrayList<T> t);
    }
}
