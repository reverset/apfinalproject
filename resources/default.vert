#version 330

// This is the default vertex shader provided by Raylib. I copied it here since for some reason
// The Java version of Raylib will not automatically load it if I only want to use a fragment shader.

// Input vertex attributes (from VBO)
in vec3 vertexPosition; // Vertex position
in vec2 vertexTexCoord; // Vertex texture coordinates
in vec4 vertexColor;    // Vertex color

// Input uniform values
uniform mat4 mvp;       // Model-View-Projection matrix

// Output vertex attributes (to Fragment Shader)
out vec2 fragTexCoord;
out vec4 fragColor;

void main()
{
    // Apply the model-view-projection transformation to the vertex position
    gl_Position = mvp * vec4(vertexPosition, 1.0);
    
    // Pass texture coordinates and color to the fragment shader
    fragTexCoord = vertexTexCoord;
    fragColor = vertexColor;
}