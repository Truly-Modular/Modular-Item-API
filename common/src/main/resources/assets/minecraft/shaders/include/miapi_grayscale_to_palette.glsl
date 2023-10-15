#version 150

/*
todo basic documentation on this for addon devs
*/
vec4 get_palette_color(sampler2D palette, vec2 pos, vec4 grayscale) {
//    ivec2 size = textureSize(palette, 0); // for testing

    /* getRaw uv for specific palette pixel:
    - x-coord: starting x (pos.x) plus grayscale amount
    - y-coord: starting y (pos.y), since all of every palettes' pixels are at the same y
    */
    ivec2 uv = ivec2(pos.x + grayscale.r*255.0, pos.y+0.01);
    vec4 color = texelFetch(palette, uv, 0) * vec4(1.0, 1.0, 1.0, grayscale.a); // fetching pixel color via texelFetch(which takes in absolute pixel coords) and then accounting for grayscale pixel's alpha
    return color;
}
