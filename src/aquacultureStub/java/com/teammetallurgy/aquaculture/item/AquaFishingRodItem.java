package com.teammetallurgy.aquaculture.item;

import com.teammetallurgy.aquaculture.api.fishing.Hook;
import com.teammetallurgy.aquaculture.api.fishing.Hooks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
// 26.3 把 ItemContainerContents 从 net.minecraft.core.component 挪到了 world.item.component
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Compile-time stub covering the rod accessors TLM's compat layer calls.
 * See {@code src/aquacultureStub/README.md} for why this exists.
 *
 * <p>It extends {@link Item} only so that {@code itemStack.getItem() instanceof AquaFishingRodItem}
 * is a legal (non-inconvertible) test.
 */
public class AquaFishingRodItem extends Item {
    public AquaFishingRodItem(Properties properties) {
        super(properties);
    }

    public ToolMaterial getTier() {
        return ToolMaterial.WOOD;
    }

    /** The rod's 4-slot container (hook, bait, line, bobber). */
    public static ItemContainerContents getHandler(ItemStack stack) {
        return ItemContainerContents.EMPTY;
    }

    public static Hook getHookType(ItemStack stack) {
        return Hooks.EMPTY;
    }

    public static ItemStack getBait(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    public static ItemStack getFishingLine(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    public static ItemStack getBobber(ItemStack stack) {
        return ItemStack.EMPTY;
    }
}
