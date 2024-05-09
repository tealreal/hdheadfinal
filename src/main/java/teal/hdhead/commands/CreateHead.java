package teal.hdhead.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import teal.hdhead.HDHeads;
import teal.hdhead.mixin.TUCInvoker;
import teal.hdhead.util.Rawsay;
import teal.hdhead.util.argument.StringArgumentTypePlus;
import teal.hdhead.util.argument.URLArgumentType;

import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public interface CreateHead extends Command<FabricClientCommandSource> {
    @Override
    default int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        FabricClientCommandSource source = context.getSource();
        if (!source.getPlayer().isCreative()) throw new SimpleCommandExceptionType(new LiteralMessage("You must be in creative mode.")).create();

        URL url = URLArgumentType.getURL(context, "url");
        int[] intArrUUID = Uuids.toIntArray(UUID.randomUUID());
        NbtCompound nbt = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        nbt.putIntArray("SkullOwnerOrig", intArrUUID);
        skullOwner.putIntArray("Id", intArrUUID);
        skullOwner.putString("Name", "xtealhead" + Long.toString(System.currentTimeMillis(), 16));

        NbtCompound properties = new NbtCompound();
        NbtList textures = new NbtList();
        textures.addElement(0, getB64Object(url));
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        nbt.put("SkullOwner", skullOwner);
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        head.setNbt(nbt);
        try {
            head.setCustomName(Rawsay.parseFormatting(StringArgumentTypePlus.getString(context, "name"), '&'));
        } catch (IllegalArgumentException IAE) {

        }

        for (int i = 0; i < 9; i++)
        {
            if (!source.getPlayer().getInventory().getStack(i).isEmpty()) continue;
            source.getPlayer().networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + i, head));
            source.getPlayer().sendMessage(Text.literal("Produced a new HD Head from ").append(Text.literal(url.toString()).formatted(Formatting.UNDERLINE)), false);
            // OH, that's how you use an invoker
            if (!TUCInvoker.callIsAllowedTextureDomain(url.toString()))
                source.getPlayer().sendMessage(
                        Text.of(Formatting.GOLD.toString() +
                                Formatting.BOLD +
                                "WARNING: " +
                                Formatting.GRAY +
                                Formatting.ITALIC +
                                "You created a head with a URL that isn't " +
                                (HDHeads.doRunMod() ? "whitelisted or was blacklisted" : "available with the mod off") +
                                ", so it will not render."
                        ), false);
            return 0;
        }

        throw new SimpleCommandExceptionType(new LiteralMessage("Your hotbar is full. Clear out a slot and retry.")).create();
    }

    static NbtCompound getB64Object(URL url) {
        JsonObject obj = new JsonObject();
        obj.add("textures", new JsonObject());
        obj.getAsJsonObject("textures").add("SKIN", new JsonObject());
        obj.getAsJsonObject("textures").getAsJsonObject("SKIN").addProperty("url", url.toString());
        NbtCompound val = new NbtCompound();
        val.putString("Value", Base64.getEncoder().encodeToString(obj.toString().getBytes()));
        return val;
    }

    static CreateHead get() {
        return new CreateHead() {};
    }
}
