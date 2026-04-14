package smartin.miapi.forge.compat.epic_fight;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EpicFightCompat {
    public static void setup() {
        EpicFightClientEventHooks.Registry.MODIFY_PATCHED_ENTITY.registerEvent(event -> {
            BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
                var renderer = event.get(entityType);
                if (renderer instanceof PHumanoidRenderer<?, ?, ?, ?, ?> playerRenderer) {
                    addLayerSafe(playerRenderer);
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    private static <E extends LivingEntity,
            T extends LivingEntityPatch<E>,
            M extends HumanoidModel<E>,
            R extends LivingEntityRenderer<E, M>,
            AM extends HumanoidMesh>
    void addLayerSafe(PHumanoidRenderer<E, T, M, R, AM> renderer) {
        renderer.addCustomLayer(new CustomModularArmorRenderer<E, T, M, AM>(null));
    }
}
