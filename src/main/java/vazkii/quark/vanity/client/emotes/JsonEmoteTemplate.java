package vazkii.quark.vanity.client.emotes;

import com.google.common.collect.Lists;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.aurelienribon.tweenengine.*;
import vazkii.quark.base.Quark;
import vazkii.quark.vanity.feature.EmoteSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import org.apache.commons.lang3.math.NumberUtils;

@SideOnly(Side.CLIENT)
public class JsonEmoteTemplate extends EmoteTemplate {

	private static final Map<String, Integer> parts = new HashMap<>();
	private static final Map<String, Integer> tweenables = new HashMap<>();
	private static final Map<String, Function> functions = new HashMap<>();
	private static final Map<String, TweenEquation> equations = new HashMap<>();

	static {
		functions.put("use", (em, model, player, timeline, tokens) -> use(em, timeline, tokens));
		functions.put("animation", (em, model, player, timeline, tokens) -> animation(em, timeline, tokens));
		functions.put("section", (em, model, player, timeline, tokens) -> section(em, timeline, tokens));
		functions.put("end", (em, model, player, timeline, tokens) -> end(em, timeline, tokens));
		functions.put("move", (em, model, player, timeline, tokens) -> move(em, model, timeline, tokens));
		functions.put("pause", (em, model, player, timeline, tokens) -> pause(em, timeline, tokens));

		Class<?> clazz = ModelAccessor.class;
		Field[] fields = clazz.getDeclaredFields();
		for(Field f : fields) {
			if(f.getType() != int.class)
				continue;

			int modifiers = f.getModifiers();
			if(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
				try {
					int val = f.getInt(null);
					String name = f.getName().toLowerCase();
					if(name.matches("^.+?_[xyz]$"))
						tweenables.put(name, val);
					else
						parts.put(name, val);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		clazz = TweenEquations.class;
		fields = clazz.getDeclaredFields();
		for(Field f : fields) {
			String name = f.getName().replaceAll("[A-Z]", "_$0").substring(5).toLowerCase();
			try {
				TweenEquation eq = (TweenEquation) f.get(null);
				equations.put(name, eq);
				if(name.equals("none"))
					equations.put("linear", eq);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<String> readLines;
	public List<Integer> usedParts;
	public Stack<Timeline> timelineStack;
	public float speed;
	public int tier;
	public boolean compiled = false;
	public boolean compiledOnce = false;

	private String name;
	private int tweenable;
	private String part;
	private String axisconstructor;
	private float lasttime;
	private String[] lasttarget;

	public JsonEmoteTemplate(String file) {
		super(file + ".json");

		if(name == null)
			name = file;
	}

	@Override
	public Timeline getTimeline(EmoteDescriptor desc, EntityPlayer player, ModelBiped model) {
		compiled = false;
		speed = 1;
		tier = 0;

		if(readLines == null)
			return readAndMakeTimeline(desc, player, model);
		else {
			Timeline timeline = null;
			timelineStack = new Stack<>();

			int i = 0;
			try {
				for(; i < readLines.size() && !compiled; i++)
					timeline = handle(model, player, timeline, readLines.get(i));
			} catch(Exception e) {
				logError(e, i);
				return Timeline.createSequence();
			}

			if(timeline == null)
				return Timeline.createSequence();

			return timeline;
		}
	}

	@Override
	public Timeline readAndMakeTimeline(EmoteDescriptor desc, EntityPlayer player, ModelBiped model) {
		Timeline timeline = null;
		usedParts = new ArrayList<>();
		timelineStack = new Stack<>();
		int lines = 0;

		tier = 0;

		BufferedReader reader = null;
		compiled = compiledOnce = false;
		readLines = new ArrayList<>();
		try {
			reader = createReader();

			try {
				String s;
				while((s = reader.readLine()) != null && !compiled) {
					lines++;
					readLines.add(s);
					timeline = handle(model, player, timeline, s);
				}
			} catch(Exception e) {
				logError(e, lines);
				return fallback();
			}

			if(timeline == null)
				return fallback();

			return timeline;
		} catch(IOException e) {
			e.printStackTrace();
			return fallback();
		} finally {
			compiledOnce = true;
			if (desc != null)
				desc.updateTier(this);
			try {
				if(reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	BufferedReader createReader() throws FileNotFoundException {
		return new BufferedReader(new FileReader(new File(EmoteSystem.emotesDir, file)));
	}

	private void logError(Exception e, int line) {
		Quark.LOG.error("[Custom Emotes] Error loading line " + (line + 1) + " of emote " + file);
		if(!(e instanceof IllegalArgumentException)) {
			Quark.LOG.error("[Custom Emotes] This is an Internal Error, and not one in the emote file, please report it", e);
		}
		else Quark.LOG.error("[Custom Emotes] " + e.getMessage());
	}

	private Timeline handle(ModelBiped model, EntityPlayer player, Timeline timeline, String s) throws IllegalArgumentException {
		s = s.trim().replaceAll("[^a-zA-Z0-9._ #}]", "");
		String[] translation = null;

		if(s.startsWith("#") || s.isEmpty())
			return timeline;

		String[] tokens = s.trim().split(" ");
		String function = tokens[0];

		if (NumberUtils.isParsable(function) && function != "0.0") {
			String time = String.valueOf((Float.parseFloat(tokens[0]) - lasttime)  * this.speed * 1000);

			translation[0] = "section";
			translation[1] = "paralell";
			timeline = functions.get(translation[0]).invoke(this, model, player, timeline, translation);

			for (int i = 1; i < 4; i++) {
				translation[0] = "move";
				switch(i) {
				case 1:
					translation[1] = part + axisconstructor + "x";
				case 2:
					translation[1] = part + axisconstructor + "y";
				case 3:
					translation[1] = part + axisconstructor + "z";
				}
				translation[2] = time;
				if (axisconstructor == "_off_") {
					translation[3] = String.valueOf(Float.parseFloat(tokens[i]) * (1.8 / 32));
				} else {
					translation[3] = tokens[i];
				}
				timeline = functions.get(translation[0]).invoke(this, model, player, timeline, translation);
			}
			translation = null;
			translation[0] = "end";
			return functions.get(translation[0]).invoke(this, model, player, timeline, translation);
		} else if (function == "}") {
			translation[0] = "end";
			return functions.get(translation[0]).invoke(this, model, player, timeline, translation);
		} else if(tweenables.containsKey(function)) {
			part = function.toLowerCase();
			translation[0] = "section";
			translation[1] = "paralell";
			return functions.get(translation[0]).invoke(this, model, player, timeline, translation);
		} else if (function == "bones") {
			translation[0] = "animation";
			translation[1] = "paralell";
			return functions.get(translation[0]).invoke(this, model, player, timeline, translation);
		} else if (function == "rotation") {
			axisconstructor = "_";
			lasttime = 0;
			lasttarget[1] = "0";
			lasttarget[2] = "0";
			lasttarget[3] = "0";
			translation[0] = "section";
			translation[1] = "sequence";
			return functions.get(translation[0]).invoke(this, model, player, timeline, translation);
		} else if (function == "position") {
			axisconstructor = "_off_";
			lasttime = 0;
			lasttarget[1] = "0";
			lasttarget[2] = "0";
			lasttarget[3] = "0";
			translation[0] = "section";
			translation[1] = "sequence";
			return functions.get(translation[0]).invoke(this, model, player, timeline, translation);
		}

		return timeline;
	}

	@Override
	void setName(String[] tokens) {
		StringBuilder builder = new StringBuilder();
		for(int i = 1; i < tokens.length; i++) {
			builder.append(tokens[i]);
			builder.append(" ");
		}

		name =  builder.toString().trim();
	}

	public String getName() {
		return name;
	}

	private static Timeline use(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(em.compiledOnce)
			return timeline;

		assertParamSize(tokens, 2);

		String part = tokens[1];

		if(parts.containsKey(part))
			em.usedParts.add(parts.get(part));
		else throw new IllegalArgumentException("Illegal part name for function use: " + part);

		return timeline;
	}

	private static Timeline animation(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline != null)
			throw new IllegalArgumentException("Illegal use of function animation, animation already started");

		assertParamSize(tokens, 2);

		String type = tokens[1];

		Timeline newTimeline;

		switch(type) {
			case "sequence":
				newTimeline = Timeline.createSequence();
				break;
			case "parallel":
				newTimeline = Timeline.createParallel();
				break;
			default:
				throw new IllegalArgumentException("Illegal animation type: " + type);
		}

		return newTimeline;
	}

	private static Timeline section(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline == null)
			throw new IllegalArgumentException("Illegal use of function section, animation not started");
		assertParamSize(tokens, 2);

		String type = tokens[1];
		Timeline newTimeline;
		switch(type) {
			case "sequence":
				newTimeline = Timeline.createSequence();
				break;
			case "parallel":
				newTimeline = Timeline.createParallel();
				break;
			default: throw new IllegalArgumentException("Illegal section type: " + type);
		}

		em.timelineStack.push(timeline);
		return newTimeline;
	}

	private static Timeline end(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline == null)
			throw new IllegalArgumentException("Illegal use of function end, animation not started");
		assertParamSize(tokens, 1);

		if(em.timelineStack.isEmpty()) {
			em.compiled = true;
			return timeline;
		}

		Timeline poppedLine = em.timelineStack.pop();
		poppedLine.push(timeline);
		return poppedLine;
	}

	private static Timeline move(EmoteTemplate em, ModelBiped model, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline == null)
			throw new IllegalArgumentException("Illegal use of function move, animation not started");
		if(tokens.length < 4)
			throw new IllegalArgumentException(String.format("Illegal parameter amount for function move: %d (at least 4 are required)", tokens.length));

		String partStr = tokens[1];
		int part;
		if(tweenables.containsKey(partStr))
			part = tweenables.get(partStr);
		else throw new IllegalArgumentException("Illegal part name for function move: " + partStr);

		float time = Float.parseFloat(tokens[2]) * em.speed;
		float target = Float.parseFloat(tokens[3]);

		Tween tween = null;
		boolean valid = model != null;
		if(valid)
			tween = Tween.to(model, part, time).target(target);
		if(tokens.length > 4) {
			int index = 4;
			while(index < tokens.length) {
				String cmd = tokens[index++];
				int times;
				float delay;
				switch(cmd) {
					case "delay":
						assertParamSize("delay", tokens, 1, index);
						delay = Float.parseFloat(tokens[index++]) * em.speed;
						if(valid)
							tween = tween.delay(delay);
						break;
					case "yoyo":
						assertParamSize("yoyo", tokens, 2, index);
						times = Integer.parseInt(tokens[index++]);
						delay = Float.parseFloat(tokens[index++]) * em.speed;
						if(valid)
							tween = tween.repeatYoyo(times, delay);
						break;
					case "repeat":
						assertParamSize("repeat", tokens, 2, index);
						times = Integer.parseInt(tokens[index++]);
						delay = Float.parseFloat(tokens[index++]) * em.speed;
						if(valid)
							tween = tween.repeat(times, delay);
						break;
					case "ease":
						assertParamSize("ease", tokens, 1, index);
						String easeType = tokens[index++];
						if(equations.containsKey(easeType)) {
							if(valid) tween.ease(equations.get(easeType));
						} else throw new IllegalArgumentException("Easing type " + easeType + " doesn't exist");
						break;
					default:
						throw new IllegalArgumentException(String.format("Invalid modifier %s for move function", cmd));
				}
			}
		}

		if(valid)
			return timeline.push(tween);
		return timeline;
	}

	private static Timeline pause(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		assertParamSize(tokens, 2);
		float ms = Float.parseFloat(tokens[1]) * em.speed;
		return timeline.pushPause(ms);
	}

	private static void assertParamSize(String[] tokens, int expect) throws IllegalArgumentException {
		if(tokens.length != expect)
			throw new IllegalArgumentException(String.format("Illegal parameter amount for function %s: %d (expected %d)", tokens[0], tokens.length, expect));
	}

	private static void assertParamSize(String[] tokens, int expectMin, int expectMax) throws IllegalArgumentException {
		if(tokens.length > expectMax || tokens.length < expectMin)
			throw new IllegalArgumentException(String.format("Illegal parameter amount for function %s: %d (expected between %d and %d)", tokens[0], tokens.length, expectMin, expectMax));
	}

	private static void assertParamSize(String mod, String[] tokens, int expect, int startingFrom) throws IllegalArgumentException {
		if(tokens.length - startingFrom < expect)
			throw new IllegalArgumentException(String.format("Illegal parameter amount for move modifier %s: %d (expected at least %d)", mod, tokens.length, expect));
	}

	@Override
	public boolean usesBodyPart(int part) {
		return usedParts.contains(part);
	}

	private interface Function {
		Timeline invoke(EmoteTemplate em, ModelBiped model, EntityPlayer player, Timeline timeline, String[] tokens) throws IllegalArgumentException;
	}

}

