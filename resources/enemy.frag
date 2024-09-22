#version 330

precision mediump float;

varying vec2 fragTexCoord;
varying vec4 fragColor;

uniform float time = 0.0;

void main() {
    // I have a feeling that there is a bug somewhere in raylib, since these coordinates don't seem to make sense.
    if (fragTexCoord.x < 0.322 || fragTexCoord.x > 0.334 || fragTexCoord.y < 0.365 || fragTexCoord.y > 0.415) {
        gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
    } else {
        gl_FragColor = vec4(sin(time+fragTexCoord.x*1000), sin(time+fragTexCoord.x*fragTexCoord.y*1000), cos(time+fragTexCoord.y*1000), 1.0);
    }
}