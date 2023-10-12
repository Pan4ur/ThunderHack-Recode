#version 150

uniform float time;
uniform vec2 resolution;
uniform sampler2D DiffuseSampler;
in vec2 texCoord;
uniform int quality;
uniform vec4 color;

out vec4 fragColor;

float snow(vec2 uv, float scale)
{
    float w=smoothstep(1., 0., -uv.y*(scale/10.));
    if (w<.1) return 0.;
    uv+=time/scale;uv.y+=time*2./scale;uv.x+=sin(uv.y+time*.5)/scale;
    uv*=scale;vec2 s=floor(uv), f=fract(uv), p;float k=3., d;
    p=.5+.35*sin(11.*fract(sin((s+p+scale)*mat2(7, 3, 6, 5))*5.))-f;d=length(p);k=min(d, k);
    k=smoothstep(0., k, sin(f.x+f.y)*0.01);
    return k*w;
}

float glowShader() {
    float divider = 158.0;
    float maxSample = 10.0;
    vec2 texelSize = vec2(1.0 / resolution.x * quality, 1.0 / resolution.y * quality);
    float alpha = 0;

    for (float x = -quality; x < quality; x++) {
        for (float y = -quality; y < quality; y++) {
            vec4 currentColor = texture(DiffuseSampler, texCoord + vec2(texelSize.x * x, texelSize.y * y));

            if (currentColor.a != 0)
            alpha += divider > 0 ? max(0.0, (maxSample - distance(vec2(x, y), vec2(0))) / divider) : 1;
        }
    }

    return alpha;
}

void main(){
    vec4 centerCol = texture(DiffuseSampler, texCoord);

    vec2 uv=(gl_FragCoord.xy*2.-resolution.xy) / min(resolution.x, resolution.y);
    vec3 finalColor=vec3(0);
    float c=smoothstep(1., 0.3, clamp(uv.y*.3+.8, 0., .75));
    c+=snow(uv, 30.)*.0;
    c+=snow(uv, 20.)*.0;
    c+=snow(uv, 15.)*.0;
    c+=snow(uv, 10.);
    c+=snow(uv, 8.);
    c+=snow(uv, 6.);
    c+=snow(uv, 5.);
    finalColor = vec3(c);
    finalColor *= vec3(color[0],color[1],color[2]);

    float alpha = 0;
    if (centerCol.a != 0) {
        alpha = color[3];
    } else {
        alpha = glowShader();
    }


    fragColor = vec4(finalColor, alpha);
}