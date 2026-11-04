package dev.hangel.tukmc.client;

import static dev.hangel.tukmc.client.TukDraw.drawDoubleOutlinedBox;
import static dev.hangel.tukmc.client.TukDraw.drawHalfString;
import static dev.hangel.tukmc.client.TukDraw.drawOutlinedBox;
import static dev.hangel.tukmc.client.TukDraw.drawSolidGradientRect;
import static dev.hangel.tukmc.client.TukDraw.drawSolidRect;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.gui.McMMOIntegration;
import dev.hangel.tukmc.gui.McMMOIntegration.LevelUpData;
import dev.hangel.tukmc.gui.McMMOIntegration.SkillData;
import dev.hangel.tukmc.gui.McMMOIntegration.SkillData.UsageType;
import dev.hangel.tukmc.handlers.KeyRegister;
import dev.hangel.tukmc.platform.Services;
import dev.hangel.tukmc.util.ColorCode;
import dev.hangel.tukmc.util.Config;
import dev.hangel.tukmc.util.FormattingCode;
import dev.hangel.tukmc.util.TukMCReference;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.tags.FluidTags;

/**
 * Port of the original TukMC 1.6.4 GuiIngame. In 1.6.4 the mod replaced the whole in-game GUI class; now each vanilla HUD
 * element hands over to the methods below (GUI layers on NeoForge, Gui mixins on Fabric).
 */
public class TukHud {

    public static final TukHud INSTANCE = new TukHud();

    private static final ResourceLocation NOTE = ResourceLocation.withDefaultNamespace("textures/particle/note.png");

    final Minecraft mc = Minecraft.getInstance();

    private int tooltipOpenFor;
    private long rendersElapsed = 0;
    private String recordPlaying = "";
    private boolean recordIsPlaying = false;
    private int lastItem = 0;
    private int tooltipSize = 0;
    private int lastHealth = 0;
    private int lastFood = 0;
    private int lastXP = 0;
    private int entityHealth = 0;
    private float entityHealthNT;
    public static boolean reset = false;
    public static boolean entityReset = false;

    private TukHud() {
    }

    // ------------------------------------------------------------------ hooks
    // The hotbar and status bars are wired per loader (TukHudLayers on NeoForge, TukHudFabric on Fabric); the pieces
    // below follow the same rules on both: @return true when TukMC drew the element and vanilla must be skipped.

    /** Gui#renderEffects: @return true if TukMC drew the buffs (vanilla is skipped). */
    public boolean renderEffects(GuiGraphics g) {
        if (!Config.get(Config.NODE_BUFFS))
            return false;
        if (visible())
            drawBuffs(g);
        return true;
    }

    /** Our extra overlay, right after the effects (where the NeoForge "tukmc:hud" layer sits). */
    public void renderOverlayLayer(GuiGraphics g, DeltaTracker dt) {
        if (visible())
            renderTukOverlay(g, dt);
    }

    public void renderAdornments(GuiGraphics g) {
        if (visible())
            drawGenericStuff(g);
    }

    /** BossHealthOverlay#render: @return true if TukMC drew the boss bars. */
    public boolean renderBossBar(GuiGraphics g) {
        if (!Config.get(Config.NODE_BOSS_BAR))
            return false;
        if (visible())
            drawBossBar(g);
        return true;
    }

    /** Gui#renderOverlayMessage: @return true if it was a "Now Playing" handled by TukMC. */
    public boolean renderOverlayMessage(GuiGraphics g, DeltaTracker dt) {
        return drawRecordDisplay(g, dt);
    }

    public void renderScoreboard(GuiGraphics g) {
        if (visible())
            drawScoreboardSidebar(g);
    }

    public void renderTabList(GuiGraphics g) {
        if (visible())
            renderPlayerList(g);
    }

    /** Gui#renderChat: @return true if TukMC handles the chat (vanilla is skipped). */
    public boolean renderChat(GuiGraphics g) {
        if (Config.get(Config.NODE_DEFAULT_CHAT))
            return false;
        if (visible())
            TukChatOverlay.render(g);
        return true;
    }

    boolean visible() {
        return !mc.options.hideGui && mc.player != null && mc.level != null;
    }

    /** Vanilla health/food/armor/air only render while the player can be hurt; keep that rule for our replacements. */
    boolean canHurt() {
        return visible() && mc.gameMode.canHurtPlayer();
    }

    // ------------------------------------------------------------------ ticking

    public void tick() {
        if (tooltipOpenFor > 0) {
            --tooltipOpenFor;
        }
        if (mc.player != null && mc.player.getInventory().selected != lastItem) {
            tooltipOpenFor = 30;
            lastItem = mc.player.getInventory().selected;
        }
    }

    // ------------------------------------------------------------------ main overlay (everything vanilla doesn't have)

    private void renderTukOverlay(GuiGraphics g, DeltaTracker dt) {
        ++rendersElapsed;
        smoothBars();

        Font fr = mc.font;
        int width = g.guiWidth();
        int height = g.guiHeight();

        drawLeftBar(g, fr, width, height);
        drawRightBar(g, fr, width, height);

        if (Config.get(Config.NODE_DANGER_DISPLAY) || Config.get(Config.NODE_TOP_BAR)) {
            BlockPos pos = mc.player.blockPosition();
            String biomeName = getBiomeName(pos);
            drawTopBar(g, fr, width, biomeName);
            drawDangerZone(g, fr, pos);
        }

        drawMCMMO(g, fr, width);

        if (mc.screen == null || mc.screen instanceof ChatScreen) {
            drawBlockAtPointer(g, fr, width);
        }

        drawArrowCount(g, fr, width, height);
        drawStatsBoard(g, fr, height);
        drawTooltip(g, fr, width, height);
    }

    private String getBiomeName(BlockPos pos) {
        Holder<Biome> biome = mc.level.getBiome(pos);
        return biome.unwrapKey().map(ResourceKey::location)
                .map(rl -> Component.translatable("biome." + rl.getNamespace() + "." + rl.getPath()).getString())
                .orElse(I18n.get("tukmc.hud.unknown"));
    }

    // ------------------------------------------------------------------ hotbar

