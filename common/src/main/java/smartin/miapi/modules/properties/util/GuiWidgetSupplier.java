package smartin.miapi.modules.properties.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;

public interface GuiWidgetSupplier {

    @Environment(EnvType.CLIENT)
    StatListWidget.TextGetter getTitle();

    @Environment(EnvType.CLIENT)
    StatListWidget.TextGetter getDescription();

    @Environment(EnvType.CLIENT)
    SingleStatDisplayDouble.StatReaderHelper getStatReader();

    @Environment(EnvType.CLIENT)
    default double getMinValue(){
        return 0;
    }

    @Environment(EnvType.CLIENT)
    default double getMaxValue(){
        return 5;
    }
}
