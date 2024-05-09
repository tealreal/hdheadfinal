package teal.hdhead.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.UUID;

public interface DecompileHead extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();
        ItemStack itemStack = player.getMainHandStack();
        try {
            if (itemStack.isOf(Items.PLAYER_HEAD)) {
                NbtCompound nbt = itemStack.getNbt();
                NbtCompound skullOwner = nbt.getCompound("SkullOwner");
                try {
                    // Get the texture JSON/NBT which is encoded in B64.
                    // Skull: {"Properties": {"textures":[{"Value":"<b64>"}]}}
                    // B64:   {"textures": {"SKIN": {"url":"<url>"},"CAPE" (sometimes, never): {"url":"<url>"}}
                    NbtCompound obj = StringNbtReader.parse(
                            new String(Base64.getDecoder().decode(
                                    skullOwner.getCompound("Properties")
                                            .getList("textures", NbtList.COMPOUND_TYPE)
                                            .getCompound(0)
                                            .getString("Value")
                            ))
                    );
                    // Code written using cocaine
                    String url = obj.getCompound("textures").getCompound("SKIN").getString("url");
                    MutableText text = itemStack.getName().copy();
                    text.append(Text.literal("\nURL: ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)));
                    text.append(Text.literal(url).setStyle(Style.EMPTY
                            .withColor(Formatting.BLUE)
                            .withUnderline(true)
                            .withBold(false)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to open the texture")))
                    ));
                    UUID id = getUUID(nbt, uuidType.ID);
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
                    UUID or = getUUID(nbt, uuidType.SKULLOWNERORIG);
                    if (or != null) {
                        text.append(Text.literal("\n"))
                            .append(Text.literal("Original UUID: ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)))
                            .append(Text.literal(or.toString()).setStyle(Style.EMPTY
                                    .withColor(Formatting.WHITE)
                                    .withItalic(true)
                                    .withBold(false)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, or.toString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to copy the original UUID")))
                            ));
                    }
                    if (!skullOwner.getString("Name").isEmpty()) {
                        text.append(Text.literal("\n"))
                            .append(Text.literal("Watermark: ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)))
                            .append(Text.literal(skullOwner.getString("Name")).setStyle(Style.EMPTY
                                    .withColor(Formatting.WHITE)
                                    .withItalic(true)
                                    .withBold(false)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, skullOwner.getString("Name")))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to copy the watermark")))
                            ));
                    }
                    text.append(Text.literal("\n\n"))
                        .append(Text.literal("[Click here to copy as JSON]").setStyle(Style.EMPTY
                                .withColor(Formatting.GREEN)
                                .withBold(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.asString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to copy the NBT")))
                        ));
                    context.getSource().getPlayer().sendMessage(text, false);
                } catch (IllegalArgumentException IAE) {
                    throw new SimpleCommandExceptionType(new LiteralMessage("Cannot decode B64.")).create();
                }
            } else throw new SimpleCommandExceptionType(new LiteralMessage("Item must be a player head.")).create();
        } catch(NullPointerException NPE) {
            throw new SimpleCommandExceptionType(new LiteralMessage("Failed to decompile head.")).create();
        }
        return 0;
    }

    static DecompileHead get() { return new DecompileHead() {}; }

    @Nullable
    static UUID getUUID(NbtCompound nbt, uuidType type) {
        String property = type == uuidType.ID ? "SkullOwner" : "SkullOwnerOrig";
        if (nbt.get(property) == null) return null;
        if (nbt.get(property).getType() == NbtElement.COMPOUND_TYPE) {
            nbt = nbt.getCompound(property);
            property = "Id";
            if (nbt.get(property) == null) return null;
        }
        return switch (nbt.get(property).getType()) {
            case NbtElement.INT_ARRAY_TYPE -> Uuids.toUuid(nbt.getIntArray(property));
            case NbtElement.STRING_TYPE -> UUID.fromString(nbt.getString(property));
            default -> null;
        };
    }

    enum uuidType {
        ID(),
        SKULLOWNERORIG(),
    }
}
