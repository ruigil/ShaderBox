#ifdef GL_ES
	precision mediump float;
#endif
// time in s
uniform float time;

// mouse position in normalized coordinates 0,1
uniform vec2 mouse;

// resolution in pixels
uniform vec2 resolution;

//affine transformation matrix of eye in camera space
//uniform mat4 eye;

void main()
{
	vec2 uv = gl_FragCoord.xy/resolution.xy;

	gl_FragColor = vec4( vec3( uv.x, uv.y, 0.0 ), 1.0 );
}
