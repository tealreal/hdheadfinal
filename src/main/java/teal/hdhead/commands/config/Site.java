package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import teal.hdhead.ConfigObject;
import teal.hdhead.HDHeads;
import teal.hdhead.util.argument.StringArgumentTypePlus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Site extends Command<FabricClientCommandSource> {

    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();
        ConfigObject obj = HDHeads.getConfig();
        boolean whitelisted = StringArgumentTypePlus.getString(context, "site_type").equals("WHITELIST");
        List<String> use = new LinkedList<>(Arrays.asList(obj.getSites(whitelisted)));
        String format = (whitelisted ? Formatting.GREEN : Formatting.RED).toString() + Formatting.BOLD;
        String[] sites = {};

        try {
            sites = StringArgumentTypePlus.getString(context, "sites").split(";+");
        } catch (IllegalArgumentException IAE) {
            if(StringArgumentTypePlus.getString(context, "method").equals("REMOVE") || StringArgumentTypePlus.getString(context, "method").equals("ADD")) {
                throw new SimpleCommandExceptionType(new LiteralMessage("Please provide URLs.")).create();
            }
        }

        List<String> modded = new LinkedList<>();
        switch (StringArgumentTypePlus.getString(context, "method")) {
            case "REMOVE" -> {
                for (String site : sites) {
                    if (use.contains(site.toLowerCase())) {
                        use.remove(site);
                        modded.add(site);
                    }
                }
                if (modded.size() == 0) {
                    throw new SimpleCommandExceptionType(new LiteralMessage("All URLs provided were not in the system.")).create();
                } else {
                    obj.setSites(use.toArray(new String[0]), whitelisted);
                    obj.write();
                    StringBuilder msg = new StringBuilder()
                            .append(format + "Removed ")
                            .append(whitelisted ? "whitelisted" : "blacklisted")
                            .append(" sites:\n" + Formatting.GRAY + Formatting.ITALIC)
                            .append(String.join(", ", modded))
                            .append("\n\nYou may need to restart your game.");
                    if (use.size() == 0) {
                        msg.append("\n\n" + Formatting.GOLD + Formatting.BOLD + "WARNING: " + Formatting.GRAY + Formatting.ITALIC + "There are no sites " + (whitelisted ? "whitelisted, so heads will not render." : "blacklisted."));
                    }
                    player.sendMessage(Text.of(msg.toString()), false);
                }
            }
            case "ADD" -> {
                for (String site : sites) {
                    if (site.equals("*") && !use.contains(site.toLowerCase())) {
                        modded.add("*");
                        use.add("*");
                    }
                    try {
                        new URL("https://" + site);
                        if (!use.contains(site.toLowerCase())) {
                            use.add(site);
                            modded.add(site);
                        }
                    } catch (MalformedURLException e) {
                    }
                }
                if (modded.size() == 0) {
                    throw new SimpleCommandExceptionType(new LiteralMessage("No valid URLs detected. Format it like '.domain.com', or 'specific.domain.com'")).create();
                } else {
                    obj.setSites(use.toArray(new String[0]), whitelisted);
                    obj.write();
                    StringBuilder msg = new StringBuilder()
                            .append(format + "Added ")
                            .append(whitelisted ? "whitelisted" : "blacklisted")
                            .append(" sites:\n" + Formatting.GRAY + Formatting.ITALIC)
                            .append(String.join(", ", modded))
                            .append("\n\nYou may need to restart your game.");
                    if (!whitelisted && modded.contains("*"))
                        msg.append("\n\n" + Formatting.GOLD + Formatting.BOLD + "WARNING: " + Formatting.GRAY + Formatting.ITALIC + "You added * to the blacklist, which will block all textures.");
                    player.sendMessage(Text.of(msg.toString()), false);
                }
            }
            case "GET" -> {
                StringBuilder msg = new StringBuilder()
                        .append(format)
                        .append(whitelisted ? "Whitelisted" : "Blacklisted")
                        .append(" sites:\n" + Formatting.GRAY + Formatting.ITALIC);
                msg.append(String.join(", ", use));
                player.sendMessage(Text.of(msg.toString()), false);
            }
        }
        return 0;
    }

    static Site get() { return new Site() {}; }
    static SiteTypeSuggester getSiteType() { return new SiteTypeSuggester(); }

}

class SiteTypeSuggester implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        builder.suggest("BLACKLIST");
        builder.suggest("WHITELIST");
        return builder.buildFuture();
    }
}