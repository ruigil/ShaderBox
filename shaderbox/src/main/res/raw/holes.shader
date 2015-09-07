#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;


float map(vec3 p) {
	float k = cos(p.z * 15.0) * 0.03 + cos(p.x * 15.0) * 0.03;
	return pow((cos(p.x) + cos(p.y)) + sin(p.z) + k, 1.3);
}


vec2 rot(vec2 r, float a) {
	return vec2(
		cos(a) * r.x - sin(a) * r.y,
		sin(a) * r.x + cos(a) * r.y);
}

void main( void ) {
	vec2 uv  = ( gl_FragCoord.xy / resolution.xy ) * 2.0 - 1.0;
	uv.x *= resolution.x / resolution.y ;
	vec3 dir = normalize(vec3(uv, 1.0));
	dir.zy = rot(dir.zy, time * 0.2);
	dir.xz = rot(dir.xz, time * 0.1); dir = dir.yzx;

	vec3 pos = vec3(0, 0, time * 2.0);
	float t = 0.0;
	for(int i = 0 ; i < 100; i++) {
		float temp = map(pos + dir * t) * 0.55;
		if(temp < 0.001) break;
		t += temp;
	}
	vec3 ip = pos + dir * t;
	gl_FragColor = vec4(vec3(max(0.01, map(ip + 0.2)) + t * 0.02) + dir * 0.3, 1.0);

}