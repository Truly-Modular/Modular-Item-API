package smartin.miapi.material.base;

public interface StatController {
    /**
     * resolving a number data, used for material stats
     *
     * @param property
     * @return
     */
    double getDouble(String property);

    /**
     * resolving a string data, mostly unused
     *
     * @param property
     * @return
     */
    String getData(String property);
}
