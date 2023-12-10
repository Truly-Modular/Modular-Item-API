package smartin.miapi.blueprint;

import com.redpxnda.nucleus.facet.FacetKey;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 Panda -> Smartin: you don't actually extend FacetKey, you need to
 implement {@link com.redpxnda.nucleus.facet.EntityFacet}, with T
 being the type you (de)serialize to. In order to get the facet key,
 register your facet in {@link com.redpxnda.nucleus.facet.FacetRegistry}.
 In order to attach it to your entity, use {@link com.redpxnda.nucleus.facet.FacetRegistry#ENTITY_FACET_ATTACHMENT} event.
 Check {@link com.redpxnda.nucleus.Nucleus#capabilities()} for examples.
 (and in order to get the facet from the entity, use the registered
 facet key you received- {@link FacetKey#get(Entity)})
 */
public class PlayerBlueprintFacet extends FacetKey<PlayerBoundBlueprint> {
    //TODO:player+item bound blueprint storage
    protected PlayerBlueprintFacet(Identifier id, Class<PlayerBoundBlueprint> cls) {
        super(id, cls);
    }
}
