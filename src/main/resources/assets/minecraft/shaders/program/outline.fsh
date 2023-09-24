#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
in vec2 oneTexel;
uniform vec4 color;
uniform vec4 outlinecolor;
out vec4 fragColor;
uniform int quality;
uniform int lineWidth;

uniform vec2 InSize;
uniform float alpha0;


void main() {
    vec4 centerCol = texture(DiffuseSampler, texCoord);

    if(centerCol.a != 0) {
        fragColor = color;
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

