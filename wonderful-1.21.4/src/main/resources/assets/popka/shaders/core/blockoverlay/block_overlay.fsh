#version 150

uniform sampler2D Sampler0;
uniform vec2 texelSize;
uniform vec3 color;
uniform vec3 color2;
uniform float time;

uniform float speed;
uniform float scale;
uniform float outline;
uniform float glow;
uniform float fill;
uniform float alpha;
uniform float outlineOnly;
uniform float worldSpace;
uniform float fov;
uniform float aspect;
uniform vec3 cameraRight;
uniform vec3 cameraUp;
uniform vec3 cameraForward;

in vec2 TexCoord;
out vec4 OutColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += noise(p) * amplitude;
        p = p * 2.02 + vec2(8.4, 5.7);
        amplitude *= 0.5;
    }
    return value;
}

float ridged(vec2 p) {
    float value = 0.0;
    float amplitude = 0.55;
    for (int i = 0; i < 4; i++) {
        float r = 1.0 - abs(noise(p) * 2.0 - 1.0);
        value += r * amplitude;
        p = p * 2.18 + vec2(3.1, 9.2);
        amplitude *= 0.52;
    }
    return value;
}

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
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += noise3(p) * amplitude;
        p = p * 2.02 + vec3(8.4, 5.7, 3.1);
        amplitude *= 0.5;
    }
    return value;
}

float ridged3(vec3 p) {
    float value = 0.0;
    float amplitude = 0.55;
    for (int i = 0; i < 4; i++) {
        float r = 1.0 - abs(noise3(p) * 2.0 - 1.0);
        value += r * amplitude;
        p = p * 2.18 + vec3(3.1, 9.2, 6.5);
        amplitude *= 0.52;
    }
    return value;
}

float getEdge(vec2 uv, float s) {
    float a0 = texture(Sampler0, uv).a;
    float ax1 = texture(Sampler0, uv + vec2(texelSize.x * s, 0.0)).a;
    float ax2 = texture(Sampler0, uv - vec2(texelSize.x * s, 0.0)).a;
    float ay1 = texture(Sampler0, uv + vec2(0.0, texelSize.y * s)).a;
    float ay2 = texture(Sampler0, uv - vec2(0.0, texelSize.y * s)).a;
    float edge = abs(a0 - ax1) + abs(a0 - ax2) + abs(a0 - ay1) + abs(a0 - ay2);

    float diag1 = texture(Sampler0, uv + vec2(texelSize.x * s, texelSize.y * s)).a;
    float diag2 = texture(Sampler0, uv + vec2(-texelSize.x * s, texelSize.y * s)).a;
    float diag3 = texture(Sampler0, uv + vec2(texelSize.x * s, -texelSize.y * s)).a;
    float diag4 = texture(Sampler0, uv + vec2(-texelSize.x * s, -texelSize.y * s)).a;
    edge += 0.7 * (abs(a0 - diag1) + abs(a0 - diag2) + abs(a0 - diag3) + abs(a0 - diag4));

    return clamp(edge * 1.9, 0.0, 1.0);
}

