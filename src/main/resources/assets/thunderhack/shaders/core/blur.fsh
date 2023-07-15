#version 150

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec4 ColorModulator;

uniform float Directions;
uniform float Quality;
uniform float Size;

out vec4 fragColor;

void main() {
    #define TAU 6.28318530718
    vec2 Radius = Size / InputResolution.xy;
    vec2 uv = gl_FragCoord.xy / InputResolution.xy;
    vec4 Color = texture(InputSampler, uv);

    for (float d = 0.0; d < TAU; d += TAU / Directions) {
        for (float i = 1.0 / Quality; i <= 1.0; i += 1.0 / Quality) {
            Color += texture(InputSampler, uv + vec2(cos(d), sin(d)) * Radius * i);
        }
    }
    Color /= Quality * Directions;
    fragColor = Color * ColorModulator;
}
