#version 330

precision mediump float;

varying vec2 fragTexCoord;
varying vec4 fragColor;

uniform float time = 0.0;

void main() {
    if (fragTexCoord.x < 0.322 || fragTexCoord.x > 0.334 || fragTexCoord.y < 0.365 || fragTexCoord.y > 0.415) {
        gl_FragColor = vec4(1.0, 0.411, 0.705, 1.0);
    } else {
        float c = sin(time*2+fragTexCoord.x+fragTexCoord.y)*0.5-0.1;
        gl_FragColor = vec4(1.0, c, c, 1.0);
        // gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
}