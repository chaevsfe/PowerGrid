package org.patryk3211.powergrid.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.filefix.FileFixerUpper;
import org.patryk3211.powergrid.datafix.V4539;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DataFixers.class, priority = 900)
public class DataFixersMixin {
    @Inject(
            method = "addFixers(Lcom/mojang/datafixers/DataFixerBuilder;Lnet/minecraft/util/filefix/FileFixerUpper$Builder;)V",
            at = @At(value = "CONSTANT", args = "intValue=4541")
    )
    private static void powerGrid$addSchema(DataFixerBuilder builder, FileFixerUpper.Builder fileFixer, CallbackInfo ci) {
        V4539.register(builder);
    }
}
