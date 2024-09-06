package smartin.miapi.craft;

public interface MaterialCraftInfo {
    int getSlotHeight();

    void setSlotHeight(int newHeight);

    double getMaterialCostClient();

    double getMaterialRequirementClient();

    default boolean renderMaterialWidget() {
        return true;
    }
}
