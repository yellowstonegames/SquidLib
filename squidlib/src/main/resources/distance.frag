#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float scale;

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {
    float distance = texture2D(u_texture, v_texCoord).a;
    float alpha = smoothstep(0.5 - 0.08 / scale, 0.5 + 0.08 / scale, distance) * v_color.a;
    gl_FragColor = vec4(v_color.rgb, alpha);
}