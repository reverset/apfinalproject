#version 330

precision mediump float;

in vec2 fragTexCoord;
out vec4 fragColor;

uniform float time = 0.0;

void main() {
    if (fragTexCoord.x > 0.334 || fragTexCoord.y > 0.415) {
        fragColor = vec4(1.0, 0.411, 0.705, 1.0);
    } else {
        float c = sin(time*10+fragTexCoord.x+fragTexCoord.y)*0.2+0.4;
        fragColor = vec4(1.0, c, c, 1.0);
    }
}