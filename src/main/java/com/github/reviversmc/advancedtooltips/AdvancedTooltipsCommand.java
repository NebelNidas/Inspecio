/*
 * Copyright (c) 2020 - 2022 LambdAurora <email@lambdaurora.dev>, Emi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.reviversmc.advancedtooltips;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

// TODO: Replace with AutoConfig
public final class AdvancedTooltipsCommand {
	private AdvancedTooltipsCommand() {
		throw new UnsupportedOperationException("AdvancedTooltipsCommand only contains static-definitions");
	}

	static void init() {
		var literalSubCommand = literal("config");

		{
			literalSubCommand.then(literal("reload")
					.executes(ctx -> {
						ctx.getSource().sendFeedback(Text.translatable("advancedtooltips.config.reloading").formatted(Formatting.GREEN));
						AdvancedTooltips.reloadConfig();
						return 0;
					})
			).then(literal("armor")
					.executes(onGetter("armor", getter(AdvancedTooltipsConfig::hasArmor)))
					.then(argument("value", BoolArgumentType.bool())
							.executes(onBooleanSetter("armor", setter(AdvancedTooltipsConfig::setArmor))))
			).then(literal("banner_pattern")
					.executes(onGetter("banner_pattern", getter(AdvancedTooltipsConfig::hasBannerPattern)))
					.then(argument("value", BoolArgumentType.bool())
							.executes(onBooleanSetter("armor", setter(AdvancedTooltipsConfig::setBannerPattern))))
			).then(literal("containers")
					.then(literal("campfire")
							.executes(onGetter("containers/campfire", getter(cfg -> cfg.getContainersConfig().isCampfireEnabled())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("containers/campfire", setter((cfg, val) -> cfg.getContainersConfig().setCampfire(val))))))
					.then(initContainer("storage", cfg -> cfg.getContainersConfig().getStorageConfig()))
					.then(initContainer("shulker_box", cfg -> cfg.getContainersConfig().getShulkerBoxConfig())
							.then(literal("color")
									.executes(onGetter("containers/shulker_box/color", getter(cfg -> cfg.getContainersConfig().getShulkerBoxConfig().hasColor())))
									.then(argument("value", BoolArgumentType.bool())
											.executes(onBooleanSetter("containers/shulker_box/color",
													setter((cfg, val) -> cfg.getContainersConfig().getShulkerBoxConfig().setColor(val)))))))
			).then(literal("effects")
					.then(literal("potions")
							.executes(onGetter("effects/potions", getter(cfg -> cfg.getEffectsConfig().hasPotions())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("effects/potions", setter((cfg, val) -> cfg.getEffectsConfig().setPotions(val)))))
					).then(literal("tipped_arrows")
							.executes(onGetter("effects/tipped_arrows", getter(cfg -> cfg.getEffectsConfig().hasTippedArrows())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("effects/tipped_arrows", setter((cfg, val) -> cfg.getEffectsConfig().setTippedArrows(val)))))
					).then(literal("spectral_arrow")
							.executes(onGetter("effects/spectral_arrow", getter(cfg -> cfg.getEffectsConfig().hasSpectralArrow())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("effects/spectral_arrow", setter((cfg, val) -> cfg.getEffectsConfig().setSpectralArrow(val)))))
					).then(literal("food")
							.executes(onGetter("effects/food", getter(cfg -> cfg.getEffectsConfig().hasFood())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("effects/food", setter((cfg, val) -> cfg.getEffectsConfig().setFood(val)))))
					).then(literal("hidden_motion")
							.executes(onGetter("effects/hidden_motion", getter(cfg -> cfg.getEffectsConfig().hasHiddenMotion())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("effects/hidden_motion", setter((cfg, val) -> cfg.getEffectsConfig().setHiddenMotion(val)))))
					).then(literal("beacon")
							.executes(onGetter("effects/beacon", getter(cfg -> cfg.getEffectsConfig().hasHiddenMotion())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("effects/beacon", setter((cfg, val) -> cfg.getEffectsConfig().setBeacon(val)))))
					)
			).then(literal("entities")
					.then(initEntity("armor_stand", cfg -> cfg.getEntitiesConfig().getArmorStandConfig()))
					.then(initEntity("bee", cfg -> cfg.getEntitiesConfig().getBeeConfig()))
					.then(initEntity("fish_bucket", cfg -> cfg.getEntitiesConfig().getFishBucketConfig()))
					.then(initEntity("spawn_egg", cfg -> cfg.getEntitiesConfig().getSpawnEggConfig()))
					.then(literal("pufferfish_puff_state")
							.executes(onGetter("entities/pufferfish_puff_state", getter(cfg -> cfg.getEntitiesConfig().getPufferFishPuffState())))
							.then(argument("value", IntegerArgumentType.integer(0, 2))
									.executes(onIntegerSetter("entities/pufferfish_puff_state", setter((cfg, val) -> cfg.getEntitiesConfig().setPufferFishPuffState(val))))))
			).then(literal("filled_map")
					.executes(onGetter("filled_map", getter(cfg -> cfg.getFilledMapConfig().isEnabled())))
					.then(argument("value", BoolArgumentType.bool())
							.executes(onBooleanSetter("filled_map", setter((cfg, val) -> cfg.getFilledMapConfig().setEnabled(val)))))
					.then(literal("show_player_icon")
							.executes(onGetter("filled_map/show_player_icon", getter(cfg -> cfg.getFilledMapConfig().shouldShowPlayerIcon())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("filled_map/show_player_icon", setter((cfg, val) -> cfg.getFilledMapConfig().setShowPlayerIcon(val))))))
			).then(literal("food")
					.then(literal("hunger")
							.executes(onGetter("food/hunger", getter(cfg -> cfg.getFoodConfig().hasHunger())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("food/hunger", setter((cfg, val) -> cfg.getFoodConfig().setHunger(val))))))
					.then(literal("saturation")
							.executes(onGetter("food/saturation", getter(cfg -> cfg.getFoodConfig().getSaturationMode())))
							.then(argument("value", SaturationTooltipMode.SaturationArgumentType.saturationTooltipMode())
									.executes(AdvancedTooltipsCommand::onSetSaturation)))
			).then(literal("jukebox")
					.executes(onGetter("jukebox", getter(AdvancedTooltipsConfig::getJukeboxTooltipMode)))
					.then(argument("value", JukeboxTooltipMode.JukeboxArgumentType.jukeboxTooltipMode())
							.executes(AdvancedTooltipsCommand::onSetJukebox))
			).then(literal("sign")
					.executes(onGetter("sign", getter(AdvancedTooltipsConfig::getSignTooltipMode)))
					.then(argument("value", SignTooltipMode.SignArgumentType.signTooltipMode())
							.executes(AdvancedTooltipsCommand::onSetSign))
			).then(literal("advanced_tooltips")
					.then(literal("repair_cost")
							.executes(onGetter("advanced_tooltips/repair_cost", getter(cfg -> cfg.getAdvancedConfig().hasRepairCost())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("advanced_tooltips/repair_cost", setter((cfg, val) -> cfg.getAdvancedConfig().setRepairCost(val))))))
					.then(literal("lodestone_coords")
							.executes(onGetter("advanced_tooltips/lodestone_coords", getter(cfg -> cfg.getAdvancedConfig().hasLodestoneCoords())))
							.then(argument("value", BoolArgumentType.bool())
									.executes(onBooleanSetter("advanced_tooltips/lodestone_coords", setter((cfg, val) -> cfg.getAdvancedConfig().setLodestoneCoords(val))))))
			);
		}

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
				literal("advancedtooltips")
						.executes(onAdvancedTooltipsCommand(literalSubCommand.build()))
						.then(literalSubCommand)
			);
		});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> initContainer(String name,
	                                                                               Function<AdvancedTooltipsConfig, AdvancedTooltipsConfig.StorageContainerConfig> containerGetter) {
		var prefix = "containers/" + name;
		return literal(name)
				.executes(onGetter(prefix, () -> containerGetter.apply(AdvancedTooltips.getConfig()).isEnabled()))
				.then(argument("value", BoolArgumentType.bool())
						.executes(onBooleanSetter(prefix, val -> containerGetter.apply(AdvancedTooltips.getConfig()).setEnabled(val))))
				.then(literal("compact")
						.executes(onGetter(prefix + "/compact", () -> containerGetter.apply(AdvancedTooltips.getConfig()).isCompact()))
						.then(argument("value", BoolArgumentType.bool())
								.executes(onBooleanSetter(prefix + "/compact", val -> containerGetter.apply(AdvancedTooltips.getConfig()).setCompact(val)))))
				.then(literal("loot_table")
						.executes(onGetter(prefix + "/loot_table", () -> containerGetter.apply(AdvancedTooltips.getConfig()).hasLootTable()))
						.then(argument("value", BoolArgumentType.bool())
								.executes(onBooleanSetter(prefix + "/loot_table", val -> containerGetter.apply(AdvancedTooltips.getConfig()).setLootTable(val)))));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> initEntity(String name,
	                                                                            Function<AdvancedTooltipsConfig, AdvancedTooltipsConfig.EntityConfig> containerGetter) {
		var prefix = "entities/" + name;
		return literal(name)
				.executes(onGetter(prefix, () -> containerGetter.apply(AdvancedTooltips.getConfig()).isEnabled()))
				.then(argument("value", BoolArgumentType.bool())
						.executes(onBooleanSetter(prefix, val -> containerGetter.apply(AdvancedTooltips.getConfig()).setEnabled(val))))
				.then(literal("always_show_name")
						.executes(onGetter(prefix + "/always_show_name", () -> containerGetter.apply(AdvancedTooltips.getConfig()).shouldAlwaysShowName()))
						.then(argument("value", BoolArgumentType.bool())
								.executes(onBooleanSetter(prefix + "/always_show_name", val -> containerGetter.apply(AdvancedTooltips.getConfig()).setAlwaysShowName(val)))))
				.then(literal("spin")
						.executes(onGetter(prefix + "/spin", () -> containerGetter.apply(AdvancedTooltips.getConfig()).shouldSpin()))
						.then(argument("value", BoolArgumentType.bool())
								.executes(onBooleanSetter(prefix + "/spin", val -> containerGetter.apply(AdvancedTooltips.getConfig()).setSpin(val)))));
	}

	private static Text formatBoolean(boolean bool) {
		return bool ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED);
	}

	private static Command<FabricClientCommandSource> onAdvancedTooltipsCommand(LiteralCommandNode<FabricClientCommandSource> config) {
		var msg = Text.literal("Tooltip").formatted(Formatting.GOLD)
				.append(Text.literal(" v" + AdvancedTooltips.getVersion() + "\n").formatted(Formatting.GRAY));
		buildHelpCommand(config, 0, msg);
		return ctx -> {
			ctx.getSource().sendFeedback(msg);
			return 0;
		};
	}

	private static void buildHelpCommand(LiteralCommandNode<FabricClientCommandSource> node, int step, MutableText text) {
		text.append(Text.literal('\n' + " ".repeat(step * 2) + "- ").formatted(Formatting.GRAY)
				.append(Text.literal(node.getLiteral()).formatted(Formatting.GOLD)));

		for (var child : node.getChildren()) {
			if (child instanceof LiteralCommandNode) {
				buildHelpCommand((LiteralCommandNode<FabricClientCommandSource>) child, step + 1, text);
			}
		}
	}

	private static int onSetJukebox(CommandContext<FabricClientCommandSource> context) {
		var value = JukeboxTooltipMode.JukeboxArgumentType.getJukeboxTooltipMode(context, "value");
		var config = AdvancedTooltips.getConfig();
		config.setJukeboxTooltipMode(value);
		config.save();
		context.getSource().sendFeedback(prefix("jukebox").append(Text.literal(value.toString()).formatted(Formatting.WHITE)));
		return 0;
	}

	private static int onSetSaturation(CommandContext<FabricClientCommandSource> context) {
		var value = SaturationTooltipMode.SaturationArgumentType.getSaturationTooltipMode(context, "value");
		var config = AdvancedTooltips.getConfig();
		config.getFoodConfig().setSaturationMode(value);
		config.save();
		context.getSource().sendFeedback(prefix("food/saturation").append(Text.literal(value.toString()).formatted(Formatting.WHITE)));
		return 0;
	}

	private static int onSetSign(CommandContext<FabricClientCommandSource> context) {
		var value = SignTooltipMode.SignArgumentType.getSignTooltipMode(context, "value");
		var config = AdvancedTooltips.getConfig();
		config.setSignTooltipMode(value);
		config.save();
		context.getSource().sendFeedback(prefix("sign").append(Text.literal(value.toString()).formatted(Formatting.WHITE)));
		return 0;
	}

	private static MutableText prefix(String path) {
		return Text.literal(path).formatted(Formatting.GOLD).append(Text.literal(": ").formatted(Formatting.GRAY));
	}

	private static <T> Supplier<T> getter(Function<AdvancedTooltipsConfig, T> func) {
		return () -> func.apply(AdvancedTooltips.getConfig());
	}

	private static <T> Consumer<T> setter(BiConsumer<AdvancedTooltipsConfig, T> func) {
		return val -> func.accept(AdvancedTooltips.getConfig(), val);
	}

	private static <T> Command<FabricClientCommandSource> onGetter(String path, Supplier<T> getter) {
		return context -> {
			var value = getter.get();

			Text valueText;

			if (value instanceof Boolean boolValue) valueText = formatBoolean(boolValue);
			else valueText = Text.literal(value.toString()).formatted(Formatting.WHITE);

			context.getSource().sendFeedback(prefix(path).append(valueText));

			return 0;
		};
	}

	private static Command<FabricClientCommandSource> onBooleanSetter(String path, Consumer<Boolean> setter) {
		return context -> {
			var value = BoolArgumentType.getBool(context, "value");

			setter.accept(value);

			AdvancedTooltips.getConfig().save();

			context.getSource().sendFeedback(prefix(path).append(formatBoolean(value)));

			return 0;
		};
	}

	private static Command<FabricClientCommandSource> onIntegerSetter(String path, Consumer<Integer> setter) {
		return context -> {
			var value = IntegerArgumentType.getInteger(context, "value");

			setter.accept(value);

			AdvancedTooltips.getConfig().save();

			context.getSource().sendFeedback(prefix(path).append(Text.literal(String.valueOf(value)).formatted(Formatting.WHITE)));

			return 0;
		};
	}
}