    void renderHotbar(GuiGraphics g, DeltaTracker dt) {
        int width = g.guiWidth();
        int height = g.guiHeight();
        LocalPlayer player = mc.player;
        if (player.isSpectator())
            return;

        if (Config.get(Config.NODE_ITEMS_BACKGROUND)) {
            drawDoubleOutlinedBox(g, width / 2 - 90, height - 22, 180, 20, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
        }

        Inventory inv = player.getInventory();
        for (int i = 0; i < 9; ++i) {
            int i1 = width / 2 - 88 + i * 20;
            int i2 = height - 20;
            boolean isHighlight = inv.selected == i;
            ItemStack stack = inv.items.get(i);
            drawSlotBox(g, i1, i2, stack, isHighlight);
        }

        // 1.6.4 had no off-hand; it gets its own TukMC slot left of the bar when something is held in it.
        ItemStack offhand = player.getOffhandItem();
        int offX = width / 2 - 88 - 26;
        if (!offhand.isEmpty()) {
            drawSlotBox(g, offX, height - 20, offhand, false);
        }

        float partial = dt.getGameTimeDeltaPartialTick(false);
        for (int i = 0; i < 9; ++i) {
            renderSlot(g, inv.items.get(i), width / 2 - 88 + i * 20, height - 20, partial, i + 1, i == inv.selected);
        }
        if (!offhand.isEmpty()) {
            renderSlot(g, offhand, offX, height - 20, partial, 10, false);
        }
    }

    private void drawSlotBox(GuiGraphics g, int x, int y, ItemStack stack, boolean isHighlight) {
        if (!stack.isEmpty()) {
            drawDoubleOutlinedBox(g, x, y, 16, 16,
                    isHighlight ? TukMCReference.BOX_HIGHLIGHT_COLOR : TukMCReference.BOX_INNER_COLOR,
                    stack.hasFoil() && !isHighlight ? TukMCReference.BOX_EFFECT_OUTLINE_COLOR
                            : TukMCReference.BOX_OUTLINE_COLOR);
        } else if (isHighlight) {
            if (!Config.get(Config.NODE_ITEMS_BACKGROUND)) {
                drawDoubleOutlinedBox(g, x + 2, y + 2, 12, 12, TukMCReference.BOX_INNER_COLOR,
                        TukMCReference.BOX_OUTLINE_COLOR, TukMCReference.BOX_HIGHLIGHT_COLOR);
            } else {
                drawDoubleOutlinedBox(g, x + 1, y + 1, 14, 14, TukMCReference.BOX_HIGHLIGHT_COLOR,
                        TukMCReference.BOX_HIGHLIGHT_COLOR);
            }
        }
    }

    private void renderSlot(GuiGraphics g, ItemStack stack, int x, int y, float partial, int seed, boolean selected) {
        if (stack.isEmpty())
            return;
        float f1 = stack.getPopTime() - partial;
        g.pose().pushPose();
        if (f1 > 0.0F) {
            float f2 = 1.0F + f1 / 5.0F;
            g.pose().translate(x + 8, y + 12, 0.0F);
            g.pose().scale(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
            g.pose().translate(-(x + 8), -(y + 12), 0.0F);
        }
        g.renderItem(mc.player, stack, x, y, seed);
        g.pose().popPose();

        renderSlotText(g, mc.font, stack, x, y);
    }

    /** ModCompat.renderNormalSlots: durability %/number, unbreaking level and stack size, all at half scale. */
    public static void renderSlotText(GuiGraphics g, Font font, ItemStack stack, int x, int y) {
        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        if (stack.isDamageableItem()) {
            int dmg = stack.getDamageValue();
            String dmgStr;
            if (Config.get(Config.NODE_NUMERICAL_DAMAGE_DISPLAY)) {
                dmgStr = (stack.getMaxDamage() - dmg + 1) + "";
            } else {
                dmgStr = (dmg == 0 ? 100 : Math.max(1, (stack.getMaxDamage() - dmg) * 100 / stack.getMaxDamage()))
                        + "%";
            }
            int unbreakLvl = getUnbreaking(stack);
            drawHalfString(g, font, dmgStr, (x + 16 - font.width(dmgStr) / 2) * 2, (y + 11) * 2,
                    durabilityColor(stack), true);
            if (unbreakLvl > 0) {
                drawHalfString(g, font, ColorCode.PINK + "" + unbreakLvl, (x + 1) * 2, (y + 1) * 2, 0xFFFFFF, true);
            }
        } else if (stack.getCount() > 1) {
            String s1 = String.valueOf(stack.getCount());
            drawHalfString(g, font, s1, (x + 16 - font.width(s1) / 2) * 2, (y + 11) * 2, 0xFFFFFF, true);
        }
        g.pose().popPose();
    }

    public static int durabilityColor(ItemStack stack) {
        if (Config.get(Config.NODE_COLORBLIND_MODE))
            return 0xFFFFFF;
        int color = (int) Math.round(255.0D - stack.getDamageValue() * 255.0D / stack.getMaxDamage());
        color = Mth.clamp(color, 0, 255);
        return 255 - color << 16 | color << 8;
    }

    private static int getUnbreaking(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return 0;
        try {
            Holder<Enchantment> unbreaking = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.UNBREAKING);
            return EnchantmentHelper.getItemEnchantmentLevel(unbreaking, stack);
        } catch (Exception e) {
            return 0;
        }
    }

    // ------------------------------------------------------------------ bars

    private boolean hasPotion(Holder<MobEffect> pot) {
        return mc.player.hasEffect(pot);
    }

    private void smoothBars() {
        LocalPlayer player = mc.player;
        int health;
        int eHealth = 0;
        double eMaxHealth = 0;

        Entity e = player.getVehicle();
        LivingEntity entity = null;

        if (e instanceof LivingEntity living) {
            entity = living;
            entityHealthNT = entity.getHealth();
            eMaxHealth = entity.getMaxHealth();
        }

        if (e == null) {
            entityHealth = 0;
            entityHealthNT = 0;
        }

        double maxHealth = player.getMaxHealth();
        int scale = !Config.get(Config.NODE_ALT_STATUS) ? 180 : 80;
        health = (int) Math.round(player.getHealth() / maxHealth * scale);
        if (entity != null)
            eHealth = (int) Math.round(entity.getHealth() / eMaxHealth * scale);

        int food = player.getFoodData().getFoodLevel() * 4;
        int xp = (int) (player.experienceProgress * (!Config.get(Config.NODE_ALT_STATUS) ? 80 : 10));
        boolean smooth = Config.get(Config.NODE_SMOOTH_TRANSITION);

        if (entityHealth == 0 || !smooth || entityReset) {
            entityHealth = eHealth;
        } else if (entityHealth > eHealth) {
            --entityHealth;
        } else if (entityHealth < eHealth) {
            entityHealth++;
        }

        if (lastFood == 0 || !smooth) {
            lastFood = food;
        } else if (lastFood > food) {
            --lastFood;
        } else if (lastFood < food) {
            lastFood++;
        }

        if (lastHealth == 0 || !smooth || reset) {
            lastHealth = health;
            reset = false;
        } else if (lastHealth > health) {
            --lastHealth;
        } else if (lastHealth < health) {
            lastHealth++;
        }

        // The original ran the health step twice per frame.
        if (lastHealth == 0 || !smooth) {
            lastHealth = health;
        } else if (lastHealth > health) {
            --lastHealth;
        } else if (lastHealth < health) {
            lastHealth++;
        }

        if (lastXP == 0 || !smooth) {
            lastXP = xp;
        } else if (lastXP > xp) {
            --lastXP;
        } else if (lastXP < xp) {
            lastXP++;
        }
    }

    void renderHealth(GuiGraphics g) {
        int width = g.guiWidth();
        int height = g.guiHeight();
        LocalPlayer p = mc.player;
        int healthBottom = hasPotion(MobEffects.REGENERATION) ? 0xd82424 : 0;
        if (hasPotion(MobEffects.HEALTH_BOOST))
            healthBottom = 0xFF4605;
        if (hasPotion(MobEffects.POISON))
            healthBottom = 0x375d12;
        int healthTop = hasPotion(MobEffects.WITHER) ? TukMCReference.BOX_INNER_COLOR : 0x901414;
        int hp = Math.round(p.getHealth());
        String absorb = FormattingCode.RESET + "+" + Math.round(p.getAbsorptionAmount());
        String text = (hp < 5 ? ColorCode.RED : "") + "" + hp + (p.getAbsorptionAmount() > 0 ? absorb : "");
        String mountText = entityHealthNT > 0 ? first4(entityHealthNT + "") : null;

        if (!Config.get(Config.NODE_ALT_STATUS)) {
            drawDoubleOutlinedBox(g, width / 2 - 90, height - 42, 180, 10, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            if (entityHealth > 0)
                drawDoubleOutlinedBox(g, width / 2 - 90, height - 51, 180, 6, TukMCReference.BOX_INNER_COLOR,
                        TukMCReference.BOX_OUTLINE_COLOR);
            drawSolidGradientRect(g, width / 2 - 90, height - 51, entityHealth, 6, 0x5900B7, 0xBA00B7);
            drawSolidGradientRect(g, width / 2 - 90, height - 42, lastHealth, 10, healthBottom, healthTop);
            if (!hasPotion(MobEffects.WITHER))
                drawHalfString(g, mc.font, text, width + 180 - mc.font.width(text), height * 2 - 84, 0xFFFFFF, false);
            if (mountText != null)
                drawHalfString(g, mc.font, mountText, width + 180 - mc.font.width(mountText), height * 2 - 102,
                        0xFFFFFF, false);
        } else {
            drawDoubleOutlinedBox(g, width / 2 - 90, height - 36, 80, 11, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            if (entityHealth > 0)
                drawDoubleOutlinedBox(g, width / 2 - 90, height - 45, 180, 6, TukMCReference.BOX_INNER_COLOR,
                        TukMCReference.BOX_OUTLINE_COLOR);
            drawSolidGradientRect(g, width / 2 - 90, height - 45, entityHealth, 6, 0x5900B7, 0xBA00B7);
            drawSolidGradientRect(g, width / 2 - 90, height - 36, lastHealth, 11, healthBottom, healthTop);
            if (!hasPotion(MobEffects.WITHER))
                drawHalfString(g, mc.font, text, width - 20 - mc.font.width(text), height * 2 - 72, 0xFFFFFF, false);
            if (mountText != null)
                drawHalfString(g, mc.font, mountText, width + 180 - mc.font.width(mountText), height * 2 - 90,
                        0xFFFFFF, false);
        }
    }

    private static String first4(String s) {
        return s.substring(0, Math.min(4, s.length()));
    }

    void renderFood(GuiGraphics g) {
        int width = g.guiWidth();
        int height = g.guiHeight();
        boolean alt = Config.get(Config.NODE_ALT_STATUS);
        int bx = alt ? width / 2 + 10 : width / 2 - 90;
        int by = alt ? height - 36 : height - 29;
        int bh = alt ? 11 : 4;

        drawDoubleOutlinedBox(g, bx, by, 80, bh, TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
        int food = mc.player.getFoodData().getFoodLevel();
        int foodHeal = 0;
        boolean overkill = false;
        if (food != 20) {
            int barWidth = 0;
            ItemStack stack = mc.player.getMainHandItem();
            FoodProperties props = stack.isEmpty() ? null : Services.PLATFORM.getFoodProperties(stack, mc.player);
            if (props != null) {
                foodHeal = props.nutrition();
                barWidth = Math.min(20, food + foodHeal);
                if (food + foodHeal > 20) {
                    overkill = true;
                }
            }
            if (barWidth > 0 && Config.get(Config.NODE_FOOD_PREDICT)) {
                drawSolidGradientRect(g, bx, by, barWidth * 4, bh, overkill ? 0 : 0xd82424, 0x901414);
            }
        }
        boolean hunger = hasPotion(MobEffects.HUNGER);
        drawSolidGradientRect(g, bx, by, lastFood, bh, hunger ? 0x0c1702 : 0x6a410b, hunger ? 0x1d3208 : 0x8e5409);
        String heal = I18n.get("tukmc.hud.will_heal", foodHeal)
                + (overkill ? I18n.get("tukmc.hud.waste", food + foodHeal - 20) : "");
        String foodStr = (food < 5 ? ColorCode.RED : "") + "" + food;
        if (!alt) {
            if (foodHeal > 0 && Config.get(Config.NODE_FOOD_PREDICT))
                drawHalfString(g, mc.font, heal, width - 178, height * 2 - 60, 0xFFFFFF, false);
            drawHalfString(g, mc.font, foodStr, width - 33, height * 2 - 58, 0xFFFFFF, true);
        } else {
            if (foodHeal > 0 && Config.get(Config.NODE_FOOD_PREDICT))
                drawHalfString(g, mc.font, heal, width + 50, height * 2 - 64, 0xFFFFFF, false);
            drawHalfString(g, mc.font, foodStr, width + 168, height * 2 - 72, 0xFFFFFF, true);
        }
    }

    void renderAir(GuiGraphics g) {
        int width = g.guiWidth();
        int height = g.guiHeight();
        if (mc.player.isEyeInFluid(FluidTags.WATER)) {
            int record = recordIsPlaying ? 20 : 5 + tooltipSize;
            int air = mc.player.getAirSupply() + 20;
            drawDoubleOutlinedBox(g, width / 2 - 80, height - 60 - record, 160, 5, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            drawSolidGradientRect(g, width / 2 - 80, height - 60 - record, air / 2, 5, air < 60 ? 0xff1818 : 0x18cbff,
                    air < 60 ? 0xff8c8c : 0x8ce5ff);
            String airStr = I18n.get("tukmc.hud.air");
            int offset = (int) (air >= 60 ? 0 : Math.sin(rendersElapsed) * 10);
            g.drawString(mc.font, airStr, width / 2 - mc.font.width(airStr) / 2 + offset, height - 72 - record,
                    0xFFFFFF, true);
        }
    }

    void renderExperience(GuiGraphics g) {
        int width = g.guiWidth();
        int height = g.guiHeight();
        LocalPlayer p = mc.player;
        int lvl = p.experienceLevel;
        String lvlStr = ColorCode.BRIGHT_GREEN + "" + lvl;
        if (!Config.get(Config.NODE_ALT_STATUS)) {
            if (lvl > 0) {
                drawDoubleOutlinedBox(g, width / 2 - mc.font.width(lvlStr) / 2 - 1, height - 32,
                        mc.font.width(lvlStr) + 2, 10, TukMCReference.BOX_INNER_COLOR,
                        TukMCReference.BOX_OUTLINE_COLOR);
                g.drawString(mc.font, lvlStr, width / 2 - mc.font.width(lvlStr) / 2, height - 31, 0xFFFFFF, true);
            }
            drawDoubleOutlinedBox(g, width / 2 + 10, height - 29, 80, 4, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            drawSolidGradientRect(g, width / 2 + 10, height - 29, lastXP, 4, 0x05d714, 0x8fea96);
            int relativeXP = (int) Math.floor(p.experienceProgress * p.getXpNeededForNextLevel());
            String lvlXP = ColorCode.BRIGHT_GREEN + "" + relativeXP + "/" + p.getXpNeededForNextLevel();
            drawHalfString(g, mc.font, lvlXP, width + 120 - mc.font.width(lvlXP), height * 2 - 58, 0xFFFFFF, true);
        } else {
            drawDoubleOutlinedBox(g, width / 2 - 5, height - 36, 10, 10, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            g.drawString(mc.font, lvlStr, width / 2 - mc.font.width(lvlStr) / 2, height - 36, 0xFFFFFF, false);
            drawDoubleOutlinedBox(g, width / 2 - 5, height - 27, 10, 2, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            drawSolidGradientRect(g, width / 2 - 5, height - 27, lastXP, 2, 0x05d714, 0x8fea96);
        }
    }

    void renderJumpBar(GuiGraphics g) {
        float charge = mc.player.getJumpRidingScale();
        int barHeight = 52;
        int filled = (int) (charge * (barHeight + 1));
        int x = g.guiWidth() / 2 + 10;
        int top = g.guiHeight() / 2 - barHeight / 2;
        int bottom = g.guiHeight() / 2 + barHeight / 2;
        drawDoubleOutlinedBox(g, x, top, 2, 52, TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
        if (filled > 0) {
            drawSolidGradientRect(g, x, bottom - filled, 2, filled, 0xFF0000, 0x55FF00);
        }
    }

    // ------------------------------------------------------------------ buffs

    private void drawBuffs(GuiGraphics g) {
        int width = g.guiWidth();
        int height = g.guiHeight();
        Font fr = mc.font;
        Collection<MobEffectInstance> potions = mc.player.getActiveEffects();
        int xPotOffset = 0;
        int yPotOffset = 0;
        int itr = 0;
        float tickrate = mc.level.tickRateManager().tickrate();
        for (MobEffectInstance effect : potions) {
            if (itr % 8 == 0) {
                xPotOffset = 0;
                yPotOffset += 1;
            }
            String effectStr = MobEffectUtil.formatDuration(effect, 1.0F, tickrate).getString();
            int bx = width - 30 - xPotOffset * 21;
            drawDoubleOutlinedBox(g, bx, height - 9 - yPotOffset * 28, fr.width(effectStr) / 2 + 2, 8,
                    TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
            drawDoubleOutlinedBox(g, bx, height - 26 - yPotOffset * 28, 18, 18, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            if (effect.showIcon()) {
                g.blit(bx, height - 26 - yPotOffset * 28, 0, 18, 18, mc.getMobEffectTextures().get(effect.getEffect()));
            }

            String level = Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)).getString();
            g.pose().pushPose();
            g.pose().translate(0, 0, 10);
            if (level.length() < 5 && !level.equals(Component.translatable("enchantment.level.1").getString())) {
                g.drawString(fr, level, width - 29 - xPotOffset * 21, height - 25 - yPotOffset * 28, 0xFFFFFF, true);
            }
            drawHalfString(g, fr, (effect.isAmbient() ? ColorCode.RED : "") + effectStr,
                    (width - 29 - xPotOffset * 21) * 2, (height - 6 - yPotOffset * 28) * 2, 0xFFFFFF, true);
            g.pose().popPose();
            ++itr;
            ++xPotOffset;
        }
    }

    // ------------------------------------------------------------------ boss bar

    private void drawBossBar(GuiGraphics g) {
        int width = g.guiWidth();
        Font fr = mc.font;
        int index = 0;
        for (LerpingBossEvent boss : mc.gui.bossOverlay.events.values()) {
            String bossName = boss.getName().getString();
            float healthScale = boss.getProgress();
            int yoffset = 15 + index * 30;
            int xoffset = 6;
            if (Config.get(Config.NODE_BOTTOM_ADORNMENTS)) {
                drawDoubleOutlinedBox(g, width / 2 - 126 + xoffset, 34 + yoffset, 5, 5, TukMCReference.BOX_INNER_COLOR,
                        TukMCReference.BOX_OUTLINE_COLOR);
                drawDoubleOutlinedBox(g, width / 2 + 121 + xoffset, 34 + yoffset, 5, 5, TukMCReference.BOX_INNER_COLOR,
                        TukMCReference.BOX_OUTLINE_COLOR);
                drawOutlinedBox(g, width / 2 - 119 + xoffset, 36 + yoffset, 238, 1, TukMCReference.BOX_OUTLINE_COLOR,
                        TukMCReference.BOX_INNER_COLOR);
                g.pose().pushPose();
                g.pose().scale(0.5F, 0.5F, 1F);
                drawSolidRect(g, width - 243 + xoffset, 71 + yoffset, width + 240, 75, TukMCReference.BOX_OUTLINE_COLOR);
                g.pose().popPose();
            }
            drawOutlinedBox(g, width / 2 - fr.width(bossName) / 2 - 3 + xoffset, 20 + yoffset,
                    fr.width(bossName) + 6 + xoffset, 14, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            drawDoubleOutlinedBox(g, width / 2 - 91 + xoffset, 31 + yoffset, 182 + xoffset, 10,
                    TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
            int renderHealth = (int) (healthScale * 182);
            drawSolidGradientRect(g, width / 2 - 91 + xoffset, 31 + yoffset, renderHealth + xoffset, 10, 0, 0x25092e);
            g.drawString(fr, ColorCode.PURPLE + bossName, width / 2 - fr.width(bossName) / 2 + xoffset + 3,
                    21 + yoffset, 0xFFFFFF, true);
            String hp = (healthScale < 0.1 ? ColorCode.RED : "") + "" + Math.round(healthScale * 100) + "%";
            if (!(healthScale < 0)) {
                g.drawString(fr, hp, width / 2 - fr.width(hp) / 2 + xoffset + 2, 32 + yoffset, 0xFFFFFF, true);
            }
            index++;
        }
    }

    // ------------------------------------------------------------------ record

    /** @return true when the overlay message was a "Now Playing" and TukMC drew it. */
    private boolean drawRecordDisplay(GuiGraphics g, DeltaTracker dt) {
        if (!visible() || !mc.gui.animateOverlayMessageColor || mc.gui.overlayMessageString == null
                || mc.gui.overlayMessageTime <= 0) {
            recordIsPlaying = false;
            return false;
        }
        recordIsPlaying = true;
        if (!Config.get(Config.NODE_MUSIC))
            return true;

        Component msg = mc.gui.overlayMessageString;
        recordPlaying = msg.getString();
        if (msg.getContents() instanceof TranslatableContents tc && tc.getArgs().length > 0) {
            Object arg = tc.getArgs()[0];
            recordPlaying = arg instanceof Component c ? c.getString() : String.valueOf(arg);
        }

        int width = g.guiWidth();
        int height = g.guiHeight();
        Font fr = mc.font;
        float color = mc.gui.overlayMessageTime - dt.getGameTimeDeltaPartialTick(false);
        int colorValue = (int) (color * 256.0F / 20.0F);
        if (colorValue > 0) {
            int colorRgb = Mth.hsvToRgb(color / 50.0F, 0.7F, 0.6F) & 0xFFFFFF;
            int length = fr.width(recordPlaying);
            drawDoubleOutlinedBox(g, width / 2 - length / 2 - 20, height - 70, length + 40, 20,
                    TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
            g.setColor(((colorRgb >> 16) & 0xFF) / 255F, ((colorRgb >> 8) & 0xFF) / 255F, (colorRgb & 0xFF) / 255F, 1F);
            g.blit(NOTE, width / 2 - length / 2 - 18, height - 68, 0, 0, 16, 16, 16, 16);
            g.blit(NOTE, width / 2 + length / 2, height - 68, 0, 0, 16, 16, 16, 16);
            g.setColor(1F, 1F, 1F, 1F);
            g.drawString(fr, recordPlaying, width / 2 - length / 2, height - 65, colorRgb, true);
        }
        return true;
    }

    // ------------------------------------------------------------------ left / right bars

    private void drawRightBar(GuiGraphics g, Font fr, int width, int height) {
        if (!Config.get(Config.NODE_RIGHT_BAR))
            return;
        int xoffset = 0;
        if (width - 183 <= width / 2 + 90) {
            xoffset = width - 183 - (width / 2 + 90);
        }
        drawDoubleOutlinedBox(g, width - 180 - xoffset, height - 20, 140, 16, TukMCReference.BOX_INNER_COLOR,
                TukMCReference.BOX_OUTLINE_COLOR);
        g.drawString(fr, I18n.get("tukmc.hud.fps", mc.getFps()), width - 176 - xoffset, height - 16, 0xFFFFFF, true);
        String right;
        if (!Config.get(Config.NODE_REPLACE_PING_WITH_TIME)) {
            right = mc.isSingleplayer() ? I18n.get("tukmc.hud.singleplayer")
                    : I18n.get(mc.isLocalServer() ? "tukmc.hud.ping.lan" : "tukmc.hud.ping.mp", getPing());
        } else {
            right = clock();
        }
        g.drawString(fr, right, width - 44 - fr.width(right) - xoffset, height - 16, 0xFFFFFF, true);
    }

    private String getPing() {
        PlayerInfo info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (info == null || info.getLatency() < 0)
            return I18n.get("tukmc.hud.na");
        return "" + info.getLatency();
    }

    private static String clock() {
        return new SimpleDateFormat(Config.get(Config.NODE_24HR_CLOCK) ? "HH:mm" : "h:mm a").format(new Date());
    }

    private void drawLeftBar(GuiGraphics g, Font fr, int width, int height) {
        if (!Config.get(Config.NODE_LEFT_BAR))
            return;
        int xoffset = 0;
        if (183 >= width / 2 - 90) {
            xoffset = 183 - (width / 2 - 90);
        }
        drawDoubleOutlinedBox(g, 40 - xoffset, height - 20, 140, 16, TukMCReference.BOX_INNER_COLOR,
                TukMCReference.BOX_OUTLINE_COLOR);
        int online = mc.getConnection() == null ? 1 : mc.getConnection().getOnlinePlayers().size();
        g.drawString(fr, I18n.get("tukmc.hud.online", online), 44 - xoffset, height - 16, 0xFFFFFF, true);
        LocalPlayer p = mc.player;
        String status = "";
        int fallDmg = Mth.ceil(p.fallDistance - 3.0F);
        if (p.isShiftKeyDown()) {
            status = I18n.get("tukmc.hud.sneaking");
        }
        if (p.isSprinting()) {
            status = I18n.get("tukmc.hud.sprinting");
        } else if (p.getFoodData().getFoodLevel() <= 6) {
            status = ColorCode.RED + I18n.get("tukmc.hud.cant_sprint");
        }
        if (p.getAbilities().flying) {
            status = I18n.get("tukmc.hud.flying");
        } else if (fallDmg > 0 && !p.getAbilities().instabuild) {
            status = I18n.get("tukmc.hud.falling", ColorCode.RED + "" + fallDmg);
        }
        String stat = status.equals("") ? p.getName().getString() : status;
        g.drawString(fr, stat, 176 - fr.width(stat) - xoffset, height - 16, 0xFFFFFF, true);
    }

    private void drawGenericStuff(GuiGraphics g) {
        if (!Config.get(Config.NODE_BOTTOM_ADORNMENTS))
            return;
        int width = g.guiWidth();
        int height = g.guiHeight();
        int in = TukMCReference.BOX_INNER_COLOR, out = TukMCReference.BOX_OUTLINE_COLOR;
        drawDoubleOutlinedBox(g, 6, height - 98, 5, 5, in, out);
        drawDoubleOutlinedBox(g, width - 10, height - 98, 5, 5, in, out);
        drawOutlinedBox(g, 50, height - 13, width - 100, 1, out, in);
        drawOutlinedBox(g, 8, height - 13, 40, 1, out, in);
        drawOutlinedBox(g, 8, height - 92, 1, 80, out, in);
        drawOutlinedBox(g, width - 48, height - 13, 40, 1, out, in);
        drawOutlinedBox(g, width - 8, height - 92, 1, 80, out, in);
        g.pose().pushPose();
        g.pose().scale(0.5F, 0.5F, 1F);
        drawSolidRect(g, 15, height * 2 - 27, 23, height * 2 - 23, out);
        drawSolidRect(g, width * 2 - 18, height * 2 - 27, width * 2 - 13, height * 2 - 23, out);
        drawSolidRect(g, 14, height * 2 - 186, 20, height * 2 - 185, out);
        drawSolidRect(g, width * 2 - 18, height * 2 - 186, width * 2 - 10, height * 2 - 185, out);
        g.pose().popPose();
    }

    // ------------------------------------------------------------------ top bar, armor, danger zone

    private void drawTopBar(GuiGraphics g, Font fr, int width, String biomeName) {
        int invSlots = 0;
        for (ItemStack element : mc.player.getInventory().items)
            if (element.isEmpty()) {
                ++invSlots;
            }

        int armoroffset = Config.get(Config.NODE_TOP_BAR) ? 0 : 15;

        if (Config.get(Config.NODE_STATUS_DISPLAY)
                && !(mc.options.keyPlayerList.isDown() && !mc.isSingleplayer())) {
            renderArmorBar(g, fr, width, armoroffset);
        }

        if (!Config.get(Config.NODE_TOP_BAR))
            return;

        String dir = I18n.get("tukmc.hud.unknown");
        int rot = 0;
        if (Config.get(Config.NODE_DIRECTION)) {
            rot = Math.abs(Math.round(mc.player.getYRot() % 360));
            if (rot >= 45 && rot < 135) {
                dir = I18n.get("tukmc.hud.west");
            } else if (rot >= 135 && rot < 225) {
                dir = I18n.get("tukmc.hud.north");
            } else if (rot >= 225 && rot < 315) {
                dir = I18n.get("tukmc.hud.east");
            } else {
                dir = I18n.get("tukmc.hud.south");
            }
        }
        if (Config.get(Config.NODE_DEGREES)) {
            dir = dir + " (" + rot + ")";
        }

        String topData = biomeName + (!Config.get(Config.NODE_REPLACE_PING_WITH_TIME) ? " | " + clock() : "")
                + (Config.get(Config.NODE_DIRECTION) ? " | " + dir : "")
                + (Config.get(Config.NODE_INV_SLOT) ? " | " + I18n.get("tukmc.hud.inventory", invSlots) : "");
        int size = fr.width(topData);

        if (Config.get(Config.NODE_CHEAT_COMPASSCLOCK)) {
            drawDoubleOutlinedBox(g, width / 2 - size / 2 - 24, -1, 18, 18, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            int clockX = size % 2 == 1 ? width / 2 + size / 2 + 5 : width / 2 + size / 2 + 4;
            drawDoubleOutlinedBox(g, clockX, -1, 18, 18, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            g.renderItem(new ItemStack(Items.CLOCK), clockX + 1, 0);
            g.renderItem(new ItemStack(Items.COMPASS), width / 2 - size / 2 - 23, 0);
        }

        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        drawDoubleOutlinedBox(g, width / 2 - size / 2 - 4, -1, size + 6, 15, TukMCReference.BOX_INNER_COLOR,
                TukMCReference.BOX_OUTLINE_COLOR);
        g.drawCenteredString(fr, topData, width / 2, 3, 0xFFFFFF);
        g.pose().popPose();
    }

    /** ModCompat.renderNormalTopBar: the four armor pieces hanging under the top bar. */
    private void renderArmorBar(GuiGraphics g, Font fr, int width, int yoffset) {
        List<ItemStack> armor = mc.player.getInventory().armor;
        int[] xs = { width / 2 + 26, width / 2 + 2, width / 2 - 22, width / 2 - 46 };
        for (int i = 0; i < 4; i++) {
            drawDoubleOutlinedBox(g, xs[i], -1, 18, 31 - yoffset, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
        }
        for (int i = 0; i < 4; i++) {
            ItemStack stack = armor.get(i);
            if (!stack.isEmpty()) {
                g.renderItem(stack, xs[i] + 1, 14 - yoffset);
                renderSlotText(g, fr, stack, xs[i] + 1, 14 - yoffset);
            }
        }
    }

    private void drawDangerZone(GuiGraphics g, Font fr, BlockPos pos) {
        if (Config.get(Config.NODE_DANGER_DISPLAY) && mc.level.getDifficulty() != Difficulty.PEACEFUL
                && isDarkEnoughToSpawn(pos)) {
            String light = (Config.get(Config.NODE_COLORBLIND_MODE) ? "" : ColorCode.RED) + I18n.get("tukmc.hud.danger");
            int lightLenght = fr.width(light);
            drawDoubleOutlinedBox(g, 39, 35, lightLenght + 20, 16, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
            g.renderItem(new ItemStack(Items.CREEPER_HEAD), 40, 35);
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            g.drawString(fr, light, 56, 39, 0xFFFFFF, true);
            g.pose().popPose();
        }
    }

    /**
     * Monster#isDarkEnoughToSpawn without its random parts: 1.6.4 warned at night under block light 7, since 1.18 monsters
     * need block light 0 (overworld) and a dark enough sky, day or night.
     */
    private boolean isDarkEnoughToSpawn(BlockPos pos) {
        DimensionType dim = mc.level.dimensionType();
        int blockLimit = dim.monsterSpawnBlockLightLimit();
        if (blockLimit < 15 && mc.level.getBrightness(LightLayer.BLOCK, pos) > blockLimit)
            return false;
        // the client never refreshes Level#skyDarken, so recompute it like Level#updateSkyBrightness
        double rain = 1.0 - mc.level.getRainLevel(1.0F) * 5.0F / 16.0;
        double thunder = 1.0 - mc.level.getThunderLevel(1.0F) * 5.0F / 16.0;
        double day = 0.5 + 2.0 * Mth.clamp(Mth.cos(mc.level.getTimeOfDay(1.0F) * (float) (Math.PI * 2)), -0.25, 0.25);
        int skyDarken = mc.level.isThundering() ? 10 : (int) ((1.0 - day * rain * thunder) * 11.0);
        return mc.level.getRawBrightness(pos, skyDarken) <= dim.monsterSpawnLightTest().getMaxValue();
    }

    // ------------------------------------------------------------------ block at pointer

    private void drawBlockAtPointer(GuiGraphics g, Font fr, int width) {
        if (!Config.get(Config.NODE_BLOCK_DISPLAY) || !(mc.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK)
            return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir())
            return;
        ItemStack stack;
        try {
            stack = Services.PLATFORM.getPickedBlock(state, hit, mc.level, pos, mc.player);
        } catch (Exception e) {
            stack = ItemStack.EMPTY;
        }
        if (stack.isEmpty())
            stack = new ItemStack(state.getBlock().asItem());

        MutableComponent itemname;
        if (stack.isEmpty()) {
            FluidState fluid = state.getFluidState();
            if (fluid.isEmpty())
                return;
            itemname = state.getBlock().getName().copy();
        } else {
            itemname = stack.getHoverName().copy().withStyle(Services.PLATFORM.getRarityStyle(stack));
        }
        if (Config.get(Config.NODE_BLOCK_DISPLAY_ID)) {
            itemname.append(Component.literal(" (" + BuiltInRegistries.BLOCK.getKey(state.getBlock()) + ")")
                    .withStyle(ChatFormatting.WHITE));
        }

        int nameWidth = fr.width(itemname);
        drawDoubleOutlinedBox(g, width - 50 - nameWidth, 34, 22 + nameWidth, 18, TukMCReference.BOX_INNER_COLOR,
                TukMCReference.BOX_OUTLINE_COLOR);
        g.drawString(fr, itemname, width - 30 - nameWidth, 39, 0xFFFFFF, false);
        if (!stack.isEmpty())
            g.renderItem(stack, width - 49 - nameWidth, 35);
    }

    // ------------------------------------------------------------------ arrows, mcMMO, stats, tooltip

    private void drawArrowCount(GuiGraphics g, Font fr, int width, int height) {
        ItemStack held = mc.player.getMainHandItem();
        if (!(held.getItem() instanceof ProjectileWeaponItem) || !Config.get(Config.NODE_SHOW_ARROWS)
                || mc.player.getAbilities().instabuild)
            return;
        int allArrows = 0;
        for (ItemStack stack : mc.player.getInventory().items)
            if (stack.getItem() instanceof ArrowItem) {
                allArrows += stack.getCount();
            }
        ItemStack off = mc.player.getOffhandItem();
        if (off.getItem() instanceof ArrowItem)
            allArrows += off.getCount();
        String arrowStr = (allArrows <= 8 && !Config.get(Config.NODE_COLORBLIND_MODE) ? ColorCode.RED : "")
                + I18n.get("tukmc.hud.arrows", allArrows);
        drawHalfString(g, fr, arrowStr, width - fr.width(arrowStr) / 2, height - 21, 0xFFFFFF, true);
    }

    private void drawMCMMO(GuiGraphics g, Font fr, int width) {
        if (!Config.get(Config.NODE_MCMMO))
            return;
        LevelUpData lvlData = McMMOIntegration.getActiveLevelUpData();
        if (lvlData != null) {
            String levelUp = ColorCode.RED + I18n.get("tukmc.hud.mcmmo.level_up");
            String skillLeveledUp = ColorCode.YELLOW + lvlData.getSkill() + ": [" + lvlData.getLevel() + "]";
            g.pose().pushPose();
            g.pose().scale(2F, 2F, 1F);
            g.drawString(fr, levelUp, width / 4 - fr.width(levelUp) / 2, 15, 0xFFFFFF, true);
            g.pose().popPose();
            g.drawString(fr, skillLeveledUp, width / 2 - fr.width(skillLeveledUp) / 2, 52, 0xFFFFFF, true);
        }

        int maxSize = 20;
        List<String> lines = new ArrayList<>();
        for (SkillData skillData : McMMOIntegration.skillData) {
            String s = (skillData.type == UsageType.READY
                    ? skillData.getTool().charAt(0) + skillData.getTool().substring(1).toLowerCase()
                    : skillData.getName()) + ": " + I18n.get(skillData.type.getName());
            lines.add(s);
            maxSize = Math.max(fr.width(s), maxSize);
        }
        if (!lines.isEmpty()) {
            drawDoubleOutlinedBox(g, 10, 58, maxSize + 4, lines.size() * 11 + 6, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);
        }
        int i = 0;
        for (String s : lines) {
            g.drawString(fr, s, 12, 60 + i * 12, 0xFFFFFF, true);
            ++i;
        }
    }

    public static int getMobKills() {
        LocalPlayer p = Minecraft.getInstance().player;
        return p == null ? 0 : p.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
    }

    public static int getPlayerKills() {
        LocalPlayer p = Minecraft.getInstance().player;
        return p == null ? 0 : p.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS));
    }

    private void drawStatsBoard(GuiGraphics g, Font fr, int height) {
        if (mc.screen instanceof ChatScreen || (mc.options.keyPlayerList.isDown() && !mc.isSingleplayer())
                || !Config.get(Config.NODE_STATBAR))
            return;
        String deathstat = I18n.get("tukmc.hud.deaths", TukMC.deaths);
        String mobkillstat = I18n.get("tukmc.hud.mob_kills", getMobKills() - TukMC.negativeMobKills);
        String pkillstat = I18n.get("tukmc.hud.player_kills", getPlayerKills() - TukMC.negativePKills);
        int max2 = Math.max(Math.max(fr.width(deathstat), fr.width(mobkillstat)), fr.width(pkillstat));
        drawDoubleOutlinedBox(g, -1, height / 2 - 22, max2 + 2, 33, TukMCReference.BOX_INNER_COLOR,
                TukMCReference.BOX_OUTLINE_COLOR);
        g.drawString(fr, deathstat, 1, height / 2 - 20, 0xFFFFFF, false);
        g.drawString(fr, mobkillstat, 1, height / 2 - 9, 0xFFFFFF, false);
        g.drawString(fr, pkillstat, 1, height / 2 + 2, 0xFFFFFF, false);
    }

    private void drawTooltip(GuiGraphics g, Font fr, int width, int height) {
        tooltipSize = 0;
        if (mc.screen instanceof ChatScreen || !(KeyRegister.showTooltipKB.isDown()
                || Config.get(Config.NODE_TOOLTIP_ALWAYS_ON) || Config.get(Config.NODE_TOOLTIPS) && tooltipOpenFor > 0))
            return;
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty())
            return;
        int loc = mc.player.getInventory().selected;
        int x = width / 2 - 88 + loc * 20;
        int y = height - 20;

        List<Component> tokensList = stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player,
                mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        if (tokensList.isEmpty())
            return;
        tooltipSize = tokensList.size() * 12 - 12;

        int lenght = 12;
        for (Component s : tokensList) {
            lenght = Math.max(lenght, fr.width(s));
        }

        if (Config.get(Config.NODE_TOOLTIPS) || Config.get(Config.NODE_TOOLTIP_ALWAYS_ON)) {
            if (!mc.player.isCreative()) {
                y = y - 20;
            }
            x = width / 2 - (lenght + 4) / 2;
        }

        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        drawDoubleOutlinedBox(g, x, y - tokensList.size() * 12 - 5, lenght + 4, tokensList.size() * 12,
                TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
        int i = 1;
        for (Component s : tokensList) {
            int ty = y - (tokensList.size() + 1) * 12 + i * 12 - 3;
            if (i == 1) {
                g.drawString(fr, s, x + 2, ty, 0xFFFFFF, true);
            } else {
                g.drawString(fr, Component.empty().withStyle(ChatFormatting.GRAY).append(s), x + 2, ty, 0xFFFFFF,
                        false);
            }
            ++i;
        }
        g.pose().popPose();
    }

    // ------------------------------------------------------------------ tab list & sidebar

    private static ResourceLocation pingSprite(int latency) {
        String s;
        if (latency < 0) {
            s = "icon/ping_unknown";
        } else if (latency < 150) {
            s = "icon/ping_5";
        } else if (latency < 300) {
            s = "icon/ping_4";
        } else if (latency < 600) {
            s = "icon/ping_3";
        } else if (latency < 1000) {
            s = "icon/ping_2";
        } else {
            s = "icon/ping_1";
        }
        return ResourceLocation.withDefaultNamespace(s);
    }

    private void renderPlayerList(GuiGraphics g) {
        if (mc.getConnection() == null)
            return;
        Collection<PlayerInfo> infos = mc.getConnection().getListedOnlinePlayers();
        if (!mc.options.keyPlayerList.isDown() || (mc.isLocalServer() && infos.size() <= 1))
            return;

        int width = g.guiWidth();
        Font fr = mc.font;
        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective scoreobjective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        List<PlayerInfo> list = new ArrayList<>(infos);
        list.sort(java.util.Comparator.comparing(i -> i.getProfile().getName(), String.CASE_INSENSITIVE_ORDER));

        ServerData sd = mc.getCurrentServer();
        if (sd != null) {
            String sdisp = sd.name + " - " + sd.ip;
            drawDoubleOutlinedBox(g, width / 2 - fr.width(sdisp) / 2 - 1, 21, fr.width(sdisp) + 2, 10,
                    TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
            g.drawString(fr, sdisp, width / 2 - fr.width(sdisp) / 2, 22, 0xFFFFFF, true);
        }

        int var13 = list.size();
        int var40 = var13;
        int var38;
        for (var38 = 1; var40 > 20; var40 = (var13 + var38 - 1) / var38) {
            ++var38;
        }
        int var16 = Math.min(150, 300 / var38);
        int var17 = (width - var38 * var16) / 2;
        int var44 = 22;
        drawDoubleOutlinedBox(g, var17 - 2, var44 - 2 + 15, var16 * var38 + 3, 9 * var40 + 3,
                TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);

        for (int var19 = 0; var19 < var13; ++var19) {
            int var20 = var17 + var19 % var38 * var16;
            int var47 = var44 + var19 / var38 * 9;
            drawOutlinedBox(g, var20, var47 + 15, var16 - 1, 8, TukMCReference.BOX_INNER_COLOR,
                    TukMCReference.BOX_OUTLINE_COLOR);

            PlayerInfo info = list.get(var19);
            String playerName = info.getProfile().getName();
            PlayerTeam team = scoreboard.getPlayersTeam(playerName);
            Component nameComp = info.getTabListDisplayName() != null ? info.getTabListDisplayName()
                    : PlayerTeam.formatNameForTeam(team, Component.literal(playerName));
            String name = nameComp.getString();
            MutableComponent display = nameComp.copy();

            var other = mc.level.getPlayerByUUID(info.getProfile().getId());
            if (other != null && other != mc.player) {
                int dist = Math.round(mc.player.distanceTo(other));
                if (dist != 0) {
                    display.append(Component.literal(" - " + dist + "m").withStyle(ChatFormatting.WHITE));
                    name = name + " - " + dist + "m";
                }
            }

            if (scoreobjective != null) {
                int j3 = var20 + fr.width(name) + 5;
                int l3 = var20 + var16 - 12 - 5;
                if (l3 - j3 > 5) {
                    ReadOnlyScoreInfo score = scoreboard
                            .getPlayerScoreInfo(net.minecraft.world.scores.ScoreHolder.forNameOnly(playerName), scoreobjective);
                    String s4 = ChatFormatting.YELLOW + "" + (score == null ? 0 : score.value());
                    g.drawString(fr, s4, l3 - fr.width(s4) - 10, var47 + 15, 0xFFFFFF, true);
                }
            }
            g.drawString(fr, display, var20, var47 + 15, 0xFFFFFF, true);
            g.pose().pushPose();
            g.pose().translate(0, 0, 100);
            g.blitSprite(pingSprite(info.getLatency()), var20 + var16 - 12, var47 + 15, 10, 8);
            String ms = I18n.get("tukmc.hud.ping", info.getLatency());
            drawHalfString(g, fr, ms, (var20 + var16 - 9 - fr.width(ms) / 2) * 2, (var47 + 15) * 2, 0xFFFFFF, true);
            g.pose().popPose();
        }
    }

    private void drawScoreboardSidebar(GuiGraphics g) {
        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective objective = null;
        PlayerTeam playerteam = scoreboard.getPlayersTeam(mc.player.getScoreboardName());
        if (playerteam != null) {
            DisplaySlot slot = DisplaySlot.teamColorToSlot(playerteam.getColor());
            if (slot != null)
                objective = scoreboard.getDisplayObjective(slot);
        }
        if (objective == null)
            objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null)
            return;

        int par2 = g.guiHeight();
        int par3 = g.guiWidth();
        Font fr = mc.font;
        List<PlayerScoreEntry> collection = scoreboard.listPlayerScores(objective).stream()
                .filter(e -> !e.isHidden())
                .sorted(java.util.Comparator.comparing(PlayerScoreEntry::value)
                        .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER).reversed())
                .limit(15).toList();
        // the original walked an ascending collection, drawing upwards from the lowest score
        collection = new ArrayList<>(collection);
        java.util.Collections.reverse(collection);

        String title = objective.getDisplayName().getString();
        int k = fr.width(title);
        for (PlayerScoreEntry score : collection) {
            PlayerTeam team = scoreboard.getPlayersTeam(score.owner());
            String s = PlayerTeam.formatNameForTeam(team, score.ownerName()).getString() + ": " + ChatFormatting.RED
                    + score.value();
            k = Math.max(k, fr.width(s));
        }

        int l = collection.size() * fr.lineHeight;
        int i1 = par2 / 2 + l / 3;
        int b0 = 3;
        int j1 = par3 - k - b0;
        int i2 = par3 - b0 + 2;
        int n = collection.size();
        if (n > 0) {
            int topLine = i1 - n * fr.lineHeight;
            drawDoubleOutlinedBox(g, j1 - 2, topLine - fr.lineHeight - 1, i2, 20 + (n - 1) * fr.lineHeight,
                    TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
            g.drawString(fr, title, j1 + k / 2 - fr.width(title) / 2, topLine - fr.lineHeight, 3648127, false);
        }
        int k1 = 0;
        for (PlayerScoreEntry score1 : collection) {
            ++k1;
            PlayerTeam team = scoreboard.getPlayersTeam(score1.owner());
            Component s1 = PlayerTeam.formatNameForTeam(team, score1.ownerName());
            String s2 = ChatFormatting.RED + "" + score1.value();
            int l1 = i1 - k1 * fr.lineHeight;
            g.drawString(fr, s1, j1, l1, 0xFFFFFF, false);
            g.drawString(fr, s2, i2 - fr.width(s2), l1, 3648127, false);
        }
    }
}
