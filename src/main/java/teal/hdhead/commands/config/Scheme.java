package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import teal.hdhead.HeadClient;
import teal.hdhead.config.ConfigObject;
import teal.hdhead.util.argument.StringArgumentTypePlus;
import teal.hdhead.util.suggestion.AdReGe;
import teal.hdhead.util.suggestion.Removals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public interface Scheme extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();
        ConfigObject obj = HeadClient.getConfig();
        List<String> use = new LinkedList<>(Arrays.asList(obj.getSchemes()));
        String[] schemes = {};

        try {
            schemes = StringArgumentTypePlus.getString(context, "schemes").split(";+");
        } catch (IllegalArgumentException IAE) {
            if (StringArgumentTypePlus.getString(context, "method").equals("REMOVE") || StringArgumentTypePlus.getString(context, "method").equals("ADD")) {
                throw new SimpleCommandExceptionType(new LiteralMessage("Please provide URL schemes.")).create();
            }
        }

        List<String> modded = new LinkedList<>();
        switch (StringArgumentTypePlus.getString(context, "method")) {
            case "REMOVE" -> {
                for (String scheme : schemes) {
                    if (use.contains(scheme.toLowerCase())) {
                        use.remove(scheme);
                        modded.add(scheme);
                    }
                }
                if (modded.isEmpty()) {
                    throw new SimpleCommandExceptionType(new LiteralMessage("All schemes provided were not in the system.")).create();
                } else {
                    obj.setSchemes(use.toArray(new String[0]));
                    obj.write();
                    StringBuilder msg = new StringBuilder()
                        .append(Formatting.RED).append(Formatting.BOLD)
                            .append("Removed schemes:\n")
                        .append(Formatting.GRAY).append(Formatting.ITALIC)
                            .append(String.join(", ", modded))
                            .append("\n\nYou may need to restart your game.");
                    if (use.isEmpty()) {
                        msg
                            .append("\n\n")
                            .append(Formatting.GOLD).append(Formatting.BOLD)
                                .append("WARNING: ")
                            .append(Formatting.GRAY).append(Formatting.ITALIC)
                                .append("There are no schemes, so heads will not render.");
                    }
                    player.sendMessage(Text.of(msg.toString()), false);
                }
            }
            case "ADD" -> {
                for (String scheme : schemes) {
                    if (scheme.equals("*") && !use.contains(scheme.toLowerCase())) {
                        modded.add("*");
                        use.add("*");
                    }
                    try {
                        new URI(scheme + "://github.com");
                        if (!use.contains(scheme.toLowerCase())) {
                            use.add(scheme);
                            modded.add(scheme);
                        }
                    } catch (URISyntaxException ignored) {
                    }
                }
                if (modded.isEmpty()) {
                    throw new SimpleCommandExceptionType(new LiteralMessage("No valid schemes detected.")).create();
                } else {
                    obj.setSchemes(use.toArray(new String[0]));
                    obj.write();
                    String msg = Formatting.GREEN.toString() + Formatting.BOLD + "Added schemes:\n" +
                                 Formatting.GRAY + Formatting.ITALIC +
                                 String.join(", ", modded) +
                                 "\n\nYou may need to restart your game.";
                    player.sendMessage(Text.of(msg), false);
                }
            }
            case "GET" -> {
                String msg = String.valueOf(Formatting.GREEN) + Formatting.BOLD +
                             "Allowed schemes:\n" +
                             Formatting.GRAY + Formatting.ITALIC +
                             String.join(", ", use);
                player.sendMessage(Text.of(msg), false);
            }
        }
        return 0;
    }

    static Scheme get() {
        return new Scheme() {
        };
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> getCommandBuilder() {
        return ClientCommandManager.literal("config").then(
            ClientCommandManager.literal("SCHEME")
                .then(ClientCommandManager.argument("method", StringArgumentTypePlus.string()).suggests(AdReGe.get())
                    .executes(Scheme.get())
                    .then(ClientCommandManager.argument("schemes", StringArgumentTypePlus.string()).suggests(Removals.get(Removals.RemovalType.SCHEMES))
                        .executes(Scheme.get())
                    )
                )

        );
    }
}