package com.xeenaa.villagermanager.client.gui;

import com.xeenaa.villagermanager.XeenaaVillagerManager;
import net.minecraft.entity.passive.VillagerEntity;

/**
 * Concrete implementation of the tabbed villager management screen.
 *
 * <p>This screen provides a tabbed interface for managing various aspects of
 * villagers including professions, equipment (for Guards), and future features.</p>
 *
 * @since 1.0.0
 * @author Xeenaa Villager Manager Team
 */
public class VillagerManagementScreen extends TabbedManagementScreen {

    /**
     * Creates a new villager management screen for the specified villager.
     *
     * @param villager the target villager to manage
     */
    public VillagerManagementScreen(VillagerEntity villager) {
        super(villager);
    }

    @Override
    protected void initializeTabs() {
        super.initializeTabs();

        // Always add profession tab (available for all non-baby villagers)
        ProfessionTab professionTab = new ProfessionTab(targetVillager);
        if (professionTab.isAvailable()) {
            addTab(professionTab);
            XeenaaVillagerManager.LOGGER.debug("Added profession tab for villager {}",
                targetVillager.getId());
        }

        // Equipment tab removed - will be replaced with rank tab in ranking system

        // TODO: Add future tabs here
        // - Trade management tab
        // - Villager statistics tab
        // - Behavior configuration tab

        XeenaaVillagerManager.LOGGER.info("Initialized VillagerManagementScreen with {} tabs for villager {}",
            getTabs().size(), targetVillager.getId());
    }

    /**
     * Gets the list of tabs for testing purposes.
     *
     * @return the list of tabs
     */
    public java.util.List<Tab> getTabs() {
        return java.util.List.copyOf(tabs);
    }
}