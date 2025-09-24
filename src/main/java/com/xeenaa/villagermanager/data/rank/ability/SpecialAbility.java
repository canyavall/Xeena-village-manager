package com.xeenaa.villagermanager.data.rank.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

/**
 * Abstract base class for special abilities that guards can use at higher ranks.
 * Each ability has a chance to trigger and specific effects.
 *
 * @since 2.0.0
 */
public abstract class SpecialAbility {
    protected final String name;
    protected final String description;
    protected final float triggerChance;

    protected SpecialAbility(String name, String description, float triggerChance) {
        this.name = name;
        this.description = description;
        this.triggerChance = triggerChance;
    }

    /**
     * Checks if the ability should trigger based on random chance
     */
    public boolean shouldTrigger(Random random) {
        return random.nextFloat() < triggerChance;
    }

    /**
     * Executes the ability effect
     * @param guard The guard using the ability
     * @param target The target entity (may be null for some abilities)
     */
    public abstract void execute(VillagerEntity guard, LivingEntity target);

    /**
     * Gets a description of the ability for UI display
     */
    public Text getDisplayText() {
        return Text.literal(String.format("%s (%.0f%% chance): %s",
                                         name, triggerChance * 100, description));
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getTriggerChance() { return triggerChance; }
}