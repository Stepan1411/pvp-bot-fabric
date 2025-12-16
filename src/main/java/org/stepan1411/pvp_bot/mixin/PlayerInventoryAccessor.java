package org.stepan1411.pvp_bot.mixin;

import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInventory.class)
public interface PlayerInventoryAccessor {
    
    @Accessor("selectedSlot")
    void setSelectedSlot(int slot);
    
    @Accessor("selectedSlot")
    int getSelectedSlot();
}
