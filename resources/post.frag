#version 330

precision mediump float;

in vec2 fragTexCoord;
out vec4 fragColor;
uniform sampler2D texture0;

// Bloom
uniform vec4 colDiffuse;

const vec2 size = vec2(1280, 720);   // render size
const float samples = 5.0;          // pixels per axis; higher = bigger glow, worse performance
const float quality = 2.5;             // lower = smaller glow, better quality

// Vignette
uniform float vignetteRadius = 0.1;
uniform float vignetteBlur = 0.9;
uniform vec3 vignetteColor = vec3(1, 0, 0);

uniform float vignetteStrength = 0;

// https://github.com/raysan5/raylib/blob/master/examples/shaders/resources/shaders/glsl100/bloom.fs
vec4 bloom() {
    vec4 sum = vec4(0);
    vec2 sizeFactor = vec2(1)/size*quality;
    
    vec4 source = texture2D(texture0, fragTexCoord);
    
    const int range = int((samples-1.0)/2.0);

    for (int x = -range; x <= range; x++)
    {
        for (int y = -range; y <= range; y++)
        {
            sum += texture2D(texture0, fragTexCoord + vec2(x, y)*sizeFactor);
        }
    }

    return ((sum/(samples*samples)) + source)*colDiffuse;
}

// https://github.com/Apfelstrudel-Technologien/raylibVignette
vec4 vignette() {
    return mix(texture(texture0, fragTexCoord), vec4(vignetteColor, 1.0), smoothstep(vignetteRadius, vignetteRadius + vignetteBlur, distance(fragTexCoord, vec2(0.5, 0.5))));
}

void main() {
    vec4 bloomColor = bloom();
    vec4 vignetteColor = vignette();

    fragColor = bloomColor + vignetteColor * vignetteStrength;
}

