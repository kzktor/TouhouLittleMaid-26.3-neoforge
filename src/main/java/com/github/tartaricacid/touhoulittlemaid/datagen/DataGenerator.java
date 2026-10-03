package com.github.tartaricacid.touhoulittlemaid.datagen;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.datagen.tag.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID)
public class DataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        var generator = event.getGenerator();
        var pack = generator.getPackOutput();
        Set<String> modIds = Set.of(TouhouLittleMaid.MOD_ID);

        // 26.3：数据包注册表分成 world / reloadable 两层，必须按这个顺序挂载，
        // 后一层才会读到「已并入本模组条目」的前一层 lookup（内部是 thenApply 链，取的是当时的 future）。
        event.createWorldRegistryObjects(RegistryDataGenerator.WORLD_BUILDER, modIds);
        event.createReloadableRegistryObjects(RegistryDataGenerator.RELOADABLE_BUILDER, modIds);

        // 可重载层的 lookup 里同时包含世界层（且两层都已并入本模组条目），之后的 provider 统一用它
        var registries = event.getReloadableLookupProvider();

        // Tags
        event.createBlockAndItemTags(
                (output, lookup) -> new TagBlock(output, lookup, TouhouLittleMaid.MOD_ID),
                (output, lookup, blockTags) -> new TagItem(output, lookup, TouhouLittleMaid.MOD_ID)
        );
        generator.addProvider(true, new TagEntity(pack, registries));
        generator.addProvider(true, new TagDamage(pack, registries));
        generator.addProvider(true, new TagTimeline(pack, registries));
        generator.addProvider(true, new TagEnchantment(pack, registries));
        generator.addProvider(true, new TagPaintingVariant(pack, registries));

        // Global Loot Modifier
        generator.addProvider(true, new GlobalLootModifier(pack, registries, TouhouLittleMaid.MOD_ID));

        generator.addProvider(true, new DataMapGenerator(pack, registries));
    }
}
