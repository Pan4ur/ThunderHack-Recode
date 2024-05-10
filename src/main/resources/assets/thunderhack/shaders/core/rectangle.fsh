#version 150

uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

uniform vec2 uSize;
uniform vec2 uLocation;
uniform float radius;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){
    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
    color += mix(0.0019607843, -0.0019607843, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
    return color;
}

void main() {
    float distance = roundedBoxSDF(gl_FragCoord.xy - uLocation - (uSize / 2.0), uSize / 2.0, radius);
    float smoothedAlpha = (1.0 - smoothstep(-1.0, 1.0, distance)) * color1.a;
    fragColor = vec4(createGradient((gl_FragCoord.xy - uLocation) / uSize, color1.rgb, color2.rgb, color3.rgb, color4.rgb), smoothedAlpha);
}