package mathax.legacy.client.systems.modules.render.hud.modules;

import mathax.legacy.client.settings.BoolSetting;
import mathax.legacy.client.settings.EnumSetting;
import mathax.legacy.client.settings.Setting;
import mathax.legacy.client.settings.SettingGroup;
import mathax.legacy.client.systems.modules.render.hud.HUD;
import mathax.legacy.client.systems.modules.render.hud.TripleTextHUDElement;
import mathax.legacy.client.utils.misc.Names;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class LookingAtHUD extends TripleTextHUDElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Determines what to target.")
        .defaultValue(Mode.Both)
        .build()
    );

    private final Setting<Boolean> position = sgGeneral.add(new BoolSetting.Builder()
        .name("position")
        .description("Displays the coordinates of the target.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> waterLogged = sgGeneral.add(new BoolSetting.Builder()
        .name("waterlogged-status")
        .description("Displays if the target is waterlogged or in water.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showUUID = sgGeneral.add(new BoolSetting.Builder()
        .name("uuid")
        .description("Displays the uuid of the target.")
        .defaultValue(false)
        .build()
    );

    public LookingAtHUD(HUD hud) {
        super(hud, "looking-at", "Displays what entity or block you are looking at.", true);
    }

    @Override
    protected String getLeft() {
        return "Looking at: ";
    }

    @Override
    protected String getRight() {
        if (isInEditor()) return position.get() ? "Obsidian [0, 0, 0]" : "Obsidian";

        if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK && mode.get() != Mode.Entities) {
            BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
            String result = Names.get(mc.world.getBlockState(pos).getBlock());

            if (position.get()) result += String.format(" (%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
            if (waterLogged.get() && mc.world.getFluidState(pos).isIn(FluidTags.WATER)) result += " (water logged)";

            return result;
        }

        else if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY && mode.get() != Mode.Blocks) {
            Entity target = ((EntityHitResult) mc.crosshairTarget).getEntity();
            String result;

            if (target instanceof PlayerEntity) result = ((PlayerEntity) target).getGameProfile().getName();
            else result = target.getName().getString();

            if (position.get()) result += String.format(" (%d, %d, %d)", target.getBlockX(), target.getBlockY(), target.getBlockZ());
            if (waterLogged.get() && target.isTouchingWater()) result += " (in water)";
            if (showUUID.get()) result += String.format(" (%s)", target.getUuidAsString());

            return result;
        }

        return "Nothing";
    }

    @Override
    protected String getEnd() {
        return "";
    }

    public enum Mode {
        Entities,
        Blocks,
        Both
    }
}
