package teal.hdhead.util.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class GeCh implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        builder.suggest("GET");
        builder.suggest("CHANGE");
        return builder.buildFuture();
    }

    public static GeCh get() {
        return new GeCh();
    }
}
