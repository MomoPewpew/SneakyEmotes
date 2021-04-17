/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [18/03/2016, 22:36:08 (GMT)]
 */
package vazkii.quark.vanity;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import vazkii.quark.base.module.Module;
import vazkii.quark.vanity.feature.EmoteSystem;

public class QuarkVanity extends Module {

	@Override
	public void addFeatures() {
		registerFeature(new EmoteSystem());
	}

	@Override
	public ItemStack getIconStack() {
		return new ItemStack(Items.LEATHER_HELMET);
	}

}