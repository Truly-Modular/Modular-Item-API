#version 150

#moj_import <fog.glsl>
#moj_import <miapi_grayscale_to_palette.glsl>

uniform sampler2D Sampler0;
uniform sampler2D MatColors;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 grayscaleColor = texture(Sampler0, texCoord0);
    vec4 color = get_palette_color(MatColors, grayscaleColor) * vertexColor * ColorModulator;

    if (color.a < 0.1) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
