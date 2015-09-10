#ifdef GL_ES
    precision mediump float;
#endif

uniform vec2 resolution;
uniform float time;

void main()
{
   vec2 p = 2.0*gl_FragCoord.xy/resolution;
   for(int n=1; n<10; n++)
   {
      float i = float(n);
      p += vec2(
        0.7/i*sin(i*p.y+time+0.3*i)+0.8,
        0.4/i*sin(i*p.x+time+0.3*i)+1.6
      );
   }
   gl_FragColor = vec4(0.5*sin(p.x)+0.5,0.5*sin(p.y)+0.5,sin(p.x+p.y),1.0);
}