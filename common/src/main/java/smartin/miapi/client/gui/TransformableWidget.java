package smartin.miapi.client.gui;

import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;

public class TransformableWidget extends InteractAbleWidget{

    protected Matrix4f projection;

    /**
     * This is a Widget build to support Children and parse the events down to them.
     * Best use in conjunction with the ParentHandledScreen as it also handles Children correct,
     * unlike the base vanilla classes.
     * If you choose to handle some Events yourself and want to support Children yourself, you need to call the correct
     * super method or handle the children yourself
     *
     * @param x      the X Position
     * @param y      the y Position
     * @param width  the width
     * @param height the height
     *               These for Params above are used to create feedback on isMouseOver() by default
     * @param title  the Title of the Widget
     */
    public TransformableWidget(int x, int y, int width, int height, Text title) {
        super(x, y, width, height, title);
    }
}
