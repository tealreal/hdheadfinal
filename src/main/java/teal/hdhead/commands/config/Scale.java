package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
import teal.hdhead.config.ConfigObject;
import teal.hdhead.util.argument.StringArgumentTypePlus;
import teal.hdhead.util.suggestion.GeCh;

import java.util.Arrays;

public interface Scale extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ConfigObject config = HeadClient.getConfig();
        String prefix = "current";
        switch (StringArgumentType.getString(context, "event")) {
            case "CHANGE" -> {
                try {
                    float xS = FloatArgumentType.getFloat(context, "x_scale");
                    float yS = FloatArgumentType.getFloat(context, "y_scale");
                    float zS = FloatArgumentType.getFloat(context, "z_scale");
                    config.setScaleInject(new Float[]{xS, yS, zS});
                    config.write();
                    prefix = "new";
                } catch (IllegalArgumentException IAE) {
                    throw new SimpleCommandExceptionType(Text.literal("Missing scale arguments.")).create();
                }
            }
            case "GET" -> {
            }
            default -> throw new SimpleCommandExceptionType(new LiteralMessage("Invalid argument given.")).create();
        }
        Float[] scaleInject = config.getScaleInject();
        context.getSource().getPlayer().sendMessage(Text.of(Formatting.GRAY + "The " + prefix + " scale is " + String.join(Formatting.GRAY + ", ", Arrays.stream(scaleInject).map((l) -> String.valueOf(Formatting.GREEN) + Formatting.BOLD + l).toList())), false);
        return 0;
    }

    static Scale get() {
        return new Scale() {
        };
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> getCommandBuilder() {
        return ClientCommandManager.literal("config").then(
            ClientCommandManager.literal("SCALE")
                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string()).suggests(GeCh.get())
                    .executes(Scale.get())
                    .then(ClientCommandManager.argument("x_scale", FloatArgumentType.floatArg())
                        .then(ClientCommandManager.argument("y_scale", FloatArgumentType.floatArg())
                            .then(ClientCommandManager.argument("z_scale", FloatArgumentType.floatArg())
                                .executes(Scale.get())
                            )
                        )
                    )
                )

        );
    }
}
