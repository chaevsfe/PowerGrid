package org.patryk3211.powergrid.compat.sable;

import org.patryk3211.powergrid.compat.sable.SubLevelAccess;
import net.minecraft.world.entity.Entity;

public interface SableProxy {
    void init();

    void setSubLevelTracking(Entity entity, SubLevelAccess subLevel);
}
