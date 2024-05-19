package teal.hdhead.util.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class AdReGe implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        builder.suggest("REMOVE");
        builder.suggest("ADD");
        builder.suggest("GET");
        return builder.buildFuture();
    }

    public static AdReGe get() {
        return new AdReGe() {
        };
    }
}