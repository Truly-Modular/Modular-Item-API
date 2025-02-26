package smartin.miapi.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import smartin.miapi.material.base.Material;

@Environment(EnvType.CLIENT)
public class FallbackColorer extends SpritePixelReplacer {
    public FallbackColorer(Material material) {
        super(material);
    }

    @Override
    public int getReplacementColor(int pixelX, int pixelY, int previousAbgr) {
        return previousAbgr;
    }

    @Override
    public boolean doTick() {
        return false;
    }

    @Override
    public Color getAverageColor() {
        return Color.WHITE;
    }
}
