package vazkii.quark.vanity.client.emotes;

import net.minecraft.util.ResourceLocation;
import vazkii.quark.vanity.feature.EmoteSystem;

public class BlockbenchEmoteDescriptor extends EmoteDescriptor {

	public BlockbenchEmoteDescriptor(String name, String regName, int index) {
		super(CustomEmote.class, name, regName, index, getSprite(name), new BlockbenchEmoteTemplate(name));
	}

	public static ResourceLocation getSprite(String name) {
		ResourceLocation customRes = new ResourceLocation(EmoteHandler.CUSTOM_EMOTE_NAMESPACE, name);
		if(EmoteSystem.resourcePack.hasResourceName(customRes.toString()))
			return customRes;

		return new ResourceLocation("quark", "textures/emotes/custom.png");
	}

	@Override
	public String getTranslationKey() {
		return ((BlockbenchEmoteTemplate) template).getName();
	}

	@Override
	public String getLocalizedName() {
		return getTranslationKey();
	}

}
