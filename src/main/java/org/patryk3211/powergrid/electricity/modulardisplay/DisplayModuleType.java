package org.patryk3211.powergrid.electricity.modulardisplay;
import net.minecraft.util.ByIdMap;
import org.patryk3211.powergrid.utility.Lang;

import java.util.function.IntFunction;

public enum DisplayModuleType {
    ZERO_TO_NINE(0, Lang.translateDirect("gui.modular_display.0 - 9").getString(), "zerotonine", 80f, 9f),
    NINE_TO_ZERO(1, Lang.translateDirect("gui.modular_display.9 - 0").getString(), "ninetozero", 80f, 9f),
    ONE_TO_ZERO(2, Lang.translateDirect("gui.modular_display.1 - 0").getString(), "onetozero", 80f, 9f),
    HEXADECIMAL(3, Lang.translateDirect("gui.modular_display.hexadecimal").getString(), "zerotof", 112f, 15f),
    SYMBOLS(4, Lang.translateDirect("gui.modular_display.symbols").getString(), "symbols", 80f, 8f),
    ALPHABET(5, Lang.translateDirect("gui.modular_display.alphabet").getString(), "alphabet", 176f, 25f);

    private static final IntFunction<DisplayModuleType> BY_ID = ByIdMap.continuous(DisplayModuleType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    private final int id;

    private final String name;
    private final String displayTexture;
    private final float spriteWidth;
    private final float characterCount;

    DisplayModuleType(int id, String name, String displayTexture, float spriteWidth, float characterCount) {
        this.id = id;
        this.name = name;
        this.displayTexture = displayTexture;
        this.spriteWidth = spriteWidth;
        this.characterCount = characterCount;
    }

    public int getId() {
        return this.id;
    }

    public String getDisplayTexture() {
        return this.displayTexture;
    }

    public float getSpriteWidth() {
        return this.spriteWidth;
    }

    public float getCharacterCount() {
        return this.characterCount;
    }

    public static DisplayModuleType byId(int moduleId) {
        return BY_ID.apply(moduleId);
    }

    public String getTranslationKey() {
        return name;
    }

}