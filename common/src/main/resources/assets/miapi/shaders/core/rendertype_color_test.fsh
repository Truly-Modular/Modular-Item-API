#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D MatColors;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 grayscale_color = texture(Sampler0, texCoord0);
    float luminance = 0.2126*grayscale_color.r + 0.7152*grayscale_color.g + 0.0722*grayscale_color.b;
    vec2 uv0 = vec2(luminance, 0.0);
    vec4 color = texture(MatColors, uv0) * vertexColor * ColorModulator;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
