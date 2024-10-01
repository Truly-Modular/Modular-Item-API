#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV1;
in vec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler0;
uniform sampler2D CustomGlintTexture;

uniform mat4 ModelViewMat;
uniform mat4 ModelMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;
uniform mat4 TextureMat;
uniform int FogShape;
uniform float GlintSpeed;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;
out vec2 localUVs;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(ModelViewMat, IViewRotMat * Position, FogShape);
    texCoord0 = UV0;
    vertexColor = Color;
    normal = vec4(Normal, 1.0);

    // Calculate the Model-View-Projection matrix
    mat4 inverseTextureMatrix = inverse(ModelMat);
    vec4 asd = inverseTextureMatrix * vec4(Position, 1.0);

    // Apply the texture scaling factor
    //uv *= textureScale;
    float scale = 0.0078125f * 100;

    // Output the final local space UVs
    localUVs.x = -asd.x * scale * 2.0 + 0.5;
    localUVs.y = -asd.y * scale * 2.0 + 0.5;
    localUVs = (TextureMat * (GlintSpeed) * vec4(vec2(localUVs/(GlintSpeed)), 0.0, 1.0)).xy;
}
