#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 resolution;

#define MAX_ITER 16
void main( void ) {
	vec2 p = (gl_FragCoord.xy / resolution * 4.0) - vec2(15.0);
	vec2 i = p;
	float c = 1.0;
	float inten = .05;

	for (int n = 0; n < MAX_ITER; n++){
		float t = time * (1.5 - (2.0 / float(n+1)));
		i = p + vec2(cos(t - i.x) + sin(t + i.y), sin(t - i.y) + cos(t + i.x));
		c += 1.0/length(vec2(p.x / (2.*sin(i.x+t)/inten),p.y / (cos(i.y+t)/inten)));
	}
	c /= float(MAX_ITER);
	c = 1.5-sqrt(pow(c,3.2));
	float col = c*c*c*c;
	gl_FragColor = vec4(vec3(col * 0.1, col * 0.5, col * 1.2), 1.0);
}