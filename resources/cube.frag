#version 330

uniform vec2 resolution;
uniform float time;

uniform vec3 cubeColorCoeffs;

out vec4 fragColor;

void main() {
    // normalization for easier calculations
    vec2 uv = (gl_FragCoord.xy - resolution * 0.5) / resolution.y;
    
    vec3 camPos = vec3(0.0, 0.0, 3.0);
    vec3 rayDir = normalize(vec3(uv, -1.0));
    
    vec3 cubeSize = vec3(0.8);
    vec3 cubePos = vec3(0.0);

    float angleX = time * 0.5;
    float angleY = time * 0.7;
    float angleZ = time * 0.3;

    mat3 rotX = mat3(
        1.0,       0.0,        0.0,
        0.0,  cos(angleX), -sin(angleX),
        0.0,  sin(angleX),  cos(angleX)
    );

    mat3 rotY = mat3(
        cos(angleY), 0.0, sin(angleY),
        0.0,         1.0, 0.0,
       -sin(angleY), 0.0, cos(angleY)
    );

    mat3 rotZ = mat3(
        cos(angleZ), -sin(angleZ), 0.0,
        sin(angleZ),  cos(angleZ), 0.0,
        0.0,           0.0,        1.0
    );

    mat3 rotation = rotZ * rotY * rotX;

    // raymarching magic lol
    float totalDist = 0.0;
    const float maxDist = 15.0;
    const float surfaceThresh = 0.01;
    const int maxSteps = 64;
    
    bool hit = false;
    for (int i = 0; i < maxSteps; i++) {
        vec3 p = camPos + rayDir * totalDist;

        vec3 rotatedPos = rotation * p;
        vec3 d = abs(rotatedPos - cubePos) - cubeSize;

        // Cube SDF
        float dist = min(max(d.x, max(d.y, d.z)), 0.0) + length(max(d, 0.0));

        if (dist < surfaceThresh) {
            hit = true;
            break;  // collision!
        }

        totalDist += dist;

        if (totalDist > maxDist) {
            break;  // missed target
        }
    }
    
    float shade = 0.9 - 2*totalDist / maxDist;
    vec3 finalColor = vec3(shade * cubeColorCoeffs.x, shade * cubeColorCoeffs.y, shade * cubeColorCoeffs.z);
    
    fragColor = vec4(finalColor, hit ? 1.0 : 0.0);
}
