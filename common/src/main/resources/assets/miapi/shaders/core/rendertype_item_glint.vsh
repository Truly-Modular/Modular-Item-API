#version 150

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
uniform mat4 TextureMat;
uniform int FogShape;
uniform float GlintSpeed;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord1;
out vec4 normal;

const float PACK_SCALE = 4096.0;

vec2 reconstructUV(vec2 hi, vec2 lo)
{
    vec2 fixedv;
    fixedv.x = float(hi.x * 256 + lo.x);
    fixedv.y = float(hi.y * 256 + lo.y);
    return fixedv / PACK_SCALE;
}

void main()
{
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(Position, FogShape);//

    texCoord0 = (TextureMat * vec4(Normal.xy, 0.0, 1.0)).xy;
    texCoord1 = UV0;

    vertexColor = Color;

    // pass through raw normal data (treated as payload)
    normal = vec4(Normal, 1.0);
}