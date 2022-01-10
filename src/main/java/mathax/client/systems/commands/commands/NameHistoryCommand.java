package mathax.client.systems.commands.commands;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import mathax.client.systems.commands.Command;
import mathax.client.systems.commands.arguments.PlayerListEntryArgumentType;
import mathax.client.utils.Utils;
import mathax.client.utils.misc.ChatUtils;
import mathax.client.utils.misc.text.TextUtils;
import mathax.client.utils.network.HTTP;
import mathax.client.utils.network.MatHaxExecutor;
import mathax.client.utils.player.PlayerUtils;
import mathax.client.utils.render.color.Color;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class NameHistoryCommand extends Command {
    private static final Type RESPONSE_TYPE = new TypeToken<List<NameHistoryObject>>() {}.getType();

    public NameHistoryCommand() {
        super("name-history", "Provides a list of a players previous names from the Mojang API.", "history", "names");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerListEntryArgumentType.playerListEntry()).executes(context -> {
            MatHaxExecutor.execute(() -> {
                PlayerListEntry lookUpTarget = PlayerListEntryArgumentType.getPlayerListEntry(context);
                UUID uuid = lookUpTarget.getProfile().getId();

                List<NameHistoryObject> nameHistoryObjects = HTTP.get("https://api.mojang.com/user/profiles/" + formatUUID(uuid) + "/names").sendJson(RESPONSE_TYPE);

                if (nameHistoryObjects == null || nameHistoryObjects.isEmpty()) {
                    error("There was an error fetching that users name history.");
                    return;
                }

                String name = lookUpTarget.getProfile().getName();
                BaseText initial = new LiteralText(name);
                initial.append(new LiteralText(name.endsWith("s") ? "'" : "'s"));

                Color nameColor = PlayerUtils.getPlayerColor(mc.world.getPlayerByUuid(uuid), Utils.WHITE);

                initial.setStyle(initial.getStyle()
                    .withColor(new TextColor(nameColor.getPacked()))
                    .withClickEvent(new ClickEvent(
                            ClickEvent.Action.OPEN_URL,
                            "https://namemc.com/search?q=" + name
                        )
                    )
                    .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new LiteralText("View on NameMC")
                            .formatted(Formatting.YELLOW)
                            .formatted(Formatting.ITALIC)
                    ))
                );

                info(initial.append(new LiteralText(" Username History:").formatted(Formatting.GRAY)));

                for (NameHistoryObject nameHistoryObject : nameHistoryObjects) {
                    BaseText nameText = new LiteralText(nameHistoryObject.name);
                    nameText.formatted(Formatting.AQUA);

                    if (nameHistoryObject.changedToAt != 0L) {
                        BaseText changed = new LiteralText("Changed at: ");
                        changed.formatted(Formatting.GRAY);

                        Date date = new Date(nameHistoryObject.changedToAt);
                        DateFormat formatter = new SimpleDateFormat("hh:mm:ss, dd/MM/yyyy");
                        changed.append(new LiteralText(formatter.format(date)).formatted(Formatting.WHITE));

                        nameText.setStyle(nameText.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, changed)));
                    }

                    ChatUtils.sendMsg(nameText);
                }
            });

            return SINGLE_SUCCESS;
        }));
    }

    private String formatUUID(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    private static class NameHistoryObject {
        String name;
        long changedToAt;
    }
}
