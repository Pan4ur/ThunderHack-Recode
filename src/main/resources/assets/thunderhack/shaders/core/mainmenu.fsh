#version 150

uniform vec2 uSize;
uniform float Time;
uniform vec4 color;

out vec4 fragColor;

vec2 rotz(in vec2 p, float ang) {
    return vec2(p.x*cos(ang)-p.y*sin(ang),p.x*sin(ang)+p.y*cos(ang));
}

void main( void ) {

    vec2 p = 0.5*( gl_FragCoord.xy / uSize.xy );
    p.x *= uSize.x/uSize.y;
    vec3 col = vec3(-.4);

    p = rotz(p, Time*0.35+atan(p.y,p.x)*0.5);
    p *= 1.1;

    for (int i = 0; i < 4; i++) {
        float dist = abs(p.y + sin(float(i)+Time*2.+4.0*p.x));

        if (dist < 1.0)
            col += (1.0-pow(abs(dist), 0.25)) * color.rgb;

      //  col += (1.0-pow(abs(dist), 0.25)) * vec3(0.5 + 0.25*sin(p.y*4.0+Time*5.), 0.7 + 0.3*sin(p.x*5.0+Time*5.5), 1);

        p.xy *= 1.5;
        p = rotz(p, 2.0);
    }
    fragColor = vec4(col/1.5, 0.5);
}