package smartin.miapi.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;

public class ResearchTree {
    public List<TreeNode> headNodes;
    public Map<Integer, List<TreeNode>> treeByLevel = new HashMap<>();


    public void setup(TreeNode rootNode) {
        addSubElement(0, rootNode);
    }

    private void addSubElement(int level, TreeNode blueprint) {
        List<TreeNode> getByLevel = treeByLevel.getOrDefault(level, new ArrayList<>());
        getByLevel.add(blueprint);
        treeByLevel.put(0, getByLevel);
        for (TreeNode researchAbleBlueprint : blueprint.childEntries) {
            addSubElement(level + 1 + researchAbleBlueprint.levelOffset, researchAbleBlueprint);
            DisplayInfo display;
            AdvancementsScreen screen;
        }
    }

    private void calculateHorizontalPositions(TreeNode node, int verticalOffset) {
        for (int i = node.childEntries.size()-1; i >= 0; i--) {
            calculateHorizontalPositions(node.childEntries.get(i), i + verticalOffset);
        }

        // Calculate the average horizontal position of children
        int totalHorizontalPos = node.childEntries.stream().mapToInt(child -> child.verticalPos).sum();
        int avgHorizontalPos = node.childEntries.isEmpty() ? 0 : totalHorizontalPos / node.childEntries.size();
        node.verticalPos = avgHorizontalPos;
    }


    public class TreeNode {
        public List<TreeNode> prerequisites = new ArrayList<>();
        public int levelOffset = 0;
        public int verticalPos = 0;
        public List<TreeNode> childEntries = new ArrayList<>();
    }
}
