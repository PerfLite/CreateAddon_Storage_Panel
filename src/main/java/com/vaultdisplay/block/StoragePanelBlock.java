package com.vaultdisplay.block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.vaultdisplay.blockentity.StoragePanelBlockEntity;
import com.vaultdisplay.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StoragePanelBlock extends BaseEntityBlock implements IWrenchable, SimpleWaterloggedBlock {

    public static final MapCodec<StoragePanelBlock> CODEC = simpleCodec(StoragePanelBlock::new);

    @Override
    public MapCodec<StoragePanelBlock> codec() { return CODEC; }

    public static final DirectionProperty FACING     = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final BooleanProperty WATERLOGGED   = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape WALL_NORTH = Block.box(1, 1, 14, 15, 15, 16);
    private static final VoxelShape WALL_SOUTH = Block.box(1, 1, 0,  15, 15, 2);
    private static final VoxelShape WALL_WEST  = Block.box(14, 1, 1, 16, 15, 15);
    private static final VoxelShape WALL_EAST  = Block.box(0,  1, 1, 2,  15, 15);
    private static final VoxelShape FLOOR      = Block.box(1, 0, 1, 15, 2, 15);
    private static final VoxelShape CEILING    = Block.box(1, 14, 1, 15, 16, 15);

    public StoragePanelBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.WALL)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        AttachFace face;
        Direction facing;

        if (clicked == Direction.UP) {
            face = AttachFace.FLOOR;
            facing = context.getHorizontalDirection();
        } else if (clicked == Direction.DOWN) {
            face = AttachFace.CEILING;
            facing = context.getHorizontalDirection();
        } else {
            face = AttachFace.WALL;
            facing = clicked;
        }

        boolean waterlogged = context.getLevel()
                .getFluidState(context.getClickedPos())
                .getType() == Fluids.WATER;

        return defaultBlockState()
                .setValue(FACE, face)
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, waterlogged);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACE)) {
            case FLOOR   -> FLOOR;
            case CEILING -> CEILING;
            default -> switch (state.getValue(FACING)) {
                case NORTH -> WALL_NORTH;
                case SOUTH -> WALL_SOUTH;
                case WEST  -> WALL_WEST;
                case EAST  -> WALL_EAST;
                default    -> WALL_NORTH;
            };
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        level.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getClockWise()), Block.UPDATE_ALL);
        IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoragePanelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.STORAGE_PANEL.get(),
                StoragePanelBlockEntity::serverTick);
    }
}
