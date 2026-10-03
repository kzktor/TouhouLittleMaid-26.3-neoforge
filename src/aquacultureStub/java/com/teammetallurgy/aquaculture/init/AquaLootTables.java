package com.teammetallurgy.aquaculture.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Compile-time stub. See {@code src/aquacultureStub/README.md} for why this exists.
 */
public final class AquaLootTables {
    public static final ResourceKey<LootTable> LAVA_FISHING = key("gameplay/fishing/lava_fishing");
    public static final ResourceKey<LootTable> NETHER_FISHING = key("gameplay/fishing/nether_fishing");

    private AquaLootTables() {
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("aquaculture", path));
    }
}
