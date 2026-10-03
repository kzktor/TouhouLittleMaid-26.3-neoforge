package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.blockentity.BlockEntityMaidBed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * 女仆床。
 * <p>
 * 26.3 起 {@code LivingEntity.startSleeping} 会检查 {@code AbstractBedBlock}，
 * 原版也删掉了 {@code Block#isBed}，所以这里必须继承 {@link AbstractBedBlock} 才能让女仆躺下。
 * 与 {@code BedBlock} 的差别是：玩家右键不做任何事（女仆由 AI 直接调用 {@code startSleeping}），
 * 也不做「使用即销毁」。
 */
public class BlockMaidBed extends AbstractBedBlock implements EntityBlock {
    public static final Map<DyeColor, BlockMaidBed> COLOR_TO_BLOCK = new EnumMap<>(DyeColor.class);

    private static final VoxelShape BASE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

    private final DyeColor color;

    public BlockMaidBed(Identifier id, DyeColor color) {
        super(BlockBehaviour.Properties.of()
                .mapColor(color)
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .sound(SoundType.WOOD)
                .strength(0.2F)
                .bounceRestitution(0.75F)
                .noOcclusion());
        this.color = color;
        COLOR_TO_BLOCK.put(color, this);
    }

    public BlockMaidBed(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
        COLOR_TO_BLOCK.put(color, this);
    }

    /**
     * 根据颜色获取对应的方块实例
     */
    @Nullable
    public static BlockMaidBed getByColor(DyeColor color) {
        return COLOR_TO_BLOCK.get(color);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return BASE;
    }

    @Override
    protected EnvironmentAttribute<BedRule> getBedEnvironmentAttribute() {
        return EnvironmentAttributes.BED_RULE;
    }

    @Override
    protected InteractionResult destroyOnUse(BlockState state, Level level, BlockPos pos, Player player) {
        return InteractionResult.PASS;
    }

    @Override
    protected void destroyOnLeave(Level level, BlockPos pos) {
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // 女仆床只给女仆睡（AI 直接调 startSleeping），玩家右键不做任何事
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        BlockPos headPos = pos.relative(state.getValue(FACING));
        if (worldIn.getBlockEntity(headPos) instanceof BlockEntityMaidBed bed) {
            bed.setColor(this.color);
        }
    }

    @Override
    public void fallOn(Level worldIn, BlockState blockState, BlockPos pos, Entity entityIn, double fallDistance) {
        super.fallOn(worldIn, blockState, pos, entityIn, fallDistance * 0.5f);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.POPPED;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == BedPart.HEAD) {
            return new BlockEntityMaidBed(pos, state);
        }
        return null;
    }

    public DyeColor getColor() {
        return color;
    }
}
