package smartin.miapi.editor.util;

import com.mojang.datafixers.util.Pair;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListEditor<T> {
    private final String title;
    private final Consumer<List<T>> onChange;
    private List<Pair<T, Supplier<T>>> rendering = new ArrayList<>();
    private final BiFunction<T, Integer, Supplier<T>> getRenderFunction;
    private final Supplier<T> defaultEntry;

    public ListEditor(String title, List<T> items,
                      Consumer<List<T>> onChange,
                      BiFunction<T, Integer, Supplier<T>> renderEntry,
                      Supplier<T> defaultEntry) {
        this.title = title;
        this.onChange = onChange;
        this.defaultEntry = defaultEntry;
        this.getRenderFunction = renderEntry;
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            rendering.add(new Pair<>(item, renderEntry.apply(item, i)));
        }
    }

    public void render() {
        if (ImGui.collapsingHeader(title, ImGuiTreeNodeFlags.DefaultOpen)) {
            List<Pair<T, Supplier<T>>> nextRendering = new ArrayList<>();
            boolean changed = false;
            for (int i = 0; i < rendering.size(); i++) {
                var toRender = rendering.get(i);
                var t = toRender.getSecond().get();
                if (ImGui.button("Remove")) {
                    changed = true;
                } else {
                    nextRendering.add(new Pair<>(t, toRender.getSecond()));
                }
            }

            if (ImGui.button("Add Entry")) {
                // This will be overridden by the specific implementation
                var t = defaultEntry.get();
                nextRendering.add(new Pair<>(t, getRenderFunction.apply(t, nextRendering.size())));
                changed = true;
            }
            rendering = nextRendering;
            if (changed) {
                onChange.accept(nextRendering.stream().map(Pair::getFirst).toList());
            }
        }
    }

    public List<T> getList(){
        return rendering.stream().map(Pair::getFirst).toList();
    }
}