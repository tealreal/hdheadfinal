package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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

public interface Scheme extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();
        ConfigObject obj = HDHeads.getConfig();
        List<String> use = new LinkedList<>(Arrays.asList(obj.getSchemes()));
        String[] schemes = {};

        try {
            schemes = StringArgumentTypePlus.getString(context, "schemes").split(";+");
        } catch (IllegalArgumentException IAE) {
            if(StringArgumentTypePlus.getString(context, "method").equals("REMOVE") || StringArgumentTypePlus.getString(context, "method").equals("ADD")) {
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
                if (modded.size() == 0) {
                    throw new SimpleCommandExceptionType(new LiteralMessage("All schemes provided were not in the system.")).create();
                } else {
                    obj.setSchemes(use.toArray(new String[0]));
                    obj.write();
                    StringBuilder msg = new StringBuilder()
                            .append(Formatting.RED.toString() + Formatting.BOLD + "Removed schemes:\n")
                            .append(Formatting.GRAY.toString() + Formatting.ITALIC)
                            .append(String.join(", ", modded))
                            .append("\n\nYou may need to restart your game.");
                    if (use.size() == 0) {
                        msg.append("\n\n" + Formatting.GOLD + Formatting.BOLD + "WARNING: " + Formatting.GRAY + Formatting.ITALIC + "There are no schemes, so heads will not render.");
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
                        new URL(scheme + "://github.com");
                        if (!use.contains(scheme.toLowerCase())) {
                            use.add(scheme);
                            modded.add(scheme);
                        }
                    } catch (MalformedURLException e) {
                    }
                }
                if (modded.size() == 0) {
                    throw new SimpleCommandExceptionType(new LiteralMessage("No valid schemes detected.")).create();
                } else {
                    obj.setSchemes(use.toArray(new String[0]));
                    obj.write();
                    StringBuilder msg = new StringBuilder()
                            .append(Formatting.GREEN.toString() + Formatting.BOLD + "Added schemes:\n")
                            .append(Formatting.GRAY.toString() + Formatting.ITALIC)
                            .append(String.join(", ", modded))
                            .append("\n\nYou may need to restart your game.");
                    player.sendMessage(Text.of(msg.toString()), false);
                }
            }
            case "GET" -> {
                StringBuilder msg = new StringBuilder()
                        .append(Formatting.GREEN.toString() + Formatting.BOLD + "Allowed schemes:\n")
                        .append(Formatting.GRAY.toString() + Formatting.ITALIC);
                msg.append(String.join(", ", use));
                player.sendMessage(Text.of(msg.toString()), false);
            }
        }
        return 0;
    }

    static Scheme get() { return new Scheme() {}; }
}