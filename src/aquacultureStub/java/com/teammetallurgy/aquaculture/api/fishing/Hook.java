package com.teammetallurgy.aquaculture.api.fishing;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Compile-time stub covering only the members TLM's compat layer touches.
 * See {@code src/aquacultureStub/README.md} for why this exists.
 */
public class Hook {
    /** Hook item lookup, indexed by hook name. */
    public static final Map<String, Supplier<Item>> HOOKS = Map.of();

    public Vec3 getWeight() {
        return null;
    }

    public SoundEvent getCatchSound() {
        return null;
    }

    public double getDoubleCatchChance() {
        return 0.0D;
    }

    public double getDurabilityChance() {
        return 0.0D;
    }

    public List<TagKey<Fluid>> getFluids() {
        return List.of();
    }

    public int getLuckModifier() {
        return 0;
    }

    public String getName() {
        return null;
    }

    public Identifier getTexture() {
        return null;
    }
}
