package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import teal.hdhead.HeadClient;
import teal.hdhead.util.argument.StringArgumentTypePlus;
import teal.hdhead.util.suggestion.GeCh;

public interface ImgSize extends Command<FabricClientCommandSource> {

    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String prefix = "M";
        int size = HeadClient.getConfig().getMaxDimension();
        switch (StringArgumentType.getString(context, "event")) {
            case "CHANGE" -> {
                size = IntegerArgumentType.getInteger(context, "size");
                HeadClient.getConfig().setMaxDimension(size);
                HeadClient.getConfig().write();
                prefix = "The new m";
            }
            case "GET" -> {
            }
            default -> throw new SimpleCommandExceptionType(new LiteralMessage("Invalid argument given.")).create();
        }
        context.getSource().getPlayer().sendMessage(Text.of(Formatting.GRAY + prefix + "ax dimension size for new incoming images is " + Formatting.GREEN + Formatting.BOLD + (size == -1 ? "none" : size + " pixels" + Formatting.GRAY + ".")), false);
        return 0;
    }

    static ImgSize get() {
        return new ImgSize() {
        };
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> getCommandBuilder() {
        return ClientCommandManager.literal("config").then(
            ClientCommandManager.literal("MAXIMGSIZE")
                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string())
                    .suggests(GeCh.get())
                    .executes(ImgSize.get())
                    .then(ClientCommandManager.argument("size", IntegerArgumentType.integer(-1))
                        .executes(ImgSize.get())
                    )
                )

        );
    }
}

