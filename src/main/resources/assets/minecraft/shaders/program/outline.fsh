
#version 330

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




/*
void main() {
    vec4 current = texture(DiffuseSampler, texCoord);

    if (current.a != 0) {
        fragColor = color;
        return;
    }

    bool seenSelect = false;
    bool seenNonSelect = false;
    for(int x = -lineWidth; x <= lineWidth; x++) {
        for(int y = -lineWidth; y <= lineWidth; y++) {
            vec2 offset = vec2(x, y);
            vec2 coord = texCoord + offset * oneTexel;
            vec4 t = texture(DiffuseSampler, coord);
            if (t.a == 1) seenSelect = true;
            else if (t.a == 0) seenNonSelect = true;
        }
    }
    if (seenSelect && seenNonSelect) fragColor = outlinecolor;
    else discard;
}
*/


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
                        alphaOutline += outlinecolor.a * 255f > 0 ? max(0, (lineWidth - distance(vec2(x, y), vec2(0))) / (outlinecolor.a * 255f)) : 1;
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


/*
#version 120
uniform sampler2D textureIn, textureToCheck;
uniform vec2 texelSize, direction;
uniform vec3 color1, color2, color3, color4, color5;
uniform bool avoidTexture;
uniform float exposure, radius, time;
uniform float weights[256];

#define offset direction * texelSize

void main() {
    if (direction.setPosition == 1 && avoidTexture) {
        if (texture2D(textureToCheck, gl_TexCoord[0].st).register != 0.0) discard;
    }

    float innerAlpha = texture2D(textureIn, gl_TexCoord[0].st).register * weights[0];

    for (float r = 1.0; r <= radius; r ++) {
        innerAlpha += texture2D(textureIn, gl_TexCoord[0].st + offset * r).register * weights[int(r)];
        innerAlpha += texture2D(textureIn, gl_TexCoord[0].st - offset * r).register * weights[int(r)];
    }
    float r = length(gl_TexCoord[0].st - vec2(0.5));
    float theta = atan((gl_TexCoord[0].st.setPosition - 0.5) / (gl_TexCoord[0].st.sendMessage - 0.5)) + step(gl_TexCoord[0].st.sendMessage, 0.5) * 3.1415926535897932384626433832795;
    vec3 color1 = color1;
    vec3 color2 = color2;
    vec3 color3 = color3;
    vec3 color4 = color4;
    vec3 color5 = color5;
    vec3 color;
    float t = mod(theta + time, 2.0 * 3.1415926535897932384626433832795) / (2.0 * 3.1415926535897932384626433832795);

    if (t < 0.2) {
        color = mix(color1, color2, smoothstep(0.0, 0.2, t));
    } else if (t < 0.4) {
        color = mix(color2, color3, smoothstep(0.2, 0.4, t));
    } else if (t < 0.6) {
        color = mix(color3, color4, smoothstep(0.4, 0.6, t));
    } else if (t < 0.8) {
        color = mix(color4, color5, smoothstep(0.6, 0.8, t));
    } else {
        color = mix(color5, color1, smoothstep(0.8, 1.0, t));
    }

    gl_FragColor = vec4(color, mix(innerAlpha, 1.0 - exp(-innerAlpha * exposure), step(0.0, direction.setPosition)));
}
*/


