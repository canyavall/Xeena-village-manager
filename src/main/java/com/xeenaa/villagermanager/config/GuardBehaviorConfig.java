package com.xeenaa.villagermanager.config;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Immutable configuration record for guard behavior settings.
 *
 * <p>This record encapsulates all configurable behavior parameters for guards:</p>
 * <ul>
 *   <li>Detection range for threat scanning</li>
 *   <li>Guard mode controlling movement behavior</li>
 *   <li>Profession lock preventing profession changes</li>
 *   <li>Follow target player UUID for FOLLOW mode</li>
 * </ul>
 *
 * <p>Configuration values are validated to ensure they remain within acceptable bounds.</p>
 *
 * @param detectionRange      the range in blocks for threat detection (10.0 - 30.0)
 * @param guardMode          the guard mode controlling movement behavior
 * @param professionLocked   whether the profession is locked from changes
 * @param followTargetPlayerId the UUID of the player to follow in FOLLOW mode (nullable)
 * @since 1.0.0
 */
public record GuardBehaviorConfig(
    double detectionRange,
    GuardMode guardMode,
    boolean professionLocked,
    UUID followTargetPlayerId
) {

    /**
     * Default configuration values matching current guard behavior.
     */
    public static final GuardBehaviorConfig DEFAULT = new GuardBehaviorConfig(
        20.0,              // Default detection range
        GuardMode.PATROL,  // Default to patrol mode
        false,             // Profession not locked by default
        null               // No follow target by default
    );

    /**
     * Minimum allowed detection range in blocks.
     */
    public static final double MIN_DETECTION_RANGE = 10.0;

    /**
     * Maximum allowed detection range in blocks.
     */
    public static final double MAX_DETECTION_RANGE = 30.0;

    /**
     * Compact constructor with validation.
     */
    public GuardBehaviorConfig {
        // Validate detection range
        if (detectionRange < MIN_DETECTION_RANGE) {
            detectionRange = MIN_DETECTION_RANGE;
        } else if (detectionRange > MAX_DETECTION_RANGE) {
            detectionRange = MAX_DETECTION_RANGE;
        }

        // Ensure non-null guard mode
        if (guardMode == null) {
            guardMode = GuardMode.PATROL;
        }

        // followTargetPlayerId can be null (nullable field)
    }

    /**
     * Serializes this configuration to NBT.
     *
     * @return the NBT compound containing configuration data
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble("DetectionRange", detectionRange);
        nbt.putString("GuardMode", guardMode.asString());
        nbt.putBoolean("ProfessionLocked", professionLocked);

        if (followTargetPlayerId != null) {
            nbt.putUuid("FollowTargetPlayerId", followTargetPlayerId);
        }

        return nbt;
    }

    /**
     * Deserializes configuration from NBT.
     * Handles migration from old config format to new format.
     *
     * @param nbt the NBT compound to read from
     * @return the deserialized configuration
     */
    public static GuardBehaviorConfig fromNbt(NbtCompound nbt) {
        double detectionRange = nbt.contains("DetectionRange") ?
            nbt.getDouble("DetectionRange") : DEFAULT.detectionRange;

        // New field: GuardMode (with migration from old PatrolEnabled field)
        GuardMode guardMode;
        if (nbt.contains("GuardMode")) {
            guardMode = GuardMode.fromString(nbt.getString("GuardMode"));
        } else if (nbt.contains("PatrolEnabled")) {
            // Migration: convert old PatrolEnabled boolean to GuardMode
            guardMode = nbt.getBoolean("PatrolEnabled") ? GuardMode.PATROL : GuardMode.STAND;
        } else {
            guardMode = DEFAULT.guardMode;
        }

        // New field: ProfessionLocked
        boolean professionLocked = nbt.contains("ProfessionLocked") ?
            nbt.getBoolean("ProfessionLocked") : DEFAULT.professionLocked;

        // New field: FollowTargetPlayerId (nullable)
        UUID followTargetPlayerId = nbt.contains("FollowTargetPlayerId") ?
            nbt.getUuid("FollowTargetPlayerId") : DEFAULT.followTargetPlayerId;

        return new GuardBehaviorConfig(detectionRange, guardMode, professionLocked, followTargetPlayerId);
    }

    /**
     * PacketCodec for network serialization.
     */
    public static final PacketCodec<RegistryByteBuf, GuardBehaviorConfig> CODEC =
        new PacketCodec<RegistryByteBuf, GuardBehaviorConfig>() {
            @Override
            public GuardBehaviorConfig decode(RegistryByteBuf buf) {
                double detectionRange = buf.readDouble();
                GuardMode guardMode = GuardMode.CODEC.decode(buf);
                boolean professionLocked = buf.readBoolean();

                // Read nullable UUID
                boolean hasFollowTarget = buf.readBoolean();
                UUID followTargetPlayerId = hasFollowTarget ? Uuids.PACKET_CODEC.decode(buf) : null;

                return new GuardBehaviorConfig(detectionRange, guardMode, professionLocked, followTargetPlayerId);
            }

            @Override
            public void encode(RegistryByteBuf buf, GuardBehaviorConfig config) {
                buf.writeDouble(config.detectionRange);
                GuardMode.CODEC.encode(buf, config.guardMode);
                buf.writeBoolean(config.professionLocked);

                // Write nullable UUID
                boolean hasFollowTarget = config.followTargetPlayerId != null;
                buf.writeBoolean(hasFollowTarget);
                if (hasFollowTarget) {
                    Uuids.PACKET_CODEC.encode(buf, config.followTargetPlayerId);
                }
            }
        };

    /**
     * Checks if this configuration uses default values.
     *
     * @return true if all values match the default configuration
     */
    public boolean isDefault() {
        return this.equals(DEFAULT);
    }
}
