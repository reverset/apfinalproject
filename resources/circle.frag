#version 330

precision mediump float;

varying vec2 fragTexCoord;
varying vec4 fragColor;

uniform float time = 0.0;

void main() {
    if (fragTexCoord.x < 0.322 || fragTexCoord.x > 0.334 || fragTexCoord.y < 0.365 || fragTexCoord.y > 0.415) {
        gl_FragColor = vec4(1.0, 0.411, 0.705, 1.0);
    } else {
        gl_FragColor = vec4(sin(time+fragTexCoord.x*1000), sin(time+fragTexCoord.x*fragTexCoord.y*1000), cos(time+fragTexCoord.y*1000), 1.0);
        // gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
}