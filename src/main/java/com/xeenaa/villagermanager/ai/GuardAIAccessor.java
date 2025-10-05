package com.xeenaa.villagermanager.ai;

/**
 * Accessor interface for reinitializing guard combat AI goals.
 * Used to properly access mixin methods from outside the mixin class.
 */
public interface GuardAIAccessor {
    /**
     * Reinitializes combat goals based on the guard's specialization path.
     * @param isRangedSpecialization true if guard uses ranged/marksman path, false for melee
     */
    void xeenaa$reinitializeCombatGoals(boolean isRangedSpecialization);
}
