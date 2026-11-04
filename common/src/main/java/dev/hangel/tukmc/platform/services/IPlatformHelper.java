package dev.hangel.tukmc.platform.services;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** What the common code needs from NeoForge or Fabric. */
public interface IPlatformHelper {

    /** "NeoForge" or "Fabric". */
    String getPlatformName();

    Path getGameDir();

    /** Writes the in-memory settings (Config nodes, colors, chat flags) to the config file. */
    void saveConfig();

    /** Puts the saved colors back (leaving the color screen without saving). */
    void loadColors();

    /** The key a key mapping is currently bound to. */
    InputConstants.Key getBoundKey(KeyMapping mapping);

    /** Food values of an item, null if it isn't food. */
    FoodProperties getFoodProperties(ItemStack stack, Player player);

    /** The item you get by picking a block (middle click). */
    ItemStack getPickedBlock(BlockState state, BlockHitResult hit, Level level, BlockPos pos, Player player);

    /** Name style of an item's rarity. */
    UnaryOperator<Style> getRarityStyle(ItemStack stack);
}
