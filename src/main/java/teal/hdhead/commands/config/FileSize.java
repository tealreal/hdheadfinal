package teal.hdhead.commands.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import teal.hdhead.HDHeads;

public interface FileSize extends Command<FabricClientCommandSource> {

    int MB = 1000000;

    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String prefix = "M";
        long mb = HDHeads.getConfig().getThresholdSize();
        switch(StringArgumentType.getString(context, "event")) {
            case "CHANGE" -> {
                mb = LongArgumentType.getLong(context, "mb") * MB;
                HDHeads.getConfig().setThresholdSize(mb < 0 ? -1 : mb);
                HDHeads.getConfig().write();
                prefix = "The new m";
            }
            case "GET" -> {}
            default -> throw new SimpleCommandExceptionType(new LiteralMessage("Invalid argument given.")).create();
        }
        context.getSource().getPlayer().sendMessage(Text.of(Formatting.GRAY + prefix + "ax file size for new incoming images is " + Formatting.GREEN + Formatting.BOLD + (mb < 0 ? "none" : String.format("%.2f MB" + Formatting.GRAY + ".", (double) mb/MB))), false);
        return 0;
    }

    static FileSize get() { return new FileSize() {}; }
}