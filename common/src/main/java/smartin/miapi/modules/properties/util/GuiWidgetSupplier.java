package smartin.miapi.modules.properties.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;

@Environment(EnvType.CLIENT)
public interface GuiWidgetSupplier {

    @Environment(EnvType.CLIENT)
    StatListWidget.TextGetter getTitle();

    @Environment(EnvType.CLIENT)
    StatListWidget.TextGetter getDescription();

    @Environment(EnvType.CLIENT)
    SingleStatDisplayDouble.StatReaderHelper getStatReader();

    default double getMinValue(){
        return 0;
    }

    default double getMaxValue(){
        return 5;
    }
}
