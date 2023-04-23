package smartin.miapi.item.modular.cache;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import smartin.miapi.Miapi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ModularItemCache {
    protected static Map<UUID, Cache> cacheMap = new HashMap<>();
    protected static Map<String, CacheObjectSupplier> supplierMap = new HashMap<>();
    protected static Map<ItemStack,UUID> lookUpTable = new WeakHashMap<>();
    public static final String cacheKey = Miapi.MOD_ID+"uuid";

    public static void setSupplier(String key, CacheObjectSupplier supplier) {
        supplierMap.put(key, supplier);
    }

    public static Object get(ItemStack stack, String key) {
        Cache itemCache = find(stack);
        return itemCache.get(key);
    }

    public static void discardCache(){
        cacheMap.clear();
    }

    public static void updateNBT(ItemStack stack){
        if(stack.hasNbt()){
            String uuidString = stack.getNbt().getString(cacheKey);
            if(uuidString!=null){
                UUID uuid = UUID.fromString(uuidString);
                Cache itemCache = cacheMap.get(uuid);
                if(itemCache.uuid.equals(uuid)){
                    itemCache.nbtHash=stack.getNbt().hashCode();
                }
            }
        }
    }

    protected static Cache find(ItemStack stack){
        UUID lookUpUUId = lookUpTable.get(stack);
        if(lookUpUUId!=null){
            Cache itemCache = cacheMap.get(lookUpUUId);
            if(itemCache!=null) {
                return itemCache;
            }
        }
        if(stack.hasNbt()){
            String uuidString = stack.getNbt().getString(cacheKey);
            if(uuidString!=null){
                try{
                    UUID uuid = UUID.fromString(uuidString);
                    Cache itemCache = cacheMap.get(uuid);
                    if(itemCache!=null) {
                        if(itemCache.nbtHash==stack.getNbt().hashCode()){
                            return itemCache;
                        }
                    }
                }
                catch (Exception e){
                }
            }
        }
        UUID newUUID = getMissingUUID();
        NbtCompound nbt = stack.getNbt();
        if(nbt==null) nbt = new NbtCompound();
        nbt.putString(cacheKey,newUUID.toString());
        stack.setNbt(nbt);
        Cache cache = new Cache(newUUID,stack);
        cacheMap.put(newUUID,cache);
        lookUpTable.put(stack,newUUID);
        return cache;
    }

    protected static Cache findopti(ItemStack stack){
        UUID uuid = null;
        if (stack.hasNbt()) {
            String uuidString = stack.getNbt().getString(cacheKey);
            if (uuidString != null) {
                try {
                    uuid = UUID.fromString(uuidString);
                    Cache itemCache = cacheMap.get(uuid);
                    if (itemCache != null && itemCache.nbtHash == stack.getNbt().hashCode()) {
                        return itemCache;
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid UUID string, ignore it
                }
            }
        }
        if (uuid == null) {
            uuid = getMissingUUID();
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putString(cacheKey, uuid.toString());
            Cache cache = new Cache(uuid, stack);
            cacheMap.put(uuid, cache);
        }
        return cacheMap.get(uuid);
    }

    protected static UUID getMissingUUID(){
        UUID uuid;
        do{
            uuid = UUID.randomUUID();
        } while (cacheMap.containsKey(uuid));
        return uuid;
    }

    public interface CacheObjectSupplier extends Function<ItemStack, Object> {
        @Override
        Object apply(ItemStack stack);
    }

    protected static class Cache {
        protected Map<String, Object> map = new ConcurrentHashMap<>();
        public UUID uuid;
        public ItemStack stack;
        public int counter = 0;
        public int nbtHash;

        public Cache(UUID uuid, ItemStack stack) {
            this.uuid = uuid;
            this.stack = stack;
            if(stack.hasNbt()){
                nbtHash = stack.getNbt().hashCode();
            }
        }

        public void set(String key, Object object) {
            map.put(key, object);
        }

        public Object get(String key) {
            return map.computeIfAbsent(key,(id)->{
                CacheObjectSupplier supplier = supplierMap.get(id);
                if(supplier!=null){
                    return supplier.apply(stack);
                }
                return null;
            });
        }
    }
}
