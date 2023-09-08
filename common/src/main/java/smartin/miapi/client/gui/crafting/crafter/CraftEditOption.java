package smartin.miapi.client.gui.crafting.crafter;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftViewRework;
import smartin.miapi.client.gui.crafting.crafter.replace.ReplaceView;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.network.Networking;

public class CraftEditOption extends InteractAbleWidget {
    EditOption.EditContext editContext;
    ItemModule module;

    public CraftEditOption(int x, int y, int width, int height, EditOption.EditContext context) {
        super(x, y, width, height, Text.empty());
        this.editContext = context;
        setMode(Mode.REPLACE);

    }

    public void setMode(Mode mode) {
        switch (mode) {
            case CRAFT -> {
                this.children.clear();
                CraftViewRework craftView = new CraftViewRework(this.getX(), this.getY(), this.width, this.height, 1, module, editContext, (backSlot) -> {
                    setMode(Mode.REPLACE);
                });
                addChild(craftView);
            }
            case REPLACE -> {
                this.children.clear();
                ReplaceView view = new ReplaceView(this.getX(), this.getY(), this.width, this.height, editContext.getSlot(), (moduleSlot -> {
                }), (itemModule -> {
                    this.module = itemModule;
                    setMode(Mode.CRAFT);
                }), (itemModule -> {
                    CraftAction action = new CraftAction(editContext.getItemstack(), editContext.getSlot(), itemModule, editContext.getPlayer(), editContext.getWorkbench(), new PacketByteBuf[0]);
                    action.setItem(editContext.getLinkedInventory().getStack(0));
                    action.linkInventory(editContext.getLinkedInventory(), 1);
                    editContext.preview(action.toPacket(Networking.createBuffer()));
                }));
                addChild(view);
            }
        }
    }

    public enum Mode {
        REPLACE,
        CRAFT
    }
}
