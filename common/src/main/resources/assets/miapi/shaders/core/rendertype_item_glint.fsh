#version 150

#moj_import <fog.glsl>
#moj_import <miapi_grayscale_to_palette.glsl>

uniform sampler2D Sampler0;
uniform sampler2D CustomGlintTexture;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float GlintAlpha;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 localUVs;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(CustomGlintTexture, localUVs) * ColorModulator * vertexColor;
    vec4 realcolor = texture(Sampler0, texCoord0);
    if (realcolor.a < 0.1) {
        discard;
    }
    float fade = linear_fog_fade(vertexDistance, FogStart, FogEnd) * GlintAlpha;
    fragColor = vec4(color.rgb * fade, color.a-0.001);
}