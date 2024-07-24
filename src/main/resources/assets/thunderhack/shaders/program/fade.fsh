#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D DiffuseSampler;
uniform vec4 primaryColor;
uniform vec4 secondaryColor;
uniform float time;
uniform vec2 InSize;

uniform vec4 outlinecolor;
uniform float alpha0;
uniform float fillAlpha;
uniform int quality;
uniform int lineWidth;
in vec2 oneTexel;

vec3 wave(vec2 pos)
{
    return mix(primaryColor.rgb, secondaryColor.rgb, sin((distance(vec2(0), pos) - time * 60.0) / 60.) * 0.5 + 0.5);
}

void main()
{
    vec4 centerCol = texture(DiffuseSampler, texCoord);

    if (centerCol.a != 0) {
        fragColor = vec4(wave(gl_FragCoord.xy), fillAlpha);
    } else {
        float alphaOutline = 0;
        vec3 colorFinal = vec3(-1);
        for (int x = -quality; x < quality; x++) {
            for (int y = -quality; y < quality; y++) {
                vec2 offset = vec2(x, y);
                vec2 coord = texCoord + offset * oneTexel;
                vec4 t = texture(DiffuseSampler, coord);
                if (t.a != 0){
                    if (alpha0 == -1.0) {
                        if (colorFinal[0] == -1) {
                            colorFinal = outlinecolor.rgb;
                        }
                        alphaOutline += outlinecolor.a * 255.0 > 0 ? max(0, (lineWidth - distance(vec2(x, y), vec2(0))) / (outlinecolor.a * 255.0)) : 1;
                    }
                    else {
                        fragColor = vec4(outlinecolor.rgb, alpha0);
                        return;
                    }
                }
            }
        }
        fragColor = vec4(colorFinal, alphaOutline);
    }
}
