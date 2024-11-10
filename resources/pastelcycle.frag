#version 330

out vec2 fragTexCoord;
out vec4 fragColor;

uniform float time = 0.0;

void main() {
    vec3 col = 0.75 + 0.25 * cos(time*3.0 + fragTexCoord.xyx*4.0+vec3(0, 2, 4));
    fragColor = vec4(col, 1.0);
}