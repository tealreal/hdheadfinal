package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import teal.hdhead.ConfigObject;
import teal.hdhead.HDHeads;

import java.util.Arrays;

public interface Scale extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ConfigObject config = HDHeads.getConfig();
        String prefix = "current";
        switch(StringArgumentType.getString(context, "event")) {
            case "CHANGE" -> {
                try {
                    int xS = IntegerArgumentType.getInteger(context, "x_scale");
                    int yS = IntegerArgumentType.getInteger(context, "y_scale");
                    int zS = IntegerArgumentType.getInteger(context, "z_scale");
                    config.setScaleInject(new Integer[]{xS, yS, zS});
                    config.write();
                    prefix = "new";
                } catch (IllegalArgumentException IAE) {
                    throw new SimpleCommandExceptionType(Text.literal("Missing scale arguments.")).create();
                }
            }
            case "GET" -> {}
            default -> throw new SimpleCommandExceptionType(new LiteralMessage("Invalid argument given.")).create();
        }
        Integer[] scaleInject = config.getScaleInject();
        context.getSource().getPlayer().sendMessage(Text.of(Formatting.GRAY + "The " + prefix + " scale is " + String.join(Formatting.GRAY + ", ", Arrays.stream(scaleInject).map((l) -> String.valueOf(Formatting.GREEN) + Formatting.BOLD + l).toList())), false);
        return 0;
    }

    static Scale get() {
        return new Scale() {};
    }
}
