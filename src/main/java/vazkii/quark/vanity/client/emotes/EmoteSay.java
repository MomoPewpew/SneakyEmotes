/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 05, 2019, 21:30 AM (EST)]
 */
package vazkii.quark.vanity.client.emotes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.arl.network.NetworkHandler;

@SideOnly(Side.CLIENT)
public class EmoteSay {
	private final EmoteTemplate template;
	private final String message;
	private final EntityPlayer player;

	public static void add(String message, EmoteTemplate template, EntityPlayer player) {
		EmoteSay emoteMagicspell = new EmoteSay(message, template, player);
		((EntityPlayerSP) player).sendChatMessage(message);
	}

	public EmoteSay(String message, EmoteTemplate template, EntityPlayer player) {
		this.template = template;
		this.message = message;
		this.player = player;
	}
}
