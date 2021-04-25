package vazkii.quark.vanity.feature;

import java.util.List;

import vazkii.quark.base.network.message.MessageRequestCancelEmote;
import vazkii.arl.network.NetworkHandler;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CancelEmoteCommand extends CommandBase {

	private final List<String> aliases = Lists.newArrayList("ce", "cancelemote");

	@Override
	public void execute(MinecraftServer arg0, ICommandSender arg1, String[] arg2)
			throws CommandException {
		NetworkHandler.INSTANCE.sendToServer(new MessageRequestCancelEmote());
		return;
	}

	@Override
	public String getName() {
		return "emote";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "ce";
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() {
	    return 0;
	}

}
