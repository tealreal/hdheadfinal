package teal.hdhead.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface Help extends Command<FabricClientCommandSource> {

    @Override
    default int run(CommandContext<FabricClientCommandSource> context) {
        context.getSource().getPlayer().sendMessage(getType().message, false);
        return 0;
    }

    Guide getType();

    static Help get(Guide type) {
        return () -> type;
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> getCommandBuilder() {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal("help");
        for (Guide guide : Guide.values())
            builder = builder.then(
                ClientCommandManager.literal(guide.name()).executes(Help.get(guide))
            );
        return builder;
    }

    enum Guide {
        Create(
            "create <url> [<name>] [<watermark>]",
            """
                Creates a new head using the specified parameters:
                • <url> is an image URL. The image should look similar to a Minecraft skin.
                • [<name>] is the name of the head. Supports color and styling and HEX (&f, &l, &#FFFFFF) through the ampersand character. (rawsay)
                • [<watermark>] is the "fingerprint" of the head, it must be a unique value or it may not render.""",
            false
        ),
        Decompile(
            "decompile",
            """
                When you are holding a head, this will show the key NBT of the head including the URL, UUID, and watermark of the head.""",
            false
        ),
        ConfigSite(
            "config SITE <BLACKLIST/WHITELIST> <ADD/REMOVE/GET> [<sites>]",
            """
                Configures the url hosts to blacklist and whitelist with the specified parameters:
                • <BLACKLIST/WHITELIST> is only relevant for whitelist, as any domains not detected on whitelist will be ignored. blacklist can be used to block a subdomain. Use an asterisk (*) to allow or deny all sites.
                • <ADD/REMOVE/GET> decides how you modify the list.
                • [<sites>] are the sites you want to add or remove, to add multiple, separate using a semicolon (;).
                
                EXAMPLES OF A VALID SITE: .mojang.com  .minecraft.net  education.minecraft.net""",
            false
        ),
        ConfigToggle(
            "config TOGGLE <CHANGE/GET>",
            """
                Toggles the functionality of HD Heads:
                • CHANGE toggles by switching out the blacklisted and whitelisted URLs, and disables rendering of HD Heads.
                • GET shows the status of the mod.""",
            false
        ),
        ConfigMerge(
            "config MERGE <CHANGE/GET>",
            """
                Toggles texture merging of HD Heads. This is off by default (vanilla behavior would be on), works by moving the textures by such a small amount that you will only notice that the textures of the head won't merge into the block it's on, also fixing wall heads. In order for this to work, config TOGGLE must be on.
                • CHANGE toggles the modification that prevents player head textures from merging with the block it is placed on.
                • GET shows the status of texture merging.""",
            false
        ),
        ConfigHash(
            "config HASH <CHANGE/GET>",
            """
                Modifies the file naming procedure to allow URLs with the same filename to go to different heads. In order for this to work, config TOGGLE must be on. Toggling between options will download textures you've already downloaded.
                • CHANGE toggles what string is used for hashing: the whole URL or just the filename.
                • GET shows the status of how textures are saved.""",
            true
        ),
        ConfigShrink(
            "config SHRINK <CHANGE/GET>",
            """
                Changes how the hat layer on heads are rendered. In order for this to work, config TOGGLE must be on. If enabled, the hat layer will be shrunk to the size of the head layer to allow more immersion with textures like laptop heads. [Reload resources after changing]
                • CHANGE toggles how the hat layer on heads are rendered.
                • GET shows the status of how the hat layer is rendered.""",
            false
        ),
        ConfigScale(
            "config SCALE <CHANGE/GET> [<x_scale> <y_scale> <z_scale>]",
            """
                Sets the scale at which player heads are rendered, intended to be a 'fun' command, large values will negatively impact user experience. The default is -1 -1 1, affecting all heads.
                • CHANGE sets the scale for x, y, and z respectively.
                • GET shows the current scale factor""",
            true
        ),
        ConfigFileSize(
            "config MAXFILESIZE <CHANGE/GET> [<kb>]",
            """
                Sets the maximum allowed file size for incoming and already downloaded textures. The default is 50 MB.
                • CHANGE will allow you to set a size in KILOBYTES, not BYTES. Put -1 to allow images of any size.
                • GET shows the maximum size allowed for rendering HD Heads.""",
            false
        ),
        ConfigImgSize(
            "config MAXIMGSIZE <CHANGE/GET> [<size>]",
            """
                Sets the maximum allowed dimensions for incoming and already downloaded textures. The default is 50000 pixels.
                • CHANGE will allow you to set a size in PIXELS. Put -1 to allow images of any size. This will be checked against both the image height and width.
                • GET shows the maximum size allowed for rendering HD Heads.""",
            false
        ),
        ConfigScheme(
            "config SCHEME <ADD/REMOVE/GET> [<schemes>]",
            """
                Configures the allowed url scheme with the specified parameters:
                • <ADD/REMOVE/GET> decides how you modify the list. Use an asterisk (*) to allow all schemes.
                • [<schemes>] are the schemes you want to add or remove, to add multiple, separate using a semicolon (;).
                
                EXAMPLES OF A VALID SCHEME: https, http""",
            true
        );

        public final MutableText message;

        Guide(String header, String contents, boolean advanced) {
            message = Text.literal(header + "\n\n").setStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(true))
                .append(Text.literal(contents).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true).withBold(false)));
            if (advanced)
                message.append(Text.literal("\nIt is not recommended to use this command unless you know what you are doing.").setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true).withItalic(true)));
        }
    }
}