#version 150

uniform sampler2D Sampler0;
uniform float fov;
uniform float aspect;
uniform float time;
uniform float starDensity;
uniform float nebulaIntensity;
uniform float twinkleSpeed;
uniform vec3 color;
uniform vec3 color2;
uniform float alpha;
uniform vec3 cameraRight;
uniform vec3 cameraUp;
uniform vec3 cameraForward;

in vec2 TexCoord;
out vec4 OutColor;

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(mix(hash13(i), hash13(i + vec3(1.0, 0.0, 0.0)), f.x),
            mix(hash13(i + vec3(0.0, 1.0, 0.0)), hash13(i + vec3(1.0, 1.0, 0.0)), f.x), f.y),
        mix(mix(hash13(i + vec3(0.0, 0.0, 1.0)), hash13(i + vec3(1.0, 0.0, 1.0)), f.x),
            mix(hash13(i + vec3(0.0, 1.0, 1.0)), hash13(i + vec3(1.0, 1.0, 1.0)), f.x), f.y),
        f.z
    );
}

float fbm3(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += noise3(p) * a;
        p = p * 2.03 + vec3(7.1, 3.7, 5.3);
        a *= 0.5;
    }
    return v;
}

vec3 starColor(float h) {
    if (h > 0.97) return vec3(1.0, 0.7, 0.5);
    if (h > 0.92) return vec3(1.0, 0.9, 0.7);
    if (h > 0.85) return vec3(0.7, 0.85, 1.0);
    return vec3(1.0, 1.0, 1.0);
}

vec3 starLayer(vec3 dir, float scale, float threshold, float starSize, float brightnessMul) {
    vec3 p = dir * scale;
    vec3 cell = floor(p);
    vec3 f = fract(p);

    float h = hash13(cell);
    if (h < threshold) return vec3(0.0);

    vec3 starPos = vec3(
        hash13(cell + vec3(1.0, 0.0, 0.0)),
        hash13(cell + vec3(0.0, 1.0, 0.0)),
        hash13(cell + vec3(0.0, 0.0, 1.0))
    );

    float d = length(f - starPos);
    float b = (h - threshold) / (1.0 - threshold);

    float core = smoothstep(starSize, 0.0, d);
    float glow = smoothstep(starSize * 3.0, 0.0, d) * 0.3;
    float star = (core + glow) * b * brightnessMul;

    float twinkle = 0.3 + 0.7 * sin(time * twinkleSpeed * 3.0 + h * 100.0);
    star *= twinkle;

    float ch = hash13(cell + vec3(5.0, 5.0, 5.0));
    return starColor(ch) * star;
}

void main() {
    float mask = texture(Sampler0, TexCoord).a;
    if (mask <= 0.0) discard;

    vec2 ndc = TexCoord * 2.0 - 1.0;
    float tanHalfFov = tan(radians(fov) * 0.5);
    vec3 viewDir = vec3(ndc.x * aspect * tanHalfFov, ndc.y * tanHalfFov, -1.0);
    vec3 worldDir = normalize(
        cameraRight  * viewDir.x +
        cameraUp     * viewDir.y +
        cameraForward * (-viewDir.z)
    );

    vec3 stars = vec3(0.0);
    stars += starLayer(worldDir, 20.0 * starDensity, 0.96, 0.06, 1.0);
    stars += starLayer(worldDir, 40.0 * starDensity, 0.97, 0.04, 0.7);
    stars += starLayer(worldDir, 80.0 * starDensity, 0.98, 0.025, 0.5);

    float neb1 = fbm3(worldDir * 2.5 + vec3(time * 0.005, 0.0, 0.0));
    float neb2 = fbm3(worldDir * 4.0 + vec3(0.0, time * 0.003, 5.0));
    float nebula = smoothstep(0.45, 0.85, neb1) * smoothstep(0.35, 0.75, neb2);

    float band = pow(max(0.0, 1.0 - abs(worldDir.y)), 4.0);
    float galaxyNoise = fbm3(worldDir * 6.0 + vec3(10.0));
    float galaxyDust = fbm3(worldDir * 12.0);
    float galaxy = band * galaxyNoise * (0.4 + galaxyDust * 0.3);

    vec3 deepSpace = mix(color, color2, 0.3) * 0.025;
    vec3 nebulaColor = mix(color, color2, neb2);
    vec3 galaxyColor = mix(color2, color, 0.4);

    vec3 rgb = deepSpace;
    rgb += stars;
    rgb += nebulaColor * nebula * nebulaIntensity * 0.6;
    rgb += galaxyColor * galaxy * nebulaIntensity * 0.5;

    float outAlpha = clamp(alpha * mask, 0.0, 1.0);
    if (outAlpha <= 0.001) discard;
    OutColor = vec4(rgb, outAlpha);
}
