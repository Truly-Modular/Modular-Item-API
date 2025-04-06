package smartin.miapi.editor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DocPage {
    public static final Codec<DocPage> CODEC = Codec.recursive(
            "module_instance",
            selfCodec -> RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("header", "no header").forGetter(p -> p.header),
                    Codec.STRING.optionalFieldOf("description", "no header").forGetter(p -> p.description),
                    Codec.unboundedMap(Codec.STRING, Codec.lazyInitialized(() -> selfCodec))
                            .optionalFieldOf("sub_pages", Map.of())
                            .forGetter(p -> p.sub_pages),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING)
                            .optionalFieldOf("data", Map.of())
                            .forGetter(p -> p.data),
                    Codec.STRING.optionalFieldOf("java").forGetter(p -> p.java),
                    Codec.STRING.listOf().optionalFieldOf("key_words", List.of()).forGetter(p -> p.key_words)
            ).apply(instance, DocPage::new)));

    public static Map<Class, DocPage> PAGE_LOOKUP = new HashMap<>();

    public final String header;
    public final String description;
    public final Map<String, String> data;
    public final Map<String, DocPage> sub_pages;
    public final Optional<String> java;
    public final List<String> key_words;

    public DocPage(String header, String description, Map<String, DocPage> sub_pages,
                   Map<String, String> data, Optional<String> java, List<String> key_words) {
        this.header = header;
        this.description = description;
        this.sub_pages = sub_pages;
        this.data = data;
        this.java = java;
        this.key_words = key_words;
    }

    public static void setupLookup(DocPage docPage) {
        if (docPage.java.isPresent()) {
            try {
                Class thisClass = docPage.getClass();
                String finalPath = docPage.java.get().replace("common/src/main/java/", "").replaceAll("/", ".").replace(".java", "");
                Class c = Class.forName(finalPath);
                PAGE_LOOKUP.put(c, docPage);
            } catch (RuntimeException | ClassNotFoundException c) {

            }
        }
        docPage.sub_pages.forEach((id, subPage) -> {
            setupLookup(subPage);
        });
    }
}

