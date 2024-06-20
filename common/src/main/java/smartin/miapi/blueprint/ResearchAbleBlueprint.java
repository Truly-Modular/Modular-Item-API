package smartin.miapi.blueprint;

import java.util.ArrayList;
import java.util.List;

public class ResearchAbleBlueprint {
    public List<ResearchAbleBlueprint> prerequisites = new ArrayList<>();
    public ResearchTree researchTree;
    public String key;
    public int levelOffset = 0;
    public List<Blueprint> blueprintList = new ArrayList<>();
    public List<ResearchAbleBlueprint> childEntries = new ArrayList<>();


    public ResearchAbleBlueprint(List<ResearchAbleBlueprint> prerequisites, String key) {
        this.prerequisites = prerequisites;
        this.key = key;
    }

    public void learn() {

    }
}
