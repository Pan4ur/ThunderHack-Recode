#version 150

uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

uniform vec2 uSize;
uniform vec2 uSize2;
uniform vec2 uLocation;

uniform float radius;

uniform float blend;
uniform float alpha;

uniform float outline;
uniform float glow;

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
    vec2 centeredCoord = gl_FragCoord.xy - uLocation - (uSize / 2.0);
    vec2 normCoord = (gl_FragCoord.xy - uLocation) / uSize;

    float distance = roundedBoxSDF(centeredCoord, uSize / 2.0, radius);
    float smoothedAlpha = (1.0 - smoothstep(-10, 10, distance)) * color1.a;
    float smoothedAlpha2 = (1.0 - smoothstep(-1., 1., distance)) * color1.a;

    vec3 gradientColor = createGradient(normCoord, color1.rgb, color2.rgb, color3.rgb, color4.rgb);

    if (smoothedAlpha2 < glow) {
        fragColor = vec4(gradientColor, smoothedAlpha);
    } else {
        float distance1 = roundedBoxSDF(centeredCoord, (uSize / 2.0) + 0.5 - outline, radius);
        float blendAmount = smoothstep(0., 2., abs(distance1) - outline);
        vec3 insideColor = createGradient(normCoord, color1.rgb / blend, color2.rgb / blend, color3.rgb / blend, color4.rgb / blend);
        vec4 insideColorVec = (distance1 < 0.) ? vec4(insideColor, alpha) : vec4(insideColor, 0.0);
        fragColor = mix(vec4(gradientColor, 1.), insideColorVec, blendAmount);
    }
}