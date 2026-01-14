package dev.doctor4t.wathe.block;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ToggleableFacingLightBlock extends FacingLightBlock {
    public static final BooleanProperty LIT = Properties.LIT;

    public ToggleableFacingLightBlock(Settings settings) {
        super(settings);
        this.setDefaultState(super.getDefaultState()
                .with(LIT, false));
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient) {
            world.scheduleBlockTick(pos, this, 4);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        super.scheduledTick(state, world, pos, random);
        
        // Check if psycho mode is active
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(world);
        if (gameWorldComponent.isPsychoActive()) {
            // Toggle the light state every 4 ticks when psycho mode is active
            boolean currentLit = state.get(LIT);
            world.setBlockState(pos, state.with(LIT, !currentLit), Block.NOTIFY_ALL);
            world.playSound(null, pos, WatheSounds.BLOCK_LIGHT_TOGGLE, SoundCategory.BLOCKS, 0.3f, currentLit ? 1f : 1.2f);
        }
        
        // Schedule the next tick
        world.scheduleBlockTick(pos, this, 4);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.shouldCancelInteraction()) {
            boolean lit = state.get(LIT);
            world.setBlockState(pos, state.with(LIT, !lit), Block.NOTIFY_ALL);
            world.playSound(null, pos, WatheSounds.BLOCK_LIGHT_TOGGLE, SoundCategory.BLOCKS, 0.5f, lit ? 1f : 1.2f);
            if (!state.get(ACTIVE)) {
                world.playSound(player, pos, WatheSounds.BLOCK_BUTTON_TOGGLE_NO_POWER, SoundCategory.BLOCKS, 0.1f, 1f);
            }
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
        super.appendProperties(builder);
    }
}
