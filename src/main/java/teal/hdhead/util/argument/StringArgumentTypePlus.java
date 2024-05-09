package teal.hdhead.util.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

import java.util.Collection;
import java.util.List;

public class StringArgumentTypePlus implements ArgumentType<String> {
    private final boolean ignoreSpace;

    private StringArgumentTypePlus(boolean ignoreSpace) {
        this.ignoreSpace = ignoreSpace;
    }

    public static StringArgumentTypePlus string() {
        return new StringArgumentTypePlus(false);
    }

    public static StringArgumentTypePlus paragraph() {
        return new StringArgumentTypePlus(true);
    }

    public static <S> String getString(final CommandContext<S> context, final String name) {
        return context.getArgument(name, String.class);
    }

    private static final Collection<String> EXAMPLES = List.of(
            "hi",
            "hello",
            "ok"
    );

    @Override
    public String parse(StringReader reader) {
        int beginning = reader.getCursor();

        if (reader.canRead()) {
            reader.skip();
        }

        while (reader.canRead() && (reader.peek() != ' ' || ignoreSpace)) {
            reader.skip();
        }

        return reader.getString().substring(beginning, reader.getCursor());
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
