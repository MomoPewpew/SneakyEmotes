/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [26/03/2016, 21:53:17 (GMT)]
 */
package vazkii.quark.vanity.feature;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.arl.network.NetworkHandler;
import vazkii.aurelienribon.tweenengine.Tween;
import vazkii.quark.base.client.ContributorRewardHandler;
import vazkii.quark.base.client.ModKeybinds;
import vazkii.quark.base.client.gui.GuiButtonTranslucent;
import vazkii.quark.base.module.Feature;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.network.message.MessageRequestEmote;
import vazkii.quark.vanity.client.emotes.*;
import vazkii.quark.vanity.client.gui.GuiButtonEmote;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.util.text.TextComponentTranslation;

public class EmoteSystem extends Feature {

	private static final String[] EMOTE_NAMES = new String[] {
			"no",
			"yes",
			"wave",
			"salute",
			"cheer",
			"clap",
			"think",
			"point",
			"shrug",
			"headbang",
			"weep",
			"facepalm"
	};

	private static final Set<String> PATREON_EMOTES = ImmutableSet.of(
			"dance",
			"tpose",
			"dab",
			"jet",
			"exorcist",
			"zombie"
	);

	public static final int EMOTE_BUTTON_WIDTH = 60;
	public static final int EMOTE_BUTTON_HEIGHT = 10;
	public static final int EMOTES_PER_ROW = 4;

	private static final List<String> EMOTE_NAME_LIST = Lists.newArrayList(EMOTE_NAMES);

	private static final int EMOTE_BUTTON_START = 1800;
	public static boolean emotesVisible = false;

	public static boolean customEmoteDebug, emoteCommands;
	public static File emotesDir;

	@SideOnly(Side.CLIENT)
	public static CustomEmoteIconResourcePack resourcePack;

	private String[] enabledEmotes;
	private String[] customEmotes;
	private String[] blockbenchEmotes;
	private static boolean enableKeybinds;

	@Override
	public void setupConfig() {
		enableKeybinds = loadPropBool("Enable Keybinds", "Should keybinds for emotes be generated? (They're all unbound by default)", true);
		enabledEmotes = loadPropStringList("Enabled Emotes", "The enabled default emotes. Remove from this list to disable them. You can also re-order them, if you feel like it.", EMOTE_NAMES);
		customEmotes = loadPropStringList("Custom Emotes", "The list of Custom Emotes to be loaded.\nWatch the tutorial on Custom Emotes to learn how to make your own: https://youtu.be/ourHUkan6aQ", new String[0]);
		blockbenchEmotes = loadPropStringList("Blockbench Emotes", "This list works exactly the same as custom emotes, but it accepts Json outputs from Blockbench.", new String[0]);

		customEmoteDebug = loadPropBool("Custom Emote Dev Mode", "Enable this to make custom emotes read the file every time they're triggered so you can edit on the fly.\nDO NOT ship enabled this in a modpack, please.", false);
		emoteCommands = loadPropBool("Custom Emote Functions", "Allow custom emotes to run function files when a user prompts them.\n"
				+ "To attach a function file to any given emote, simply place a .mcfunction file with the same name as your .emote file (sans extension) in /SneakyEmotes.\n"
				+ "Command output from emote functions is enabled only if both \"Custom Emote Dev Mode\" and the \"commandBlockOutput\" gamerule are enabled. ", false);

		emotesDir = new File(ModuleLoader.configFile.getParent(), "SneakyEmotes");
		if(!emotesDir.exists())
			if (!emotesDir.mkdir())
				customEmotes = new String[0];
	}

