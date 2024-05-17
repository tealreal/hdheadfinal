package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.LongArgumentType;
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

public interface FileSize extends Command<FabricClientCommandSource> {

    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String prefix = "M";
        long kb = HeadClient.getConfig().getThresholdSizeInKilobytes();
        switch (StringArgumentType.getString(context, "event")) {
            case "CHANGE" -> {
                kb = LongArgumentType.getLong(context, "kb");
                HeadClient.getConfig().setThresholdSize(kb < 0 ? -1 : kb);
                HeadClient.getConfig().write();
                prefix = "The new m";
            }
            case "GET" -> {
            }
            default -> throw new SimpleCommandExceptionType(new LiteralMessage("Invalid argument given.")).create();
        }
        context.getSource().getPlayer().sendMessage(Text.of(Formatting.GRAY + prefix + "ax file size for new incoming images is " + Formatting.GREEN + Formatting.BOLD + (kb < 0 ? "none" : String.format("%.2f KB" + Formatting.GRAY + ".", (double) kb))), false);
        return 0;
    }

    static FileSize get() {
        return new FileSize() {
        };
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> getCommandBuilder() {
        return ClientCommandManager.literal("config").then(
            ClientCommandManager.literal("MAXFILESIZE")
                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string())
                    .suggests(GeCh.get())
                    .executes(FileSize.get())
                    .then(ClientCommandManager.argument("kb", LongArgumentType.longArg(-1))
                        .executes(FileSize.get())
                    )
                )

        );
    }
}