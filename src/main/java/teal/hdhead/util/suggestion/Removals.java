package teal.hdhead.util.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import teal.hdhead.HeadClient;
import teal.hdhead.util.argument.StringArgumentTypePlus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Removals implements SuggestionProvider<FabricClientCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        String method = StringArgumentTypePlus.getString(context, "method");
        if (method.equals("REMOVE")) {
            String base = "";
            try {
                base = (StringArgumentTypePlus.getString(context, "sites") + ';').replaceAll(";+", ";").toLowerCase();
            } catch (IllegalArgumentException ignored) {

            }
            List<String> input = List.of(base.toLowerCase().split(";"));
            String[] use = HeadClient.getConfig().getSites(StringArgumentTypePlus.getString(context, "site_type").equals("WHITELIST"));
            for (String s : use) {
                if (input.contains(s.toLowerCase())) continue;
                builder.suggest(base + s);
            }
        }
        return builder.buildFuture();
    }

    public static Removals get() {
        return new Removals();
    }
}