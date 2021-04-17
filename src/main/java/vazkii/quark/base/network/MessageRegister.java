/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [28/08/2016, 00:37:40 (GMT)]
 */
package vazkii.quark.base.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import vazkii.arl.network.NetworkHandler;
import vazkii.arl.network.NetworkMessage;
import vazkii.quark.base.network.message.*;

import java.io.IOException;
import java.util.UUID;

public class MessageRegister {

	@SuppressWarnings("unchecked")
	public static void init() {
		NetworkHandler.register(MessageDoEmote.class, Side.CLIENT);
		NetworkHandler.register(MessageRequestEmote.class, Side.SERVER);

		NetworkMessage.mapHandler(ITextComponent.class, MessageRegister::readComponent, MessageRegister::writeComponent);
		NetworkMessage.mapHandler(UUID.class, MessageRegister::readUUID, MessageRegister::writeUUID);
	}

	private static ITextComponent readComponent(ByteBuf buf) {
		try {
			return new PacketBuffer(buf).readTextComponent();
		} catch (IOException e) {
			return new TextComponentString("");
		}
	}

	private static void writeComponent(ITextComponent comp, ByteBuf buf) {
		new PacketBuffer(buf).writeTextComponent(comp);
	}

	private static UUID readUUID(ByteBuf buf) {
		if (buf.readBoolean())
			return null;
		return new PacketBuffer(buf).readUniqueId();
	}

	private static void writeUUID(UUID uuid, ByteBuf buf) {
		if (uuid == null)
			buf.writeBoolean(true);
		else {
			buf.writeBoolean(false);
			new PacketBuffer(buf).writeUniqueId(uuid);
		}
	}

}
