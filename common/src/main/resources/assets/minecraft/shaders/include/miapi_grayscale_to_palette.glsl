#version 150

vec4 get_palette_color(sampler2D palette, vec4 grayscale) {
    float luminance = 0.2126*grayscale.r + 0.7152*grayscale.g + 0.0722*grayscale.b;
    vec2 uv0 = vec2(luminance, 0.0);
    vec4 color = texture(MatColors, uv0) * vec4(1.0, 1.0, 1.0, grayscale.a);
    return color;
}
