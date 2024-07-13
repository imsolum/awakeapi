package sol.awakeapi.keybindings;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class AwakeApiKeybindings {
    public static KeyBinding SPEAK_TO_MOB;
    public static KeyBinding DISPLAY_MESSAGES;

    public static void initialise() {
        SPEAK_TO_MOB = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.awake.speak_to_mob",
                GLFW.GLFW_KEY_UNKNOWN,
                "category.awake"
        ));

        DISPLAY_MESSAGES = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.awake.display_messages",
                GLFW.GLFW_KEY_UNKNOWN,
                "category.awake"
        ));
    }
}
