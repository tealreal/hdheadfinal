package teal.hdhead.util.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

public class URLArgumentType implements ArgumentType<URL> {
    public static URLArgumentType url() {
        return new URLArgumentType();
    }

    public static <S> URL getURL(final CommandContext<S> context, final String name) {
        return context.getArgument(name, URL.class);
    }

    private static final Collection<String> EXAMPLES = List.of(
        "https://minecraft.net",
        "https://youtube.com",
        "https://mojang.com"
    );

    @Override
    public URL parse(StringReader reader) throws CommandSyntaxException {
        int beginning = reader.getCursor();
        if (reader.canRead()) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }

        String urlString = reader.getString().substring(beginning, reader.getCursor());

        try {
            return new URI(urlString).toURL();
        } catch (URISyntaxException | MalformedURLException MUE) {
            throw new SimpleCommandExceptionType(Text.of(MUE.getMessage())).createWithContext(reader);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
