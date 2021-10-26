package mathax.legacy.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import mathax.legacy.client.gui.renderer.GuiRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import static mathax.legacy.client.MatHaxLegacy.mc;

public class WItem extends WWidget {
    protected ItemStack itemStack;

    public WItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    protected void onCalculateSize() {
        double s = theme.scale(32);

        width = s;
        height = s;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.post(() -> {
            double s = theme.scale(2);

            MatrixStack matrices = RenderSystem.getModelViewStack();

            matrices.push();
            matrices.scale((float) s, (float) s, 1);
            matrices.translate(x / s, y / s, 0);

            mc.getItemRenderer().renderGuiItemIcon(itemStack, 0, 0);

            matrices.pop();
        });
    }

    public void set(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
