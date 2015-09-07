#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

float tri(in float x){return abs(fract(x)-.5);}
vec3 tri3(in vec3 p){return vec3( tri(p.z+tri(p.y*1.)), tri(p.z+tri(p.x*1.)), tri(p.y+tri(p.x*1.)));}

float triNoise3d(in vec3 p, in float spd)
{
	float z=1.4;
	float rz = 0.;
 	vec3 bp = p;
	for (float i=0.; i<=3.; i++ )
	{
		vec3 dg = tri3(bp*2.);
		p += (dg+time*spd);

		bp *= 1.8;
		z *= 1.5;
		p *= 1.2;

		rz+= (tri(p.z+tri(p.x+tri(p.y))))/z;
		bp += 0.14;
	}
	return rz;
}
float map(vec3 p) {
	p.y *= 1.0;
	float d = length(p) - 0.5 + 0.02 * triNoise3d(p * 2.0, 0.013);
	return d;
}

vec3 calcNormal(vec3 p) {
	vec2 e = vec2(-1.0, 1.0) * 0.0001;
	vec3 nor = e.xyy*map(p+e.xyy) + e.yxy*map(p+e.yxy) + e.yyx*map(p+e.yyx) + e.xxx*map(p+e.xxx);
	return normalize(nor);
}

void main( void ) {

	vec2 p = ( gl_FragCoord.xy / resolution.xy );
	p = 2.0 * p - 1.0;
	p.x *= resolution.x / resolution.y;
	float color = 0.0;

	float t0 = time * 0.11;
	float t1 = t0 + 0.01;
	float r0 = 0.5;
	float k = 0.5 + 0.5 * sin(time * 0.2);
	k = k*k*(3.0 - 2.0*k);
	k = pow(k, 3.0);
	float r1 = 0.5 + k * 2.0;
	vec3 ro = vec3(0.0, r1 * cos(t0), r1 * sin(t0));
	vec3 ta = vec3(0.0, r0 * cos(t1), r0 * sin(t1));
	vec3 cw = normalize(ta - ro);
	vec3 up = normalize(ro - vec3(0.0));
	vec3 cu = normalize(cross(cw, up));
	vec3 cv = normalize(cross(cu, cw));

	vec3 rd = normalize(p.x * cu + p.y * cv + 3.0 * cw);
	float d = length(rd.xy);
	float e = 0.0001;
	float h = e * 2.0;
	float t = 0.0;
	for(int i = 0; i < 60; i++) {
		if(abs(h) < e || t > 20.0) continue;
		h = map(ro + rd * t);
		t += h;
	}
	vec3 pos = ro + rd * t;
	vec3 nor = calcNormal(pos);
	vec3 lig = normalize(vec3(1.0));
	float dif = clamp(dot(nor, lig), 0.0, 1.0);
	float fre = clamp(1.0 + dot(nor, rd), 0.0, 1.0);
	float spe = pow(clamp(dot(reflect(rd, nor), lig), 0.0, 1.0), 32.0);
	color = fre + spe;
	if(t > 20.0) color = 0.2 / d;

	gl_FragColor = vec4( vec3( color ), 1.0 );

}