package com.github.tartaricacid.touhoulittlemaid.datagen;

import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 26.3 把数据包注册表拆成了「world（世界层，含维度）」与「reloadable（可重载层）」两层，
 * 各自要单独挂到 {@code GatherDataEvent} 上（见 {@link DataGenerator}）。
 * <p>
 * 原先「继承 {@code DatapackBuiltinEntriesProvider} 再 {@code addProvider}」的写法对不上新 API：
 * 26.3 的构造函数只收 {@code Collection<RegistryData<?>>}，层的信息由
 * {@code forWorldLayer} / {@code forReloadableLayer} 两个工厂方法填，
 * 所以这里退化成「持有两个 {@link RegistrySetBuilder} 的工具类」，
 * 由 {@code GatherDataEvent#createWorldRegistryObjects} / {@code #createReloadableRegistryObjects} 挂载。
 */
public final class RegistryDataGenerator {
    /**
     * 世界层注册表。附魔、伤害类型、画、时间线在 26.3 都属于「同步注册表」，归这一层。
     */
    public static final RegistrySetBuilder WORLD_BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, EnchantmentKeys::bootstrap)
            .add(Registries.DAMAGE_TYPE, DamageTypeProvider::bootstrap)
            .add(Registries.PAINTING_VARIANT, RegistryDataGenerator::genPainting)
            .add(Registries.TIMELINE, TimelinesProvider::bootstrap);

    /**
     * 可重载层注册表。26.3 起战利品表、进度、配方都是可重载注册表，
     * 直接以 {@code SingleRegistryBootstrap} / {@code MultiRegistryBootstrap} 的形式挂在这里，
     * 不再各自当 DataProvider 往 {@code src/generated} 里写 json。
     */
    public static final RegistrySetBuilder RELOADABLE_BUILDER = new RegistrySetBuilder()
            .add(Registries.LOOT_TABLE, new LootTableProvider(Set.of(), List.of(
                    new LootTableProvider.SubProviderEntry(LootTableGenerator.ChestLootTables::new, LootContextParamSets.CHEST),
                    new LootTableProvider.SubProviderEntry(LootTableGenerator.AdvancementLootTables::new, LootContextParamSets.ADVANCEMENT_REWARD),
                    new LootTableProvider.SubProviderEntry(LootTableGenerator.EntityLootTables::new, LootContextParamSets.ENTITY),
                    new LootTableProvider.SubProviderEntry(LootTableGenerator.BlockLootTables::new, LootContextParamSets.BLOCK)
            )))
            .add(Registries.ADVANCEMENT, AdvancementDataGen.PROVIDER)
            .add(RecipeGenerator.BOOTSTRAP);

    private RegistryDataGenerator() {
    }

    private static void genPainting(BootstrapContext<PaintingVariant> ctx) {
        var id = IdentifierUtil.modLoc("wine_fox");
        ctx.register(ResourceKey.create(Registries.PAINTING_VARIANT, id),
                new PaintingVariant(2, 3, id, Optional.empty(), Optional.empty()));
    }
}
