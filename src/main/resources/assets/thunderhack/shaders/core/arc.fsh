#version 150

uniform vec4 color1;
uniform vec4 color2;
uniform float radius;
uniform float thickness;
uniform float start;
uniform float end;
uniform vec2 uSize;
uniform vec2 uLocation;
uniform float time;


out vec4 fragColor;

#define PI 3.141592653589793
#define RAD 0.0174533

void main() {
    float startAngle = start * RAD;
    float endAngle = end * RAD;
    startAngle = clamp(startAngle, -90.0, endAngle);
    endAngle = clamp(endAngle, startAngle, PI * 2);

    float smoothThresh = 6.0 * (1.0 / length(uSize));
    vec2 centerPos = ((gl_FragCoord.xy - uLocation) / uSize.xy) * 2.0 - 1.0;

    float dist = length(centerPos);
    float bandAlpha = smoothstep(radius, radius + smoothThresh, dist) * smoothstep(radius + thickness, (radius + thickness) - smoothThresh, dist);
    float angle = (atan(centerPos.y, centerPos.x) + PI);
    float angleAlpha = smoothstep(angle, angle - smoothThresh, startAngle) * smoothstep(angle, angle + smoothThresh, endAngle);

    float angle2 = (time + (angle / PI * 180.));
    angle2 = angle2 - 360. * floor(angle2 / 360.);
    if (angle2 >= 180.) {
        angle2 = (360. - angle2) * 2.;
    } else {
        angle2 = angle2 * 2.;
    }
    fragColor = mix(color1, color2, angle2 / 360.) * bandAlpha * angleAlpha;
}
