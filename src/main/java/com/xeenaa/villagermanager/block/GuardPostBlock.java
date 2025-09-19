package com.xeenaa.villagermanager.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Guard Post block that serves as the workstation for the Guard profession.
 * <p>
 * This block represents a medieval-style guard post that villagers with the Guard
 * profession will use as their workstation. The block is directional and has a
 * custom shape to represent a guard tower or post structure.
 * </p>
 * <p>
 * The block follows Minecraft 1.21.1 conventions for workstation blocks and
 * integrates properly with the villager profession system.
 * </p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager
 */
public class GuardPostBlock extends Block {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardPostBlock.class);

    // Block properties
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // Custom shape for the guard post (resembling a small tower/post)
    private static final VoxelShape SHAPE_NORTH = VoxelShapes.union(
        // Base platform (slightly larger than center post)
        Block.createCuboidShape(2, 0, 2, 14, 2, 14),
        // Center post
        Block.createCuboidShape(6, 2, 6, 10, 14, 10),
        // Top platform
        Block.createCuboidShape(4, 14, 4, 12, 16, 12)
    );

    // Rotated shapes for different facings
    private static final VoxelShape SHAPE_EAST = VoxelShapes.union(
        Block.createCuboidShape(2, 0, 2, 14, 2, 14),
        Block.createCuboidShape(6, 2, 6, 10, 14, 10),
        Block.createCuboidShape(4, 14, 4, 12, 16, 12)
    );

    private static final VoxelShape SHAPE_SOUTH = VoxelShapes.union(
        Block.createCuboidShape(2, 0, 2, 14, 2, 14),
        Block.createCuboidShape(6, 2, 6, 10, 14, 10),
        Block.createCuboidShape(4, 14, 4, 12, 16, 12)
    );

    private static final VoxelShape SHAPE_WEST = VoxelShapes.union(
        Block.createCuboidShape(2, 0, 2, 14, 2, 14),
        Block.createCuboidShape(6, 2, 6, 10, 14, 10),
        Block.createCuboidShape(4, 14, 4, 12, 16, 12)
    );

    /**
     * Creates a new Guard Post block with appropriate settings.
     * <p>
     * The block is configured to have similar properties to other workstation
     * blocks like the smithing table, with appropriate hardness and resistance.
     * </p>
     *
     * @param settings the block settings to use
     * @throws IllegalArgumentException if settings is null
     * @since 1.0.0
     */
    public GuardPostBlock(Settings settings) {
        super(Objects.requireNonNull(settings, "Block settings must not be null"));

        // Set default state with north facing
        this.setDefaultState(
            this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
        );

        LOGGER.debug("Guard Post block created with default state: facing=NORTH");
    }

    /**
     * Handles player interaction with the Guard Post block.
     * <p>
     * Currently, the block doesn't have a special interaction beyond serving
     * as a workstation for villagers. Future versions might add a GUI or
     * special functionality.
     * </p>
     *
     * @param state the block state
     * @param world the world instance
     * @param pos the block position
     * @param player the interacting player
     * @param hand the hand used for interaction
     * @param hit the hit result
     * @return the action result
     * @since 1.0.0
     */
    @Override
    protected ActionResult onUse(
        BlockState state,
        World world,
        BlockPos pos,
        PlayerEntity player,
        BlockHitResult hit
    ) {
        // Log interaction for debugging
        if (!world.isClient) {
            LOGGER.debug("Player {} interacted with Guard Post at {}",
                player.getName().getString(), pos);
        }

        // For now, the guard post doesn't have special functionality
        // In the future, this could open a guard management GUI
        return ActionResult.SUCCESS;
    }

    /**
     * Gets the placement state for the block when placed by a player.
     * <p>
     * The block will face away from the player when placed, which is the
     * conventional behavior for directional workstation blocks.
     * </p>
     *
     * @param ctx the placement context
     * @return the block state for placement, or null if placement is invalid
     * @since 1.0.0
     */
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Objects.requireNonNull(ctx, "Placement context must not be null");

        // Face away from the player (opposite of player's facing direction)
        Direction playerFacing = ctx.getHorizontalPlayerFacing();
        Direction blockFacing = playerFacing.getOpposite();

        LOGGER.debug("Placing Guard Post with facing: {} (player facing: {})",
            blockFacing, playerFacing);

        return this.getDefaultState().with(FACING, blockFacing);
    }

    /**
     * Gets the outline shape of the block for rendering and collision.
     *
     * @param state the block state
     * @param world the world instance
     * @param pos the block position
     * @param context the shape context
     * @return the voxel shape for the block
     * @since 1.0.0
     */
    @Override
    public VoxelShape getOutlineShape(
        BlockState state,
        BlockView world,
        BlockPos pos,
        ShapeContext context
    ) {
        // Return the appropriate shape based on facing direction
        // Note: For this simple post design, all facings use the same shape
        // In a more complex design, shapes could vary by direction
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH; // Fallback
        };
    }

    /**
     * Rotates the block state.
     *
     * @param state the current block state
     * @param rotation the rotation to apply
     * @return the rotated block state
     * @since 1.0.0
     */
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        Objects.requireNonNull(state, "Block state must not be null");
        Objects.requireNonNull(rotation, "Rotation must not be null");

        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    /**
     * Mirrors the block state.
     *
     * @param state the current block state
     * @param mirror the mirror transformation to apply
     * @return the mirrored block state
     * @since 1.0.0
     */
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        Objects.requireNonNull(state, "Block state must not be null");
        Objects.requireNonNull(mirror, "Mirror must not be null");

        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    /**
     * Appends block state properties to the state manager.
     * <p>
     * This method registers the FACING property so the block can store
     * its orientation in the world.
     * </p>
     *
     * @param builder the state manager builder
     * @since 1.0.0
     */
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        Objects.requireNonNull(builder, "State manager builder must not be null");

        builder.add(FACING);
        LOGGER.debug("Added FACING property to Guard Post block state manager");
    }


    /**
     * Gets the default block settings for the Guard Post.
     * <p>
     * This static factory method provides the standard settings used
     * for Guard Post blocks, including hardness, resistance, and material.
     * </p>
     *
     * @return the default block settings
     * @since 1.0.0
     */
    public static Settings getDefaultSettings() {
        return Settings.copy(Blocks.SMITHING_TABLE)
            .strength(2.5f, 6.0f)  // Similar to smithing table
            .requiresTool();        // Requires appropriate tool to break
    }
}