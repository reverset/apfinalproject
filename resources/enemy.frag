#version 330

precision mediump float;

in vec2 fragTexCoord;
out vec4 fragColor;

uniform float time = 0.0;

void main() {
    // there's a bug in raylib when using shaders on rectangles, these coordinates don't make much sense.
    if (fragTexCoord.x < 0.322 || fragTexCoord.x > 0.334 || fragTexCoord.y < 0.365 || fragTexCoord.y > 0.415) {
        fragColor = vec4(1.0, 0.0, 0.0, 1.0);
    } else {
        fragColor = vec4(sin(time+fragTexCoord.x*1000), sin(time+fragTexCoord.x*fragTexCoord.y*1000), cos(time+fragTexCoord.y*1000), 1.0);
    }
}