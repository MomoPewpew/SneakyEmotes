package vazkii.quark.vanity.feature;

import java.util.Arrays;
import java.util.List;

import vazkii.quark.base.client.ContributorRewardHandler;
import vazkii.quark.base.network.message.MessageDoEmote;
import vazkii.quark.vanity.client.emotes.EmoteHandler;
import vazkii.arl.network.NetworkHandler;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class EmoteCommand extends CommandBase {

	private final List<String> aliases = Lists.newArrayList("e", "emote");

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
			throws CommandException {

		String emote = new String(args[0]);

		if(!Arrays.asList(EmoteSystem.getEmoteNames()).contains(emote) && !EmoteSystem.getPatreonEmoteNames().contains(emote)) {
			emote = EmoteHandler.CUSTOM_PREFIX + emote;
		}

		NetworkHandler.INSTANCE.sendToAll(new MessageDoEmote(emote, sender.getName(), ContributorRewardHandler.getTier((EntityPlayer) sender)));
		return;
	}

	@Override
	public String getName() {
		return "emote";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "e <emote name>";
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
