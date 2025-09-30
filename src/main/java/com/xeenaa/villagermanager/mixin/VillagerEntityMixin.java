package com.xeenaa.villagermanager.mixin;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import com.xeenaa.villagermanager.data.GuardData;
import com.xeenaa.villagermanager.data.GuardDataManager;
import com.xeenaa.villagermanager.data.rank.GuardRank;
import com.xeenaa.villagermanager.data.rank.GuardRankData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to handle guard data persistence for villagers.
 *
 * <p>This mixin intercepts NBT read/write operations to ensure guard data
 * is properly saved and loaded with villager entities.</p>
 *
 * @since 1.0.0
 */
@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    private static final String GUARD_DATA_KEY = "XeenaaGuardData";
    private static final Identifier GUARD_PROFESSION_ID =
        Identifier.of("xeenaa_villager_manager", "guard");

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Saves guard data when villager NBT is written
     */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void saveGuardData(NbtCompound nbt, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // Check if this is a guard villager
        if (isGuardProfession(villager)) {
            World world = villager.getWorld();
            if (world instanceof ServerWorld) {
                GuardDataManager manager = GuardDataManager.get(world);
                GuardData guardData = manager.getGuardData(villager.getUuid());

                if (guardData != null) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    nbt.put(GUARD_DATA_KEY, guardData.serializeNbt(serverWorld.getRegistryManager()));
                    XeenaaVillagerManager.LOGGER.debug("Saved guard data to villager NBT: {}",
                        villager.getUuid());
                }
            }
        }
    }

    /**
     * Loads guard data when villager NBT is read
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void loadGuardData(NbtCompound nbt, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        if (nbt.contains(GUARD_DATA_KEY)) {
            World world = villager.getWorld();
            if (world instanceof ServerWorld serverWorld) {
                GuardDataManager manager = GuardDataManager.get(world);
                GuardData guardData = new GuardData(villager.getUuid());
                guardData.deserializeNbt(nbt.getCompound(GUARD_DATA_KEY), serverWorld.getRegistryManager());

                manager.updateGuardData(villager, guardData);

                XeenaaVillagerManager.LOGGER.debug("Loaded guard data from villager NBT: {}",
                    villager.getUuid());
            }
        }
    }

    /**
     * Checks if a villager has the guard profession
     */
    private boolean isGuardProfession(VillagerEntity villager) {
        String professionString = villager.getVillagerData().getProfession().id();
        Identifier currentProfession = Identifier.of(professionString);
        return GUARD_PROFESSION_ID.equals(currentProfession);
    }

}