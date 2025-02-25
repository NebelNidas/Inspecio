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

package io.github.queerbric.inspecio.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Provides an inventory context for the given item stack.
 */
@Environment(EnvType.CLIENT)
@FunctionalInterface
@Deprecated
public interface InventoryProvider extends com.github.reviversmc.advancedtooltips.api.InventoryProvider {}
