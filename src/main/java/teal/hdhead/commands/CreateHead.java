package teal.hdhead.commands;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import teal.hdhead.HeadClient;
import teal.hdhead.config.ConfigObject;
import teal.hdhead.mixin.TUCInvoker;
import teal.hdhead.util.Rawsay;
import teal.hdhead.util.argument.StringArgumentTypePlus;
import teal.hdhead.util.argument.URLArgumentType;

import java.net.URL;
import java.util.*;

public interface CreateHead extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        FabricClientCommandSource source = context.getSource();
        if (!source.getPlayer().isCreative())
            throw new SimpleCommandExceptionType(new LiteralMessage("You must be in creative mode.")).create();

        URL url = URLArgumentType.getURL(context, "url");
        ItemStack head = new ItemStack(Items.PLAYER_HEAD, 1);
        Property property = new Property("textures", getB64Object(url));
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put("textures", property);
        ProfileComponent profileComponent = new ProfileComponent(Optional.of(("teal." + Long.toString(System.currentTimeMillis(), 16).repeat(16)).substring(0, 16)), Optional.of(UUID.randomUUID()), propertyMap);
        head.set(DataComponentTypes.PROFILE, profileComponent);
        try {
            head.set(DataComponentTypes.CUSTOM_NAME, Rawsay.parseFormatting(StringArgumentTypePlus.getString(context, "name"), '&'));
        } catch (IllegalArgumentException ignored) {
        }

        for (int i = 0; i < 9; i++) {
            if (!source.getPlayer().getInventory().getStack(i).isEmpty()) continue;
            source.getPlayer().networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + i, head));
            source.getPlayer().sendMessage(Text.literal("Produced a new HD Head from ").append(Text.literal(url.toString()).formatted(Formatting.UNDERLINE)), false);
            if (!TUCInvoker.callIsAllowedTextureDomain(url.toString()))
                source.getPlayer().sendMessage(
                    Text.of(Formatting.GOLD.toString() +
                            Formatting.BOLD +
                            "WARNING: " +
                            Formatting.GRAY +
                            Formatting.ITALIC +
                            "You created a head with a URL that isn't " +
                            (HeadClient.doRunMod() ? "whitelisted or was blacklisted" : "available with the mod off") +
                            ", so it will not render."
                    ), false);
            return 0;
        }

        throw new SimpleCommandExceptionType(new LiteralMessage("Your hotbar is full. Clear out a slot and retry.")).create();
    }

    static String getB64Object(URL url) {
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = new HashMap<>();
        textures.put(MinecraftProfileTexture.Type.SKIN, new MinecraftProfileTexture(url.toString(), null));
        MinecraftTexturesPayload payload = new MinecraftTexturesPayload(0, null, null, false, textures);
        return Base64.getEncoder().encodeToString(ConfigObject.gson.toJson(payload).getBytes());
    }

    static CreateHead get() {
        return new CreateHead() {
        };
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> getCommandBuilder() {
        return ClientCommandManager.literal("create")
            .then(ClientCommandManager.argument("url", URLArgumentType.url())
                .executes(CreateHead.get())
                .then(ClientCommandManager.argument("name", StringArgumentTypePlus.paragraph())
                    .executes(CreateHead.get())
                )
            );
    }
}
