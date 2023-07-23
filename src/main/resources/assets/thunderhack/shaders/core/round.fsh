#version 150

uniform vec4 color;
uniform vec2 uSize;
uniform vec2 uLocation;
uniform float Size;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

void main() {
    float distance = roundedBoxSDF(gl_FragCoord.xy - uLocation - (uSize / 2.0), uSize / 2.0, Size);
    float smoothedAlpha = 1.0 - smoothstep(-1.0, 1.0, distance);
    fragColor = vec4(color.rgb, color.a * smoothedAlpha);
}

