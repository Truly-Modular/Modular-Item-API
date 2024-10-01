#version 150

#moj_import <fog.glsl>
#moj_import <miapi_grayscale_to_palette.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec2 texCoord0;
in vec2 overlayCoord;
in vec4 normal;

out vec4 fragColor;

void main() {
    /*vec2 textureSize = textureSize(MaterialAtlas, 0);
    vec4 grayscaleColor = texture(Sampler0, texCoord0);
    vec2 paleteCoord = texCoord1;
    paleteCoord.x = (materialUV.x + grayscaleColor.r*255) / textureSize.x;
    paleteCoord.y =  (materialUV.y+5) / textureSize.y;
    vec4 color = texture(MaterialAtlas, paleteCoord) * ColorModulator;
    if (grayscaleColor.a < 0.1) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);*/

    vec4 grayscaleColor = texture(Sampler0, texCoord0);
    vec4 color = get_palette_color(Sampler1, overlayCoord/*vec2(0.0, 0.0)*/, grayscaleColor)/* * vertexColor * ColorModulator*/;

    if (color.a < 0.1) {
        discard;
    }

    color *= vertexColor * ColorModulator;
    color *= lightMapColor;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