	@SideOnly(Side.CLIENT)
	public static void addResourcePack(List<IResourcePack> packs) {
		packs.add(resourcePack = new CustomEmoteIconResourcePack());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preInitClient() {
		Tween.registerAccessor(ModelBiped.class, ModelAccessor.INSTANCE);

		for(String s : enabledEmotes)
			if(EMOTE_NAME_LIST.contains(s))
				EmoteHandler.addEmote(s);

		for(String s : PATREON_EMOTES)
			EmoteHandler.addEmote(s);

		for(String s : customEmotes)
			EmoteHandler.addCustomEmote(s);

		for(String s : blockbenchEmotes)
			EmoteHandler.addBlockbenchEmote(s);

		if(enableKeybinds)
			ModKeybinds.initEmoteKeybinds();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		if(gui instanceof GuiChat) {
			List<GuiButton> list = event.getButtonList();
			list.add(new GuiButtonTranslucent(EMOTE_BUTTON_START, gui.width - 1 - EMOTE_BUTTON_WIDTH, gui.height - 40, EMOTE_BUTTON_HEIGHT * EMOTES_PER_ROW, 20, I18n.format("quark.gui.emotes")));
			emotesVisible = false;

			TIntObjectHashMap<List<EmoteDescriptor>> descriptorSorting = new TIntObjectHashMap<>();

			for (EmoteDescriptor desc : EmoteHandler.emoteMap.values()) {
				if (desc.getTier() <= ContributorRewardHandler.localPatronTier) {
					List<EmoteDescriptor> descriptors = descriptorSorting.get(desc.getTier());
					if (descriptors == null)
						descriptorSorting.put(desc.getTier(), descriptors = Lists.newArrayList());

					descriptors.add(desc);
				}
			}

			int rows = 0;

			int i = 0;
			int row = 0;
			int tierRow, rowPos;

			int[] keys = descriptorSorting.keys();
			Arrays.sort(keys);


			for (int tier : keys) {
				List<EmoteDescriptor> descriptors = descriptorSorting.get(tier);
				if (descriptors != null) {
					rows += descriptors.size() / 3;
					if (descriptors.size() % 3 != 0)
						rows++;
				}
			}

			for (int tier : keys) {
				rowPos = 0;
				tierRow = 0;
				List<EmoteDescriptor> descriptors = descriptorSorting.get(tier);
				if (descriptors != null) {
					for (EmoteDescriptor desc : descriptors) {
						int rowSize = Math.min(descriptors.size() - tierRow * EMOTES_PER_ROW, EMOTES_PER_ROW);

						int x = gui.width - (((rowPos + 1) * 2 + EMOTES_PER_ROW - rowSize) * EMOTE_BUTTON_WIDTH / 2 + 1);
						int y = gui.height - (21 + EMOTE_BUTTON_HEIGHT * (rows - row));

						GuiButton button = new GuiButtonEmote(EMOTE_BUTTON_START + i + 1, x, y, desc);
						button.visible = emotesVisible;
						button.enabled = emotesVisible;
						list.add(button);

						i++;

						if (++rowPos == EMOTES_PER_ROW) {
							tierRow++;
							row++;
							rowPos = 0;
						}
					}
				}
				if (rowPos != 0)
					row++;
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void performAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
		GuiButton button = event.getButton();

		if(button.id == EMOTE_BUTTON_START) {
			event.getGui();
			List<GuiButton> list = event.getButtonList();

			for(GuiButton b : list)
				if(b instanceof GuiButtonEmote) {
					b.visible = !b.visible;
					b.enabled = !b.enabled;
				}

			emotesVisible = !emotesVisible;
		} else if(button instanceof GuiButtonEmote) {
			String name = ((GuiButtonEmote) button).desc.getRegistryName();
			NetworkHandler.INSTANCE.sendToServer(new MessageRequestEmote(name));
			Minecraft.getMinecraft().player.closeScreen();
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(KeyInputEvent event) {
		onHotkey();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onMouseInput(MouseInputEvent event) {
		onHotkey();
	}

	@SideOnly(Side.CLIENT)
	public void onHotkey() {
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.inGameHasFocus && enableKeybinds) {
			for(KeyBinding key : ModKeybinds.emoteKeys.keySet())
				if(key.isKeyDown()) {
					String emote = ModKeybinds.emoteKeys.get(key);
					Boolean emoting = false;

/*					if(EmoteHandler.getPlayeremotes().containsKey(mc.player.getName())) {
						for (Entry<String, EmoteBase> pair : EmoteHandler.getPlayeremotes().entrySet()) {
							if (pair.getKey() == mc.player.getName() && pair.getValue().desc.name == emote) {
								emoting = true;
							}
						}
					}*/

					if(EmoteHandler.getPlayeremotes().containsKey(mc.player.getName())) {
						if (EmoteHandler.getPlayerEmote(mc.player).desc.name == emote) {
							emoting = true;
						}
					}

					if (emoting == false) {
						NetworkHandler.INSTANCE.sendToServer(new MessageRequestEmote(emote));
						return;
					}
				}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void drawHUD(RenderGameOverlayEvent.Post event) {

	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderTick(RenderTickEvent event) {
		EmoteHandler.onRenderTick(Minecraft.getMinecraft());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@SideOnly(Side.CLIENT)
	public void preRenderLiving(RenderLivingEvent.Pre event) {
		if (event.getEntity() instanceof EntityPlayer)
			EmoteHandler.preRender((EntityPlayer) event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	@SideOnly(Side.CLIENT)
	public void postRenderLiving(RenderLivingEvent.Post event) {
		if (event.getEntity() instanceof EntityPlayer)
			EmoteHandler.postRender((EntityPlayer) event.getEntity());
	}

	@Override
	public boolean hasSubscriptions() {
		return isClient();
	}

	@Override
	public boolean requiresMinecraftRestartToEnable() {
		return true;
	}

	public static String[] getEmoteNames() {
		return EMOTE_NAMES;
	}

	public static Set<String> getPatreonEmoteNames() {
		return PATREON_EMOTES;
	}

	public static boolean isEnableKeybinds() {
		return enableKeybinds;
	}

}
