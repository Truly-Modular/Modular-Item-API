package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class BoxList extends InteractAbleWidget {
    private Map<Integer, List<ClickableWidget>> widgetPages;
    private int currentPage = 0;
    private int totalPages = 0;
    private int space = 5;
    private int maxHeight;
    private int maxWidth;
    private PageButton left;
    private PageButton right;

    /**
     * @param x       the x Position of the BoxList
     * @param y       the y Position of the BoxList
     * @param width   the width of the BoxList
     * @param height  the width of the BoxList
     * @param message the Title of the BoxList, Unused
     * @param widgets the supplier of Clickable Elements
     *                once the first number returns null
     *                no further elements are added.
     *                Make sure x,y,width and height are working for the ClickAbleWidgets
     */
    public BoxList(int x, int y, int width, int height, Text message, @Nullable List<ClickableWidget> widgets) {
        super(x, y, width, height, message);
        setButtons(new Identifier(Miapi.MOD_ID, "textures/button_left.png"), 10, 10, true);
        setButtons(new Identifier(Miapi.MOD_ID, "textures/button_right.png"), 10, 10, false);
        setWidgets(widgets, space);
    }

    public void setSpace(int space){
        this.space = space;
        List<ClickableWidget> widgets = new ArrayList<>();
        widgetPages.values().stream().forEach(subList->{
            widgets.addAll(subList);
        });
        setWidgets(widgets,space);
    }

    public void setButtons(Identifier texture, int width, int height, boolean isLeft) {
        if (isLeft) {
            left = new PageButton(0, 0, width, height, texture, true);
        } else {
            right = new PageButton(0, 0, width, height, texture, false);
        }
    }

    /**
     * This functions resets the List of widgets
     *
     * @param widgets the new List of widgets
     * @param space   the space in pixels between the widgets
     */
    public void setWidgets(@Nullable List<ClickableWidget> widgets, int space) {
        this.space = space;
        if (widgets == null) {
            widgets = new ArrayList<>();
        }
        maxHeight = 0;
        maxWidth = 0;
        for (int i = 0; i < widgets.size(); i++) {
            if (widgets.get(i).getWidth() > maxWidth) {
                maxWidth = widgets.get(i).getWidth();
            }
            if (widgets.get(i).getHeight() > maxHeight) {
                maxHeight = widgets.get(i).getHeight();
            }
        }
        int pageButtonHeight = Math.max(left.getHeight(), right.getHeight());
        int availableWidth = this.width - (2 * space);
        int availableHeight = this.height - (3 * space) - pageButtonHeight;
        int maxColumns = (int) Math.floor(availableWidth / (maxWidth + space));
        int maxRows = (int) Math.floor(availableHeight / (maxHeight + space));
        int totalPages = (int) Math.ceil((double) widgets.size() / (maxColumns * maxRows));
        widgetPages = new HashMap<>();
        for (int i = 0; i < totalPages; i++) {
            widgetPages.put(i, new ArrayList<>());
        }
        for (int i = 0; i < widgets.size(); i++) {
            int page = i / (maxColumns * maxRows);
            int columnIndex = (i % (maxColumns * maxRows)) % maxColumns;
            int rowIndex = (i % (maxColumns * maxRows)) / maxColumns;
            widgets.get(i).x = (columnIndex * (maxWidth + space)) + space + this.x;
            widgets.get(i).y = (rowIndex * (maxHeight + space)) + space + this.y;
            widgetPages.get(page).add(widgets.get(i));
        }
        this.totalPages = totalPages;
        setPage(currentPage);
        left.x = this.x + space;
        left.y = this.y + this.height - left.getHeight() - space;
        right.x = this.x + this.getWidth() - right.getHeight() - space;
        right.y = this.y + this.height - right.getHeight() - space;
    }

    public void setPage(int i) {
        currentPage = Math.min(Math.max(i, 0), totalPages - 1);
        this.children().clear();
        this.children().add(left);
        this.children().add(right);
        if(widgetPages.get(currentPage)!=null){
            this.children().addAll(widgetPages.get(currentPage));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //TODO:render current page
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    public class PageButton extends InteractAbleWidget {
        private boolean isLeft;
        private Identifier texture;
        private boolean isClicked;

        public PageButton(int x, int y, int width, int height, @Nonnull Identifier texture, boolean isLeft) {
            super(x, y, width, height, Text.empty());
            this.isLeft = isLeft;
            this.texture = texture;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.isClicked = true;
                if (isLeft) {
                    setPage(currentPage - 1);
                } else {
                    setPage(currentPage + 1);
                }
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.isClicked = true;
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int textureOffset = 0;

            if (isClicked) {
                textureOffset = 20;
                if (!isMouseOver(mouseX, mouseY)) {
                    isClicked = false;
                }
            } else if (this.isMouseOver(mouseX, mouseY)) {
                textureOffset = 10;
            }
            if (isLeft && currentPage == 0) {
                textureOffset = 30;
            }
            if (!isLeft && currentPage == totalPages - 1) {
                textureOffset = 30;
            }

            drawTexture(matrices, x, y, 0, textureOffset, 0, this.width, this.height, 40, this.height);
        }
    }
}
