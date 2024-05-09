package teal.hdhead.util.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import teal.hdhead.HDHeads;
import teal.hdhead.util.argument.StringArgumentTypePlus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Removals implements SuggestionProvider<FabricClientCommandSource> {
    private final RemovalType removalType;

    public Removals(RemovalType removalType) {
        this.removalType = removalType;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        String method = StringArgumentTypePlus.getString(context, "method");
        if(method.equals("REMOVE")) {
            String base = "";
            try {
                base = (StringArgumentTypePlus.getString(context, this.removalType.string) + ';').replaceAll(";+", ";").toLowerCase();
            } catch (IllegalArgumentException IAE) {

            }
            List<String> input = List.of(base.toLowerCase().split(";"));
            String[] use = switch(this.removalType) {
                case SITES -> HDHeads.getConfig().getSites(StringArgumentTypePlus.getString(context, "site_type").equals("WHITELIST"));
                case SCHEMES -> HDHeads.getConfig().getSchemes();
            };
            for (String s : use) {
                if (input.contains(s.toLowerCase())) continue;
                builder.suggest(base + s);
            }
        }
        return builder.buildFuture();
    }

    public static Removals get(RemovalType removalType) {
        return new Removals(removalType);
    }

    public enum RemovalType {
        SITES("sites"),
        SCHEMES("schemes");

        final String string;

        RemovalType(String type) {
            this.string = type;
        }
    }
}