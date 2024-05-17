package teal.hdhead;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import teal.hdhead.commands.CreateHead;
import teal.hdhead.commands.DecompileHead;
import teal.hdhead.commands.Help;
import teal.hdhead.commands.config.*;
import teal.hdhead.config.ConfigObject;


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
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("hdheads")
                // Base
                .then(CreateHead.getCommandBuilder())
                .then(DecompileHead.getCommandBuilder())
                .then(Help.getCommandBuilder())
                // Configuration
                .then(Site.getCommandBuilder())
                .then(Scheme.getCommandBuilder())
                .then(Scale.getCommandBuilder())
                .then(ImgSize.getCommandBuilder())
                .then(FileSize.getCommandBuilder())
                .then(Toggle.getCommandBuilder())
                .executes((ctx) -> 0)
        )));
    }
}