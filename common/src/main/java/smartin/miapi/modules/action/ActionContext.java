package smartin.miapi.modules.action;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ActionContext {
    private final Map<Class<?>, ListTypeManager<?>> listRegistry = new HashMap<>();
    private final Map<Class<?>, SingleTypeManager<?>> objectRegistry = new HashMap<>();
    public ServerLevel level;
    public Player contextPlayer;
    public ItemStack contextItem;

    public <T> void addList(Class<T> type, String key, List<T> list) {
        getListTypeManager(type).putList(key, list);
    }

    public <T> void addObject(Class<T> type, String key, T object) {
        getSingleTypeManager(type).putObject(key, object);
    }

    public void addEntities(String key, List<Entity> entities) {
        addList(Entity.class, key, entities);
        addList(Vec3.class, key, entities.stream().map(Entity::position).toList());
    }

    public <T> Optional<List<T>> getList(Class<T> type, String key) {
        return getListTypeManager(type).getList(key);
    }

    public <T> Optional<T> getObject(Class<T> type, String key) {
        var opt = getSingleTypeManager(type).getObject(key);
        if (opt.isEmpty()) {
            var list = getList(type, key);
            if (list.isPresent()) {
                return Optional.of(list.get().getFirst());
            }
        }
        return getSingleTypeManager(type).getObject(key);
    }

    private <T> ListTypeManager<T> getListTypeManager(Class<T> type) {
        return (ListTypeManager<T>) listRegistry.computeIfAbsent(type, k -> new ListTypeManager<T>());
    }

    private <T> SingleTypeManager<T> getSingleTypeManager(Class<T> type) {
        return (SingleTypeManager<T>) objectRegistry.computeIfAbsent(type, k -> new SingleTypeManager<T>());
    }

    private static class ListTypeManager<T> {
        private final Map<String, List<T>> listMap = new HashMap<>();

        public void putList(String key, List<T> list) {
            listMap.put(key, list);
        }

        public Optional<List<T>> getList(String key) {
            return Optional.ofNullable(listMap.get(key));
        }
    }

    private static class SingleTypeManager<T> {
        private final Map<String, T> objectMap = new HashMap<>();

        public void putObject(String key, T object) {
            objectMap.put(key, object);
        }

        public Optional<T> getObject(String key) {
            return Optional.ofNullable(objectMap.get(key));
        }
    }
}
