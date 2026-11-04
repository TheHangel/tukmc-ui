package dev.hangel.tukmc.platform;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

import com.mojang.blaze3d.platform.InputConstants;

import dev.hangel.tukmc.platform.services.IPlatformHelper;
import dev.hangel.tukmc.util.NeoForgeConfigFile;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.loading.FMLPaths;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public void saveConfig() {
        NeoForgeConfigFile.save();
    }

    @Override
    public void loadColors() {
        NeoForgeConfigFile.loadColors();
    }

    @Override
    public InputConstants.Key getBoundKey(KeyMapping mapping) {
        return mapping.getKey();
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, Player player) {
        return stack.getFoodProperties(player);
    }

    @Override
    public ItemStack getPickedBlock(BlockState state, BlockHitResult hit, Level level, BlockPos pos, Player player) {
        return state.getCloneItemStack(hit, level, pos, player);
    }

    @Override
    public UnaryOperator<Style> getRarityStyle(ItemStack stack) {
        return stack.getRarity().getStyleModifier();
    }
}
