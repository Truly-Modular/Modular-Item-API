package smartin.miapi.client.gui.crafting.crafter.help;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.help.pages.SinglePageTextImage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HelpPage extends InteractAbleWidget {
    int color = Color.GREEN.argb();
    List<SinglePageTextImage> pages;
    ScrollingTextWidget header;
    SimpleButton backBtn;
    SimpleButton prev;
    SimpleButton next;
    int currentPage = 0;
    static Identifier BACKGROUND = Identifier.of(Miapi.MOD_ID, "textures/gui/help_background.png");
    public static final Identifier BACKGROUND_TEXTURE = new Identifier(Miapi.MOD_ID, "textures/gui/help_background.png");

    public HelpPage(int x, int y, int width, int height, Text title, Consumer<InteractAbleWidget> remove, List<SinglePageTextImage> pages) {
        super(x, y, width, height, title);
        this.pages = pages;
        backBtn = new SimpleButton<>(x + 3, y + height - 21, 50, 18, Text.translatable(Miapi.MOD_ID + ".ui.back"), null, (a) -> {
            remove.accept(this);
        });
        prev = new SimpleButton<>(x + 55, y + height - 21, 50, 18, Text.translatable(Miapi.MOD_ID + ".ui.prev"), null, (a) -> {
            currentPage = Math.max(currentPage - 1, 0);
            setPage(currentPage);
        });
        next = new SimpleButton<>(x + width - 53, y + height - 21, 50, 18, Text.translatable(Miapi.MOD_ID + ".ui.next"), null, (a) -> {
            currentPage = Math.min(currentPage + 1, (pages.size() - 1) / 2);
            setPage(currentPage);
        });
        header = new ScrollingTextWidget(x + 10, y + 6, width - 10, title);
        setPage(0);
    }

    public void setPage(int x) {
        currentPage = x;
        this.children().clear();
        int lowerPage = x * 2;
        int upperPage = x * 2 + 1;
        if (pages.size() > lowerPage) {
            pages.get(lowerPage).setX(this.getX());
            pages.get(lowerPage).setY(this.getY() + 18);
            this.addChild(pages.get(lowerPage));
        }
        if (pages.size() > upperPage) {
            pages.get(upperPage).setX(this.getX() + width / 2);
            pages.get(upperPage).setY(this.getY() + 18);
            this.addChild(pages.get(upperPage));
        }
        if (pages.size() > 2) {
            next.isEnabled = pages.size() - 1 > upperPage;
            prev.isEnabled = x > 0;
            this.addChild(prev);
            this.addChild(next);
        }
        this.addChild(header);
        this.addChild(backBtn);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        //drawContext.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
        drawContext.drawTexture(BACKGROUND, getX(), getY(), getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        //drawContext.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, getX(), getY(), 0, 0, getWidth(), getHeight());
        CraftingScreen craftingScreen;
        super.render(drawContext, mouseX, mouseY, delta);
    }

    public static List<SinglePageTextImage> getPages(String key, int count) {
        List<SinglePageTextImage> pages = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            pages.add(new SinglePageTextImage("miapi.help." + key + ".info." + i, "textures/gui/help/" + key + "/" + i + ".png"));
        }
        return pages;
    }
}
