package com.teammetallurgy.aquaculture.init;

import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * Compile-time stub. See {@code src/aquacultureStub/README.md} for why this exists.
 *
 * <p>The suppliers throw rather than returning a placeholder: this stub is never on the runtime
 * classpath, so reaching one of these means the compat layer ran without the real mod installed.
 */
public final class AquaSounds {
    public static final Supplier<SoundEvent> BOBBER_LAND_IN_LAVA = AquaSounds::unreachable;
    public static final Supplier<SoundEvent> BOBBER_BAIT_BREAK = AquaSounds::unreachable;

    private AquaSounds() {
    }

    private static SoundEvent unreachable() {
        throw new UnsupportedOperationException(
                "aquaculture stub — this class must not be present at runtime; the real Aquaculture mod provides it");
    }
}
