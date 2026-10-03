package com.teammetallurgy.aquaculture.item;

import com.teammetallurgy.aquaculture.api.fishing.Hook;
import com.teammetallurgy.aquaculture.api.fishing.Hooks;
import net.minecraft.world.item.Item;

/**
 * Compile-time stub. See {@code src/aquacultureStub/README.md} for why this exists.
 */
public class HookItem extends Item {
    public HookItem(Properties properties) {
        super(properties);
    }

    public Hook getHookType() {
        return Hooks.EMPTY;
    }
}
