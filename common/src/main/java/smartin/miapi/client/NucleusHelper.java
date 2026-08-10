package smartin.miapi.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.math.InterpolateMode;
import com.redpxnda.nucleus.pose.client.HumanoidPoseAnimation;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class NucleusHelper {
    public static final Codec<HumanoidPoseAnimation.PartState> codec = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.listOf().optionalFieldOf("position", List.of(0f, 0f, 0f))
                            .forGetter(p -> List.of(p.position.x, p.position.y, p.position.z)),

                    Codec.FLOAT.listOf().optionalFieldOf("rotation", List.of(0f, 0f, 0f))
                            .forGetter(p -> List.of(
                                    (float) Math.toDegrees(p.rotation.x),
                                    (float) Math.toDegrees(p.rotation.y),
                                    (float) Math.toDegrees(p.rotation.z)
                            )),

                    Codec.FLOAT.listOf().optionalFieldOf("scale", List.of(1f, 1f, 1f))
                            .forGetter(p -> List.of(p.scale.x, p.scale.y, p.scale.z)),

                    InterpolateMode.codec.optionalFieldOf("interpolateMode")
                            .forGetter(p -> Optional.ofNullable(p.interpolateMode))
            ).apply(instance, (position, rotation, scale, interpolateMode) -> {
                HumanoidPoseAnimation.PartState state = new HumanoidPoseAnimation.PartState();

                state.position = new Vector3f(
                        position.get(0),
                        position.get(1),
                        position.get(2)
                );

                state.rotation = new Vector3f(
                        (float) Math.toRadians(rotation.get(0)),
                        (float) Math.toRadians(rotation.get(1)),
                        (float) Math.toRadians(rotation.get(2))
                );

                state.scale = new Vector3f(
                        scale.get(0),
                        scale.get(1),
                        scale.get(2)
                );

                state.interpolateMode = interpolateMode.orElse(null);

                return state;
            })
    );
}
