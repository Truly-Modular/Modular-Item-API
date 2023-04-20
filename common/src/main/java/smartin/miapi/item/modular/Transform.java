package smartin.miapi.item.modular;

import com.google.gson.Gson;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class Transform extends Transformation{
    public static final Transform IDENTITY = new Transform(new Vec3f(), new Vec3f(), new Vec3f(1.0F, 1.0F, 1.0F));
    public Transform(Vec3f rotation, Vec3f translation, Vec3f scale) {
        super(rotation, translation, scale);
    }

    public static Transform merge(Transformation parent, Transformation child) {
        Vec3f parentRotation = parent.rotation.copy();
        Vec3f parentTranslation = parent.translation.copy();
        Vec3f parentScale = parent.scale.copy();

        Vec3f childRotation = child.rotation.copy();
        Vec3f childTranslation = child.translation.copy();
        Vec3f childScale = child.scale.copy();

        // apply parent rotation to child translation
        childTranslation.rotate(Quaternion.fromEulerXyzDegrees(parentRotation));

        // combine translation, rotation, and scale
        parentTranslation.add(childTranslation);
        parentRotation.add(childRotation);
        parentScale.multiplyComponentwise(childScale.getX(),childScale.getY(),childScale.getZ());

        return new Transform(parentRotation,parentTranslation, parentScale);
    }

    public Transform copy(){
        return new Transform(this.rotation.copy(),this.translation.copy(),this.scale.copy());
    }

    public static Transform repair(Transformation transformation){
        Vec3f parentRotation = transformation.rotation;
        if(parentRotation==null){
            parentRotation = new Vec3f(0,0,0);
        }
        Vec3f parentTranslation = transformation.translation;
        if(parentTranslation==null){
            parentTranslation = new Vec3f(0,0,0);
        }
        Vec3f parentScale = transformation.scale;
        if(parentScale==null){
            parentScale = new Vec3f(1,1,1);
        }
        return new Transform(parentRotation.copy(),parentTranslation.copy(),parentScale.copy());
    }

    public static Transform toModelTransformation(Transformation transformation){
        Transform transform = repair(transformation);
        transform.translation.scale(1.0f/16.0f);
        //Vec3f scale = transform.scale;
        //scale.scale(1.0f/16.0f);
        //transform = new Transform(transform.rotation,transform.translation,scale);
        return transform;
    }

    @Override
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
