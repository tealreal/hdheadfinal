package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static teal.hdhead.HDHeads.*;

public interface Toggle extends Command<FabricClientCommandSource> {

    Type getType();

    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        boolean check;
        String type;
        switch(getType()) {
            case TOGGLE -> {
                check = doRunMod();
                type = "HD Heads functionality";
            }
            case MERGE -> {
                check = getConfig().isMerge();
                type = "texture merging";
            }
            case HASH -> {
                check = getConfig().isHash();
                type = "vanilla hashing";
            }
            default -> throw new SimpleCommandExceptionType(new LiteralMessage("Toggle not chosen")).create();
        }
        switch(StringArgumentType.getString(context, "event")) {
            case "CHANGE" -> {
                context.getSource().getPlayer().sendMessage(Text.literal("Turned o" + (check ? "ff " : "n ") + type + "."), true);
                switch(getType()) {
                    case TOGGLE -> setRunMod(!check);
                    case MERGE -> getConfig().setMerge(!check);
                    case HASH -> getConfig().setHash(!check);
                }
            }
            case "GET" -> context.getSource().getPlayer().sendMessage(Text.literal(type.substring(0,1).toUpperCase() + type.substring(1) + " is " + (check ? Formatting.GREEN.toString() + Formatting.BOLD + "ON" : Formatting.RED.toString() + Formatting.BOLD + "OFF")), true);
            default -> throw new SimpleCommandExceptionType(new LiteralMessage("Invalid argument given.")).create();
        }
        return 0;
    }

    static Toggle get(Type type) {
        return () -> type;
    }

    enum Type {
        TOGGLE,
        MERGE,
        HASH
    }

}