package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;

/*
Any kind of property of a Module should be implemented here
 */
public interface ModuleProperty {

    /*
    This function loads and Validates data from the moduleFile
    Return true if it is valid
    return false if this shouldn't be loaded
    throw a detailed error of the data is broken in some way

    This can be used to cache property data if necessary.
     */
    boolean load(String moduleKey, JsonElement data) throws Exception;
}
