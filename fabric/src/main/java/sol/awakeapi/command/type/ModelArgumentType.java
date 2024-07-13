package sol.awakeapi.command.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

import java.util.Arrays;
import java.util.Collection;

public class ModelArgumentType implements ArgumentType<String> {

    public static ModelArgumentType model() {
        return new ModelArgumentType();
    }

    public static String getModel(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(final StringReader reader) {
        StringBuilder result = new StringBuilder();
        while (reader.canRead() && reader.peek() != ' ') {
            result.append(reader.read());
        }
        return result.toString();
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList("meta-llama/Llama-3-70b-chat-hf", "example-model");
    }
}
