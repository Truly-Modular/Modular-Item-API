#version 150

/*
todo basic documentation on this for addon devs
*/
vec4 get_palette_color(sampler2D palette, vec2 pos, vec4 grayscale) {
    ivec2 size = textureSize(palette, 0); // need sampler size because pos is exact, rather than 0-1 like it should be
    vec2 uv = vec2((pos.x + (grayscale.r - 0.003921))/float(size.x), pos.y/float(size.y));
    vec4 color = texture(palette, uv) * vec4(1.0, 1.0, 1.0, grayscale.a);
    return color;
}
