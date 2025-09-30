package dev.doctor4t.trainmurdermystery.index;

import dev.doctor4t.ratatouille.util.registrar.BlockEntityTypeRegistrar;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.block.entity.HornBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.*;
import net.minecraft.block.entity.BlockEntityType;

public interface TMMBlockEntities {
    BlockEntityTypeRegistrar registrar = new BlockEntityTypeRegistrar(TMM.MOD_ID);

    BlockEntityType<SprinklerBlockEntity> SPRINKLER = registrar.create("sprinkler", BlockEntityType.Builder.create(SprinklerBlockEntity::new, TMMBlocks.GOLD_SPRINKLER, TMMBlocks.STAINLESS_STEEL_SPRINKLER));
    BlockEntityType<SmallDoorBlockEntity> SMALL_GLASS_DOOR = registrar.create("small_glass_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createGlass, TMMBlocks.SMALL_GLASS_DOOR));
    BlockEntityType<SmallDoorBlockEntity> SMALL_WOOD_DOOR = registrar.create("small_wood_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createWood, TMMBlocks.SMALL_WOOD_DOOR));
    BlockEntityType<SmallDoorBlockEntity> SMALL_TRAIN_DOOR = registrar.create("small_train_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createTrain, TMMBlocks.SMALL_TRAIN_DOOR));
    BlockEntityType<WheelBlockEntity> WHEEL = registrar.create("wheel", BlockEntityType.Builder.create(WheelBlockEntity::create, TMMBlocks.WHEEL));
    BlockEntityType<BeveragePlateBlockEntity> BEVERAGE_PLATE = registrar.create("beverage_plate", BlockEntityType.Builder.create(BeveragePlateBlockEntity::new, TMMBlocks.FOOD_PLATTER, TMMBlocks.DRINK_TRAY));
    BlockEntityType<TrimmedBedBlockEntity> TRIMMED_BED = registrar.create("trimmed_bed", BlockEntityType.Builder.create(TrimmedBedBlockEntity::create, TMMBlocks.RED_TRIMMED_BED, TMMBlocks.WHITE_TRIMMED_BED));
    BlockEntityType<HornBlockEntity> HORN = registrar.create("horn", BlockEntityType.Builder.create(HornBlockEntity::new, TMMBlocks.HORN));
    BlockEntityType<ChimneyBlockEntity> CHIMNEY = registrar.create("chimney", BlockEntityType.Builder.create(ChimneyBlockEntity::new, TMMBlocks.CHIMNEY));

    static void initialize() {
        registrar.registerEntries();
    }
}