void main() {
    vec2 uv = TexCoord;
    float mask = texture(Sampler0, uv).a;
    if (mask <= 0.0) discard;

    float edge = getEdge(uv, outline);
    float edgeBand = smoothstep(0.02, 0.42, edge);
    if (outlineOnly > 0.5) {
        float edgeAlpha = clamp(edgeBand * alpha, 0.0, 1.0) * mask;
        if (edgeAlpha <= 0.001) discard;
        OutColor = vec4(mix(color, color2, 0.35), edgeAlpha);
        return;
    }

    float t = time * max(speed, 0.001);

    float mist;
    float veins;
    float stripeA;
    float stripeB;

    if (worldSpace > 0.5) {
        vec2 ndc = TexCoord * 2.0 - 1.0;
        float tanHalfFov = tan(radians(fov) * 0.5);
        vec3 viewDir = vec3(ndc.x * aspect * tanHalfFov, ndc.y * tanHalfFov, -1.0);
        vec3 worldDir = normalize(
            cameraRight  * viewDir.x +
            cameraUp     * viewDir.y +
            cameraForward * (-viewDir.z)
        );

        float scl = mix(1.2, 3.2, clamp(scale / 3.0, 0.0, 1.0));
        vec3 p = worldDir * scl;
        vec3 drift3 = vec3(t * 0.20, -t * 0.15, t * 0.12);

        vec3 warp = vec3(
            fbm3(p * 0.90 + drift3 * 0.75 + vec3(0.0, 4.1, 2.3)),
            fbm3(p * 0.78 - drift3 * 0.48 + vec3(3.7, 1.8, 5.1)),
            fbm3(p * 0.83 + drift3 * 0.33 + vec3(1.2, 6.4, 8.7))
        );
        vec3 q = p + (warp - 0.5) * 1.8;

        mist = fbm3(q * 0.72 - drift3 * 0.24 + vec3(4.2, 8.1, 1.5));
        veins = ridged3(q * 1.85 + vec3(mist * 2.5, mist * 1.6, mist * 1.1) - drift3 * 0.55);
        veins = pow(clamp(veins, 0.0, 1.0), 2.4);

        stripeA = 1.0 - abs(sin(dot(q, vec3(1.08, 0.42, 0.71)) * 1.7 + time * 0.85 + mist * 4.3));
        stripeB = 1.0 - abs(sin(dot(q, vec3(-0.58, 1.12, 0.34)) * 1.45 - time * 0.65 - mist * 2.9));
        stripeA = pow(clamp(stripeA, 0.0, 1.0), 4.8);
        stripeB = pow(clamp(stripeB, 0.0, 1.0), 5.4);
    } else {
        vec2 flow = uv * mix(1.2, 3.2, clamp(scale / 3.0, 0.0, 1.0));
        vec2 drift = vec2(t * 0.20, -t * 0.15);

        vec2 warp = vec2(
            fbm(flow * 0.90 + drift * 0.75 + vec2(0.0, 4.1)),
            fbm(flow * 0.78 - drift * 0.48 + vec2(3.7, 1.8))
        );
        vec2 q = flow + (warp - 0.5) * 1.8;

        mist = fbm(q * 0.72 - drift * 0.24 + vec2(4.2, 8.1));
        veins = ridged(q * 1.85 + vec2(mist * 2.5, mist * 1.6) - drift * 0.55);
        veins = pow(clamp(veins, 0.0, 1.0), 2.4);

        stripeA = 1.0 - abs(sin((q.x * 1.08 + q.y * 0.42) * 1.7 + time * 0.85 + mist * 4.3));
        stripeB = 1.0 - abs(sin((q.x * -0.58 + q.y * 1.12) * 1.45 - time * 0.65 - mist * 2.9));
        stripeA = pow(clamp(stripeA, 0.0, 1.0), 4.8);
        stripeB = pow(clamp(stripeB, 0.0, 1.0), 5.4);
    }

    float energy = clamp(mist * 0.22 + veins * 0.88 + stripeA * 0.55 + stripeB * 0.32, 0.0, 1.0);
    float core = smoothstep(0.18, 0.98, energy);
    float accent = pow(clamp(max(veins, stripeA), 0.0, 1.0), 1.25);

    vec3 outlineColor = mix(color, color2, 0.35);
    vec3 shaderColor = mix(color, color2, clamp(core * 0.75 + stripeB * 0.25, 0.0, 1.0));

    float innerMask = clamp(mask - edgeBand * 0.58, 0.0, 1.0);
    float fillStrength = fill * innerMask * (0.26 + core * 0.82 + accent * 0.28);
    float edgeStrength = edgeBand * (0.34 + glow * 0.12);

    vec3 rgb = shaderColor * fillStrength + outlineColor * edgeStrength;
    float outAlpha = clamp(alpha * (fillStrength * 0.92 + edgeBand * 0.48) * mask, 0.0, 1.0);

    if (outAlpha <= 0.001) discard;
    OutColor = vec4(rgb, outAlpha);
}
