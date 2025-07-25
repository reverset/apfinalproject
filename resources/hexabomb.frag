#version 330

precision mediump float;

in vec2 fragTexCoord;
out vec4 fragColor;

uniform float time = 0.0;

void main() {
    float c = sin(time*50+fragTexCoord.x+fragTexCoord.y)*0.2+0.4;
    fragColor = vec4(1.0, 1.0, c, 1.0);
}