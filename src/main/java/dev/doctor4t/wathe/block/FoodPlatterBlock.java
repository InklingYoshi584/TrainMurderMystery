package dev.doctor4t.wathe.block;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.wathe.index.WatheBlockEntities;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.item.CocktailItem;
import dev.doctor4t.wathe.util.PoisonUtils;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import java.util.List;

public class FoodPlatterBlock extends BlockWithEntity {
    public static final MapCodec<FoodPlatterBlock> CODEC = createCodec(FoodPlatterBlock::new);

    public FoodPlatterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        BeveragePlateBlockEntity plate = new BeveragePlateBlockEntity(pos, state);
        plate.setDrink(false);
        return plate;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return this.getShape(state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getShape(state);
    }

    protected VoxelShape getShape(BlockState state) {
        return createCuboidShape(0, 0, 0, 16, 2, 16);
    }

    @Override
    protected ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof BeveragePlateBlockEntity blockEntity)) return ActionResult.PASS;

        if (player.isCreative()) {
            ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);
            if (!heldItem.isEmpty()) {
                blockEntity.addItem(heldItem);
                return ActionResult.SUCCESS;
            }
        }
        if (player.getStackInHand(Hand.MAIN_HAND).isOf(WatheItems.POISON_VIAL) && blockEntity.getPoisoner() == null) {
            blockEntity.setPoisoner(player.getUuidAsString());
            player.getStackInHand(Hand.MAIN_HAND).decrement(1);
            player.playSoundToPlayer(SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.5f, 1f);
            
            // Spread poison to adjacent food platters and drink trays
            if (!world.isClient) {
                PoisonUtils.spreadPlatePoison(world, pos, player.getUuidAsString());
            }
            
            return ActionResult.SUCCESS;
        }
        if (player.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
            List<ItemStack> platter = blockEntity.getStoredItems();
            if (platter.isEmpty()) return ActionResult.SUCCESS;

            // Check if player already has food or drink items
            boolean hasFoodItem = false;
            boolean hasDrinkItem = false;
            
            // Check player's inventory for existing food/drink items
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack invItem = player.getInventory().getStack(i);
                FoodComponent food = (FoodComponent)invItem.get(DataComponentTypes.FOOD);
                if (!invItem.isEmpty()) {
                    // Check if item is a drink (CocktailItem)
                    if (invItem.getItem() instanceof CocktailItem) {
                        hasDrinkItem = true;
                    }
                    
                    // Check if item is food (has food component or is edible)
                    else if (food != null) {
                        hasFoodItem = true;
                    }
                }
            }

            // Check if the platter contains food or drink items
            boolean platterHasFood = false;
            boolean platterHasDrink = false;
            for (ItemStack platterItem : platter) {
                FoodComponent foodComponent = (FoodComponent)platterItem.get(DataComponentTypes.FOOD);
                if (platterItem.getItem() instanceof CocktailItem) {
                    platterHasDrink = true;
                } else if (foodComponent != null) {
                    platterHasFood = true;
                }
            }

            // Prevent picking up if player already has the same type of item
            if ((platterHasFood && hasFoodItem) || (platterHasDrink && hasDrinkItem)) {
                return ActionResult.PASS;
            }

            // Original logic: check for exact same item type
            boolean hasPlatterItem = false;
            for (ItemStack platterItem : platter) {
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack invItem = player.getInventory().getStack(i);
                    if (invItem.getItem() == platterItem.getItem()) {
                        hasPlatterItem = true;
                        break;
                    }
                }
                if (hasPlatterItem) break;
            }

            if (!hasPlatterItem) {
                ItemStack randomItem = platter.get(world.random.nextInt(platter.size())).copy();
                randomItem.setCount(1);
                randomItem.set(DataComponentTypes.MAX_STACK_SIZE, 1);
                String poisoner = blockEntity.getPoisoner();
                if (poisoner != null) {
                    randomItem.set(WatheDataComponentTypes.POISONER, poisoner);
                    blockEntity.setPoisoner(null);
                    
                    // Remove poison from adjacent plates when item is taken
                    if (!world.isClient) {
                        removeAdjacentPlatePoison(world, pos, poisoner);
                    }
                }
                player.playSoundToPlayer(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1f);
                player.setStackInHand(Hand.MAIN_HAND, randomItem);
            }
        }

        return ActionResult.PASS;
    }

    /**
     * Remove poison from adjacent plates when an item is taken from a poisoned plate.
     */
    private void removeAdjacentPlatePoison(World world, BlockPos centerPos, String poisoner) {
        int radius = 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centerPos.add(dx, dy, dz);
                    
                    // Skip the center position (source of poison removal)
                    if (pos.equals(centerPos)) continue;
                    
                    // Check if this is a food platter or drink tray
                    if (world.getBlockEntity(pos) instanceof BeveragePlateBlockEntity plateEntity) {
                        // Only remove poison from plates with the same poisoner
                        if (poisoner.equals(plateEntity.getPoisoner())) {
                            // Remove the poison
                            plateEntity.setPoisoner(null);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient || !type.equals(WatheBlockEntities.BEVERAGE_PLATE)) return null;
        return BeveragePlateBlockEntity::clientTick;
    }
}
