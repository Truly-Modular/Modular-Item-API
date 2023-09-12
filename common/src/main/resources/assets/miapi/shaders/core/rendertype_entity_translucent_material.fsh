#version 150

#moj_import <fog.glsl>
#moj_import <miapi_grayscale_to_palette.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D MaterialAtlas;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec2 materialUV;

in float vertexDistance;
in vec3 position;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;
in vec2 UV2;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec2 textureSize = textureSize(MaterialAtlas, 0);
    vec4 grayscaleColor = texture(Sampler0, texCoord0);
    vec2 paleteCoord = texCoord1;
    paleteCoord.x = (materialUV.x + grayscaleColor.r*255) / textureSize.x;
    paleteCoord.y =  (materialUV.y+5) / textureSize.y;
    vec4 color = texture(MaterialAtlas, paleteCoord) * ColorModulator;
    if (grayscaleColor.a < 0.1) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
