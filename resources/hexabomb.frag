#version 330

precision mediump float;

varying vec2 fragTexCoord;
varying vec4 fragColor;

uniform float time = 0.0;

void main() {
    float c = sin(time*50+fragTexCoord.x+fragTexCoord.y)*0.2+0.4;
    gl_FragColor = vec4(1.0, 1.0, c, 1.0);
}