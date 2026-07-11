#version 150

uniform sampler2D Sampler0;
uniform vec3 color;
uniform float opacity;
uniform vec2 offset;
uniform vec2 texelSize;

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    vec2 uv = TexCoord + offset;

    float center = texture(Sampler0, uv).a;
    if (center <= 0.001) discard;

    float edge = 0.0;
    edge += texture(Sampler0, uv + vec2(texelSize.x, 0.0)).a;
    edge += texture(Sampler0, uv - vec2(texelSize.x, 0.0)).a;
    edge += texture(Sampler0, uv + vec2(0.0, texelSize.y)).a;
    edge += texture(Sampler0, uv - vec2(0.0, texelSize.y)).a;
    edge *= 0.25;

    float rim = clamp(edge - center, 0.0, 1.0) * 1.8;
    float core = smoothstep(0.15, 0.85, center);

    float a = clamp(opacity * (core * 0.7 + rim * 0.5), 0.0, 1.0);
    if (a <= 0.002) discard;

    vec3 c = color * (0.8 + core * 0.35);
    OutColor = vec4(c, a);
}
