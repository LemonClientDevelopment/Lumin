#version 150

uniform sampler2D InputSampler;
layout(std140) uniform BlurUniforms {
    vec4 Params1;
    vec4 Params2;
    vec4 Color1;
    vec4 Params3;
};

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

vec4 blur() {
    #define TAU 6.28318530718

    vec2 inputResolution = Params1.xy;
    float Quality = Params1.z;
    vec2 Radius = Quality / inputResolution.xy;

    vec2 uv = gl_FragCoord.xy / inputResolution.xy;
    vec4 Color = texture(InputSampler, uv);

    float step =  TAU / 16.0;

    for (float d = 0.0; d < TAU; d += step) {
        for (float i = 0.2; i <= 1.0; i += 0.2) {
            Color += texture(InputSampler, uv + vec2(cos(d), sin(d)) * Radius * i);
        }
    }

    Color /= 81.0;
    return (Color + Color1);
}

void main() {
    vec2 uSize = Params2.xy;
    vec2 uLocation = Params2.zw;
    float radius = Params3.x;
    float Brightness = Params1.w;

    vec2 halfSize = uSize / 2.0;
    float smoothedAlpha = (1.0 - smoothstep(0.0, 1.0, roundedBoxSDF(gl_FragCoord.xy - uLocation - halfSize, halfSize, radius)));
    fragColor = vec4(blur().rgb, smoothedAlpha * Brightness);
}