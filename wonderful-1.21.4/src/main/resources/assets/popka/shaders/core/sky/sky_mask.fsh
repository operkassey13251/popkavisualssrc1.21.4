#version 150

uniform sampler2D Sampler0;

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    float depth = texture(Sampler0, TexCoord).r;
    float mask = step(0.9999, depth);
    OutColor = vec4(mask, mask, mask, mask);
}
