package teal.hdhead;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import teal.hdhead.commands.CreateHead;
import teal.hdhead.commands.DecompileHead;
import teal.hdhead.commands.Help;
import teal.hdhead.commands.config.*;
import teal.hdhead.config.ConfigObject;

import java.util.List;


public final class HeadClient implements ClientModInitializer {

    private static final ConfigObject config = ConfigObject.getConfigObject();
    public static final Logger logger = LoggerFactory.getLogger(HeadClient.class);

    private static boolean runMod = true;

    public static boolean doRunMod() {
        return runMod;
    }

    public static void setRunMod(boolean doRun) {
        runMod = doRun;
    }

    public static ConfigObject getConfig() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        logger.info("Initializing HD Heads by xTeal.");
        for(LiteralArgumentBuilder<FabricClientCommandSource> command : List.of(
            // Base
            CreateHead.getCommandBuilder(),
            DecompileHead.getCommandBuilder(),
            Help.getCommandBuilder(),
            // Configuration
            Site.getCommandBuilder(),
            Scale.getCommandBuilder(),
            ImgSize.getCommandBuilder(),
            FileSize.getCommandBuilder(),
            Toggle.getCommandBuilder()
        )) {
            ClientCommandManager.DISPATCHER.register(
                ClientCommandManager.literal("hdheads")
                    .then(command)
            );
        }
    }
}