package com.protein.factioncollector.enums;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Optional;

public enum CollectionType {

    /*Items*/
    CACTUS,
    SUGAR_CANE,
    TNT,

    /*Entity Types*/
    CREEPER,
    SKELETON,
    SPIDER,
    GIANT,
    ZOMBIE,
    SLIME,
    GHAST,
    PIG_ZOMBIE,
    ENDERMAN,
    CAVE_SPIDER,
    SILVERFISH,
    BLAZE,
    MAGMA_CUBE,
    ENDER_DRAGON,
    WITHER,
    BAT,
    WITCH,
    ENDERMITE,
    GUARDIAN,
    PIG,
    SHEEP,
    COW,
    CHICKEN,
    SQUID,
    WOLF,
    MUSHROOM_COW,
    SNOWMAN,
    OCELOT,
    IRON_GOLEM,
    HORSE,
    RABBIT,
    VILLAGER,
    ENDER_CRYSTAL;

    private double value;

    public static Optional<CollectionType> fromEntityType(EntityType entityType) {
        return Arrays.stream(CollectionType.values()).filter(collectionType -> collectionType.name().equals(entityType.name())).findFirst();
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Optional<EntityType> parseEntityType() {
        return Arrays.stream(EntityType.values()).filter(entityType -> entityType.name().equals(name())).findFirst();
    }

    public Optional<Material> parseMaterial() {
        return Arrays.stream(Material.values()).filter(material -> material.name().equals(name())).findFirst();
    }

    @Override
    public String toString() {
        return name();
    }
}
