package smartin.miapi.client.gui.crafting.crafter;

import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftViewRework;
import smartin.miapi.client.gui.crafting.crafter.replace.ReplaceView;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.network.Networking;

import java.util.HashMap;

public class CraftEditOption extends InteractAbleWidget {
    EditOption.EditContext editContext;
    CraftOption option = new CraftOption(ItemModule.empty,new HashMap<>());

    public CraftEditOption(int x, int y, int width, int height, EditOption.EditContext context) {
        super(x, y, width, height, Text.empty());
        this.editContext = context;
        setMode(Mode.REPLACE);

    }

    public void setMode(Mode mode) {
        switch (mode) {
            case CRAFT -> {
                this.children.clear();
                CraftViewRework craftView = new CraftViewRework(this.getX(), this.getY(), this.width, this.height, 1, option, editContext, (backSlot) -> {
                    setMode(Mode.REPLACE);
                });
                addChild(craftView);
            }
            case REPLACE -> {
                this.children.clear();
                ReplaceView view = new ReplaceView(this.getX(), this.getY(), this.width, this.height, editContext.getSlot(),editContext, (moduleSlot -> {
                }), (craftOption -> {
                    option = craftOption;
                    setMode(Mode.CRAFT);
                }), (craftOption -> {
                    option = craftOption;
                    CraftAction action = new CraftAction(editContext.getItemstack(), editContext.getSlot(), option.module(), editContext.getPlayer(), editContext.getWorkbench(), craftOption.data());
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
