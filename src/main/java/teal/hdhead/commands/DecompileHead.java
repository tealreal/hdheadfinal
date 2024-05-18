package teal.hdhead.commands;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import teal.hdhead.HeadClient;
import teal.hdhead.config.ConfigObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public interface DecompileHead extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();
        ItemStack itemStack = player.getMainHandStack();
        try {
            if (itemStack.isOf(Items.PLAYER_HEAD)) {
                ProfileComponent profileComponent = itemStack.get(DataComponentTypes.PROFILE);
                MutableText text = itemStack.getName().copy();
                if (profileComponent == null)
                    throw new SimpleCommandExceptionType(new LiteralMessage("Head does not have any NBT.")).create();
                try {
                    // Get the texture JSON/NBT which is encoded in B64.
                    // Skull: {"Properties": {"textures":[{"Value":"<b64>"}]}}
                    // B64:   {"textures": {"SKIN": {"url":"<url>"},"CAPE" (sometimes, never): {"url":"<url>"}}
                    Property property = MinecraftClient.getInstance().getSessionService().getPackedTextures(profileComponent.gameProfile());
                    if (property == null)
                        throw new SimpleCommandExceptionType(new LiteralMessage("Head does not have profileComponent NBT.")).create();
                    // any json/b64 errors caught by the main try-catch block
                    MinecraftTexturesPayload payload = ConfigObject.gson.fromJson(new String(Base64.getDecoder().decode(property.value()), StandardCharsets.UTF_8), MinecraftTexturesPayload.class);
                    MinecraftProfileTexture skin = payload.textures().get(MinecraftProfileTexture.Type.SKIN);
                    if (skin == null)
                        throw new SimpleCommandExceptionType(new LiteralMessage("Head does not have skin NBT.")).create();
                    text.append(Text.literal("\nURL: ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)));
                    text.append(Text.literal(skin.getUrl()).setStyle(Style.EMPTY
                        .withColor(Formatting.BLUE)
                        .withUnderline(true)
                        .withBold(false)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, skin.getUrl()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to open the texture")))
                    ));
                    UUID id = profileComponent.gameProfile().getId();
                    if (id != null) {
                        text.append(Text.literal("\n"))
                            .append(Text.literal("UUID: ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)))
                            .append(Text.literal(id.toString()).setStyle(Style.EMPTY
                                .withColor(Formatting.WHITE)
                                .withItalic(true)
                                .withBold(false)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id.toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to copy the UUID")))
                            ));
                    }
                    text.append(Text.literal("\n\n"))
                        .append(Text.literal("[Click here to copy as JSON]").setStyle(Style.EMPTY
                            .withColor(Formatting.GREEN)
                            .withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, new ItemStackArgument(itemStack.getRegistryEntry(), itemStack.getComponents()).asString(player.getWorld().getRegistryManager())))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to copy the NBT")))
                        ));
                    context.getSource().getPlayer().sendMessage(text, false);
                } catch (IllegalArgumentException IAE) {
                    throw new SimpleCommandExceptionType(new LiteralMessage("Cannot decode B64.")).create();
                }
            } else throw new SimpleCommandExceptionType(new LiteralMessage("Item must be a player head.")).create();
        } catch (NullPointerException NPE) {
            HeadClient.logger.error(NPE.getMessage());
            throw new SimpleCommandExceptionType(new LiteralMessage("Failed to decompile head.")).create();
        }
        return 0;
    }

    static DecompileHead get() {
        return new DecompileHead() {
        };
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> getCommandBuilder() {
        return ClientCommandManager.literal("decompile").executes(DecompileHead.get());
    }

}
