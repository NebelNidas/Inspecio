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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.github.reviversmc.advancedtooltips.api.AdvancedTooltipsEntrypoint;
import com.github.reviversmc.advancedtooltips.api.InventoryProvider;
import com.github.reviversmc.advancedtooltips.tooltip.ConvertibleTooltipData;

import io.github.queerbric.inspecio.api.InspecioEntrypoint;

import java.util.List;
import java.util.function.Consumer;

public class AdvancedTooltips implements ClientModInitializer {
	public static final String NAMESPACE = "advancedtooltips";
	private static final Logger LOGGER = LogManager.getLogger(NAMESPACE);
	private static AdvancedTooltipsConfig config = AdvancedTooltipsConfig.defaultConfig();

	// TODO: Switch to Client Tag API: https://github.com/FabricMC/fabric/pull/2308
	public static final TagKey<Item> HIDDEN_EFFECTS_TAG = TagKey.of(Registry.ITEM_KEY, new Identifier(NAMESPACE, "hidden_effects"));
	public static List<Item> hiddenEffectsItems = List.of(Items.SUSPICIOUS_STEW);


	@Override
	public void onInitializeClient() {
		reloadConfig();

		InventoryProvider.register((stack, config) -> {
			if (config != null && config.isEnabled() && stack.getItem() instanceof BlockItem blockItem) {
				DyeColor color = null;
				if (blockItem.getBlock() instanceof ShulkerBoxBlock shulkerBoxBlock && ((AdvancedTooltipsConfig.ShulkerBoxConfig) config).hasColor())
					color = shulkerBoxBlock.getColor();

				var nbt = BlockItem.getBlockEntityNbtFromStack(stack);
				if (nbt == null) return null;

				DefaultedList<ItemStack> inventory = DefaultedList.ofSize(getInvSizeFor(stack), ItemStack.EMPTY);
				Inventories.readNbt(nbt, inventory);
				if (inventory.stream().allMatch(ItemStack::isEmpty))
					return null;

				return new InventoryProvider.Context(inventory, color);
			}

			return null;
		});

		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof ConvertibleTooltipData convertible) {
				return convertible.getComponent();
			}
			return null;
		});

		AdvancedTooltipsCommand.init();

		List<AdvancedTooltipsEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("advancedtooltips", AdvancedTooltipsEntrypoint.class);
		for (var entrypoint : entrypoints) {
			entrypoint.onAdvancedTooltipsInitialized();
		}
		List<InspecioEntrypoint> legacyEntrypoints = FabricLoader.getInstance().getEntrypoints("inspecio", InspecioEntrypoint.class);
		for (var entrypoint : legacyEntrypoints) {
			entrypoint.onInspecioInitialized();
		}
	}

	/**
	 * Prints a message to the terminal.
	 *
	 * @param info the message to log
	 */
	public static void log(String info) {
		LOGGER.info("[Advanced Tooltips] " + info);
	}

	/**
	 * Prints a warning message to the terminal.
	 *
	 * @param info the message to log
	 */
	public static void warn(String info) {
		LOGGER.warn("[Advanced Tooltips] " + info);
	}

	/**
	 * Prints a warning message to the terminal.
	 *
	 * @param info the message to log
	 * @param params parameters to the message.
	 */
	public static void warn(String info, Object... params) {
		LOGGER.warn("[Advanced Tooltips] " + info, params);
	}

	/**
	 * Prints a warning message to the terminal.
	 *
	 * @param info the message to log
	 * @param throwable the exception to log, including its stack trace.
	 */
	public static void warn(String info, Throwable throwable) {
		LOGGER.warn("[Advanced Tooltips] " + info, throwable);
	}

	public static AdvancedTooltipsConfig getConfig() {
		return config;
	}

	static void reloadConfig() {
		config = AdvancedTooltipsConfig.load();
	}

	static Consumer<String> onConfigError(String path) {
		return error -> {
			AdvancedTooltipsConfig.shouldSaveConfigAfterLoad = true;
			warn("Configuration error at \"" + path + "\", error: " + error);
		};
	}

	static String getVersion() {
		return FabricLoader.getInstance().getModContainer(NAMESPACE)
				.map(container -> {
					var version = container.getMetadata().getVersion().getFriendlyString();
					if (version.equals("${version}"))
						return "dev";
					return version;
				}).orElse("unknown");
	}

	private static int getInvSizeFor(ItemStack stack) {
		if (stack.getItem() instanceof BlockItem blockItem) {
			var block = blockItem.getBlock();
			if (block instanceof DispenserBlock)
				return 9;
			else if (block instanceof HopperBlock)
				return 5;
			return 27;
		}
		return 0;
	}

	/**
	 * Appends block item tooltips.
	 *
	 * @param stack the stack to add tooltip to
	 * @param block the block
	 * @param tooltip the tooltip
	 */
	public static void appendBlockItemTooltip(ItemStack stack, Block block, List<Text> tooltip) {
		var config = AdvancedTooltips.getConfig().getContainersConfig().forBlock(block);
		if (config != null && config.hasLootTable()) {
			var blockEntityNbt = BlockItem.getBlockEntityNbtFromStack(stack);
			if (blockEntityNbt != null && blockEntityNbt.contains("LootTable")) {
				tooltip.add(Text.translatable("advancedtooltips.tooltip.loot_table",
						Text.literal(blockEntityNbt.getString("LootTable"))
								.formatted(Formatting.GOLD))
						.formatted(Formatting.GRAY));
			}
		}
	}

	public static void removeVanillaTooltips(List<Text> tooltips, int fromIndex) {
		if (fromIndex >= tooltips.size()) return;

		int keepIndex = tooltips.indexOf(Text.empty());
		if (keepIndex != -1) {
			// we wanna keep tooltips that come after a line break
			keepIndex++;

			int tooltipsToKeep = tooltips.size() - keepIndex;

			// shift tooltips to keep to the front
			for (int i = 0; i < tooltipsToKeep; i++) {
				tooltips.set(fromIndex + i, tooltips.get(keepIndex + i));
			}

			// don't remove them
			fromIndex += tooltipsToKeep;
		}

		tooltips.subList(fromIndex, tooltips.size()).clear();
	}

	public static @Nullable StatusEffectInstance getRawEffectFromTag(NbtCompound tag, String tagKey) {
		if (tag == null) {
			return null;
		}
		if (tag.contains(tagKey, NbtElement.INT_TYPE)) {
			var effect = StatusEffect.byRawId(tag.getInt(tagKey));
			if (effect != null)
				return new StatusEffectInstance(effect, 200, 0);
		}
		return null;
	}
}
