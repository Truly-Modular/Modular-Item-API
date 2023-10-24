package smartin.miapi.modules.properties.util;

import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;

public interface GuiWidgetSupplier {

    StatListWidget.TextGetter getTitle();

    StatListWidget.TextGetter getDescription();

    SingleStatDisplayDouble.StatReaderHelper getStatReader();
}
