package com.protein.factioncollector.enums;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public enum CollectionType {

    /*Items*/
    CACTUS(Material.CACTUS),
    SUGAR_CANE(Material.SUGAR_CANE),
    TNT(Material.TNT),

    /*Entity Types*/
    CREEPER(EntityType.CREEPER),
    SKELETON(EntityType.SKELETON),
    SPIDER(EntityType.SPIDER),
    GIANT(EntityType.GIANT),
    ZOMBIE(EntityType.ZOMBIE),
    SLIME(EntityType.SLIME),
    GHAST(EntityType.GHAST),
    PIG_ZOMBIE(EntityType.PIG_ZOMBIE),
    ENDERMAN(EntityType.ENDERMAN),
    CAVE_SPIDER(EntityType.CAVE_SPIDER),
    SILVERFISH(EntityType.SILVERFISH),
    BLAZE(EntityType.BLAZE),
    MAGMA_CUBE(EntityType.MAGMA_CUBE),
    ENDER_DRAGON(EntityType.ENDER_DRAGON),
    WITHER(EntityType.WITHER),
    BAT(EntityType.BAT),
    WITCH(EntityType.WITCH),
    ENDERMITE(EntityType.ENDERMITE),
    GUARDIAN(EntityType.GUARDIAN),
    PIG(EntityType.PIG),
    SHEEP(EntityType.SHEEP),
    COW(EntityType.COW),
    CHICKEN(EntityType.CHICKEN),
    SQUID(EntityType.SQUID),
    WOLF(EntityType.WOLF),
    MUSHROOM_COW(EntityType.MUSHROOM_COW),
    SNOWMAN(EntityType.SNOWMAN),
    OCELOT(EntityType.OCELOT),
    IRON_GOLEM(EntityType.IRON_GOLEM),
    HORSE(EntityType.HORSE),
    RABBIT(EntityType.RABBIT),
    VILLAGER(EntityType.VILLAGER),
    ENDER_CRYSTAL(EntityType.ENDER_CRYSTAL);

    private Material material = null;
    private EntityType entityType = null;

    CollectionType(EntityType entityType) {
        this.entityType = entityType;
    }

    CollectionType(Material material) {
        this.material = material;
    }

    private double value;

    public static CollectionType fromEntityType(EntityType entityType) {
        for (CollectionType value : values()) {
            if (value.entityType == entityType) {
                return value;
            }
        }
        return null;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public EntityType parseEntityType() {
        return entityType;
    }

    public Material parseMaterial() {
        return material;
    }

    @Override
    public String toString() {
        return name();
    }

    public Material getMaterial() {
        return material;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
