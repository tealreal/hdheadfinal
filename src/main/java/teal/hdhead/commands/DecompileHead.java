package teal.hdhead.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import org.jetbrains.annotations.Nullable;
import teal.hdhead.HeadClient;

import java.util.Base64;
import java.util.UUID;

public interface DecompileHead extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();
        ItemStack itemStack = player.getMainHandStack();
        try {
            if (itemStack.isOf(Items.PLAYER_HEAD)) {
                NbtCompound nbt = itemStack.getTag();
                if(nbt == null) throw new SimpleCommandExceptionType(new LiteralMessage("Head does not have any NBT.")).create();
                NbtCompound skullOwner = nbt.getCompound("SkullOwner");
                if(skullOwner == null) throw new SimpleCommandExceptionType(new LiteralMessage("Head does not have a skull owner property.")).create();
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
                    MutableText text = itemStack.getName().shallowCopy();
                    text.append(Text.of("\nURL: ").copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)));
                    text.append(Text.of(url).copy().setStyle(Style.EMPTY
                        .withColor(Formatting.BLUE)
                        .withUnderline(true)
                        .withBold(false)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click here to open the texture")))
                    ));
                    UUID id = getUUID(nbt, uuidType.ID);
                    if (id != null) {
                        text.append(Text.of("\n"))
                            .append(Text.of("UUID: ").copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)))
                            .append(Text.of(id.toString()).copy().setStyle(Style.EMPTY
                                .withColor(Formatting.WHITE)
                                .withItalic(true)
                                .withBold(false)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id.toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click here to copy the UUID")))
                            ));
                    }
                    UUID or = getUUID(nbt, uuidType.SKULLOWNERORIG);
                    if (or != null) {
                        text.append(Text.of("\n"))
                            .append(Text.of("Original UUID: ").copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)))
                            .append(Text.of(or.toString()).copy().setStyle(Style.EMPTY
                                .withColor(Formatting.WHITE)
                                .withItalic(true)
                                .withBold(false)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, or.toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click here to copy the original UUID")))
                            ));
                    }
                    if (!skullOwner.getString("Name").isEmpty()) {
                        text.append(Text.of("\n"))
                            .append(Text.of("Watermark: ").copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)))
                            .append(Text.of(skullOwner.getString("Name")).copy().setStyle(Style.EMPTY
                                .withColor(Formatting.WHITE)
                                .withItalic(true)
                                .withBold(false)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, skullOwner.getString("Name")))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click here to copy the watermark")))
                            ));
                    }
                    text.append(Text.of("\n\n"))
                        .append(Text.of("[Click here to copy as JSON]").copy().setStyle(Style.EMPTY
                            .withColor(Formatting.GREEN)
                            .withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.asString()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click here to copy the NBT")))
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

    @Nullable
    static UUID getUUID(NbtCompound nbt, uuidType type) {
        String property = type == uuidType.ID ? "SkullOwner" : "SkullOwnerOrig";
        NbtElement ele = nbt.get(property);
        if (ele == null) return null;
        if (ele.getType() == NbtElement.COMPOUND_TYPE) {
            nbt = nbt.getCompound(property);
            property = "Id";
            ele = nbt.get(property);
            if (ele == null) return null;
        }
        return switch (ele.getType()) {
            case NbtElement.INT_ARRAY_TYPE -> DynamicSerializableUuid.toUuid(nbt.getIntArray(property));
            case NbtElement.STRING_TYPE -> UUID.fromString(nbt.getString(property));
            default -> null;
        };
    }

    enum uuidType {
        ID(),
        SKULLOWNERORIG(),
    }
}
