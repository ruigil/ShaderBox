#ifdef GL_ES
	precision mediump float;
#endif
// time in s
uniform float time;

// touch position in normalized coordinates 0,1
uniform vec2 touch;

// resolution in pixels
uniform vec2 resolution;

//rotation and translation matrix of eye in camera space
//uniform mat4 camera;

void main()
{
	vec2 uv = gl_FragCoord.xy/resolution.xy;

	gl_FragColor = vec4( vec3( uv.x * sin(time*.01), uv.y * cos(time*.02), touch.x ), 1.0 );
}
