package smartin.miapi.modules.material.palette;

import com.redpxnda.nucleus.util.Color;
import smartin.miapi.modules.material.Material;

public class FallbackColorer extends SpritePixelReplacer {
    public FallbackColorer(Material material) {
        super(material);
    }

    @Override
    public int getReplacementColor(int pixelX, int pixelY, int previousAbgr) {
        return previousAbgr;
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public Color getAverageColor() {
        return Color.WHITE;
    }
}
