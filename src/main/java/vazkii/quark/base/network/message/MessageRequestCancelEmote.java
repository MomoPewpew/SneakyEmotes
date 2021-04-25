package vazkii.quark.base.network.message;

import net.minecraft.command.FunctionObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.IOUtils;
import vazkii.arl.network.NetworkHandler;
import vazkii.arl.network.NetworkMessage;
import vazkii.quark.base.client.ContributorRewardHandler;
import vazkii.quark.vanity.feature.EmoteSystem;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MessageRequestCancelEmote extends NetworkMessage<MessageRequestCancelEmote> {


	public MessageRequestCancelEmote() { }

	@Override
	public IMessage handleMessage(MessageContext context) {
		EntityPlayerMP player = context.getServerHandler().player;
		MinecraftServer server = player.getServer();
		if (server != null) server.addScheduledTask(() -> {
			NetworkHandler.INSTANCE.sendToAll(new MessageCancelEmote(player.getName()));
		});

		return null;
	}

	private static class EmoteCommandSender implements ICommandSender {

		public final MinecraftServer server;
		public final ICommandSender superSender;

		public EmoteCommandSender(MinecraftServer server, ICommandSender superSender) {
			this.server = server;
			this.superSender = superSender;
		}

		@Override
		public MinecraftServer getServer() {
			return server;
		}

		@Nonnull
		@Override
		public String getName() {
			return "Quark-Emotes[" + superSender.getName() + "]";
		}

		@Nonnull
		@Override
		public World getEntityWorld() {
			return superSender.getEntityWorld();
		}

		@Override
		public boolean canUseCommand(int permLevel, @Nonnull String commandName) {
			return !commandName.equals("cancelemote") && permLevel <= 2;
		}

		@Nonnull
		@Override
		public BlockPos getPosition() {
			return superSender.getPosition();
		}

		@Nonnull
		@Override
		public Vec3d getPositionVector() {
			return superSender.getPositionVector();
		}

		@Override
		public Entity getCommandSenderEntity() {
			return superSender.getCommandSenderEntity();
		}

		@Override
		public boolean sendCommandFeedback() {
			return EmoteSystem.customEmoteDebug && getEntityWorld().getGameRules().getBoolean("commandBlockOutput");
		}

	}

}
