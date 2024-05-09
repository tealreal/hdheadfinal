package teal.hdhead;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import teal.hdhead.commands.config.*;
import teal.hdhead.util.argument.URLArgumentType;
import teal.hdhead.commands.CreateHead;
import teal.hdhead.commands.DecompileHead;
import teal.hdhead.commands.Help;
import teal.hdhead.util.argument.StringArgumentTypePlus;
import teal.hdhead.util.suggestion.AdReGe;
import teal.hdhead.util.suggestion.GeCh;
import teal.hdhead.util.suggestion.Removals;


public final class HDHeads implements ClientModInitializer {

    private static final ConfigObject config = ConfigObject.getConfigObject();
    public static final Logger logger = LoggerFactory.getLogger(HDHeads.class);
    // public static final HashMap<String, PlayerSkinTexture> textureCache = new HashMap<>();

    private static boolean runMod = true;
    public static boolean doRunMod() {
        return runMod;
    }
    public static void setRunMod(boolean doRun) { runMod = doRun; }

    public static ConfigObject getConfig() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        logger.info("Initializing HD Heads by xTeal.");
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("hdheads")
                .then(ClientCommandManager.literal("create")
                        .then(ClientCommandManager.argument("url", URLArgumentType.url())
                                .executes(CreateHead.get())
                                .then(ClientCommandManager.argument("name", StringArgumentTypePlus.paragraph())
                                        .executes(CreateHead.get())
                                )
                        )
                )
                .then(ClientCommandManager.literal("decompile").executes(DecompileHead.get()))
                .then(ClientCommandManager.literal("config")
                        .then(ClientCommandManager.literal("SITE")
                                .then(ClientCommandManager.argument("site_type", StringArgumentTypePlus.string()).suggests(Site.getSiteType())
                                        .then(ClientCommandManager.argument("method", StringArgumentTypePlus.string()).suggests(AdReGe.get())
                                                .executes(Site.get())
                                                .then(ClientCommandManager.argument("sites", StringArgumentTypePlus.string()).suggests(Removals.get(Removals.RemovalType.SITES))
                                                        .executes(Site.get())
                                                )
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("SCHEME")
                                .then(ClientCommandManager.argument("method", StringArgumentTypePlus.string()).suggests(AdReGe.get())
                                        .executes(Scheme.get())
                                        .then(ClientCommandManager.argument("schemes", StringArgumentTypePlus.string()).suggests(Removals.get(Removals.RemovalType.SCHEMES))
                                                .executes(Scheme.get())
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("TOGGLE")
                                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string())
                                        .suggests(GeCh.get())
                                        .executes(Toggle.get(Toggle.Type.TOGGLE))
                                )
                        )
                        .then(ClientCommandManager.literal("MERGE")
                                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string())
                                        .suggests(GeCh.get())
                                        .executes(Toggle.get(Toggle.Type.MERGE))
                                )
                        )
                        .then(ClientCommandManager.literal("HASH")
                                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string())
                                        .suggests(GeCh.get())
                                        .executes(Toggle.get(Toggle.Type.HASH))
                                )
                        )
                        .then(ClientCommandManager.literal("SCALE")
                                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string()).suggests(GeCh.get())
                                        .executes(Scale.get())
                                        .then(ClientCommandManager.argument("x_scale", IntegerArgumentType.integer())
                                                .then(ClientCommandManager.argument("y_scale", IntegerArgumentType.integer())
                                                        .then(ClientCommandManager.argument("z_scale", IntegerArgumentType.integer())
                                                                .executes(Scale.get())
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("MAXFILESIZE")
                                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string())
                                        .suggests(GeCh.get())
                                        .executes(FileSize.get())
                                        .then(ClientCommandManager.argument("mb", LongArgumentType.longArg(-1))
                                                .executes(FileSize.get())
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("MAXIMGSIZE")
                                .then(ClientCommandManager.argument("event", StringArgumentTypePlus.string())
                                        .suggests(GeCh.get())
                                        .executes(ImgSize.get())
                                        .then(ClientCommandManager.argument("size", IntegerArgumentType.integer(-1))
                                                .executes(ImgSize.get())
                                        )
                                )
                        )
                )
                .then(ClientCommandManager.literal("help")
                        .then(ClientCommandManager.literal("create")
                                .executes(Help.get(Help.Guide.Create))
                        )
                        .then(ClientCommandManager.literal("decompile")
                                .executes(Help.get(Help.Guide.Decompile))
                        )
                        .then(ClientCommandManager.literal("configsite")
                                .executes(Help.get(Help.Guide.ConfigSite))
                        )
                        .then(ClientCommandManager.literal("configtoggle")
                                .executes(Help.get(Help.Guide.ConfigToggle))
                        )
                        .then(ClientCommandManager.literal("configmerge")
                                .executes(Help.get(Help.Guide.ConfigMerge))
                        )
                        .then(ClientCommandManager.literal("confighash")
                                .executes(Help.get(Help.Guide.ConfigHash))
                        )
                        .then(ClientCommandManager.literal("configscale")
                                .executes(Help.get(Help.Guide.ConfigScale))
                        )
                        .then(ClientCommandManager.literal("configfilesize")
                                .executes(Help.get(Help.Guide.ConfigFileSize))
                        )
                        .then(ClientCommandManager.literal("configimgsize")
                                .executes(Help.get(Help.Guide.ConfigImgSize))
                        )
                        .then(ClientCommandManager.literal("configscheme")
                                .executes(Help.get(Help.Guide.ConfigScheme))
                        )
                )
                .executes((ctx) -> 0)
        )));
    }
}