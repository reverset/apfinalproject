#version 330

precision mediump float;

varying vec2 fragTexCoord;
varying vec4 fragColor;

uniform sampler2D texture0;
uniform vec4 colDiffuse;

const vec2 size = vec2(850, 450);   // render size
const float samples = 5.0;          // pixels per axis; higher = bigger glow, worse performance
const float quality = 2.5;             // lower = smaller glow, better quality

void main() {
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

    gl_FragColor = ((sum/(samples*samples)) + source)*colDiffuse;
}