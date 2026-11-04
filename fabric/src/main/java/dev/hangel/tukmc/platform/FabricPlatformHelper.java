package dev.hangel.tukmc.platform;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

import com.mojang.blaze3d.platform.InputConstants;

import dev.hangel.tukmc.platform.services.IPlatformHelper;
import dev.hangel.tukmc.util.FabricConfigFile;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void saveConfig() {
        FabricConfigFile.save();
    }

    @Override
    public void loadColors() {
        FabricConfigFile.loadColors();
    }

    @Override
    public InputConstants.Key getBoundKey(KeyMapping mapping) {
        return KeyBindingHelper.getBoundKeyOf(mapping);
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, Player player) {
        return stack.get(DataComponents.FOOD);
    }

    @Override
    public ItemStack getPickedBlock(BlockState state, BlockHitResult hit, Level level, BlockPos pos, Player player) {
        return state.getBlock().getCloneItemStack(level, pos, state);
    }

    @Override
    public UnaryOperator<Style> getRarityStyle(ItemStack stack) {
        return style -> style.withColor(stack.getRarity().color());
    }
}
