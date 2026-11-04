package dev.hangel.tukmc.handlers;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;

/** Key bindings, registered by each loader (RegisterKeyMappingsEvent on NeoForge, KeyBindingHelper on Fabric). */
public class KeyRegister {

    public static final String CATEGORY = "key.categories.tukmc";

    public static final KeyMapping showTooltipKB = new KeyMapping("key.tukmc.tooltip", GLFW.GLFW_KEY_DELETE, CATEGORY);
    // "TukMC Open Config" (K) is replaced by the Config button of the mod list (NeoForge mod list, Mod Menu on Fabric).
    // "TukMC Chat Command" (/) is now a vanilla key binding: the chat screen it opens is swapped for ours.
}
