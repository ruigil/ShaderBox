#ifdef GL_ES
precision mediump float;
#endif
                                  struct Z{//2
                             vec3 a;float b,c,d,e,f
                        ;};struct Y{vec3 a,b,c;float d,e
                   ;Z f;};float a;uniform float  time;uniform
             vec2 resolution;Z l(vec3 b,float c,float d){if(d==1.){
          float e=clamp(b.y+.6,0.,1.);return Z(mix(vec3(.47843,.23137,
        .078431),vec3(.52941),e),.4,.7,mix(1e3,10.,e),0.,-.3);}else if(d
      ==2.)return Z(mix(vec3(.85882),      vec3(.32157),clamp(sign(b.z),0.
     ,1.)),.3,.7,1e3,0.,0.);else if          (d==3.)return Z(vec3(0),.5,.5,
   0.,(sign(b.z)+1.  )/2.+1.,0.);              else if(d==4.)return Z(abs(pow
  (clamp(b.y-.2,0.,1. ),2.)-mix(                vec3(.85882),vec3(.3098,.31373
 ,.32157),abs(a))),.3,.7,abs(a)                  *1e2+1e-5,0.,0.);else if(d==5.
)return Z(vec3(.32157) ,.05,.5,                  10.,0.,0.); }vec3 m(vec3 b,vec3
c){return floor(b.x/1e2+.5)==0.                  ?mix(vec3(.37647,.062745,.78824
),vec3(.066667,.078431,.21961),                  c.y*2.5):floor(b.x/1e2+.5)==1.?
mix(vec3(.98824,1,.85098),vec3(                  .61176,.77255,1) ,c.y* 6.):mix(
vec3( .67059 ,.082353 ,.16078),                  vec3(.21176,.043137, .066667) ,
c.y *5.);}vec3  n(vec3  b,Y  c,                  float d,float e){return mix(b,m
(c.b*(1.-e),c.c),clamp ((mix(d,                  c.e,e)-6.)/14.*1.5,0.,1.));}//2
float o(vec2 b){vec4 c,d,e,f,g;                  c=mod(floor(b.xyxy)+vec4(0,0,1,
1),289.);d=fract(b.xyxy)-vec4(0                  ,0,1,1);e=fract(mod(((mod((//1@
c.xzxz *  34.+1.)* c.xzxz,289.)                  +c.yyww)*34.+1.)* (mod((c.xzxz*
34.+1.)  *c.xzxz,289.)+c.yyww),                  289. )/41.)*2.-1.;f=abs(e)-.5;e
-=floor(e+.5)   ;g=inversesqrt(                  sqrt(e*e+f*f));vec2 h,i;h=((//#
d.xy*6.-15.)*d.xy+10.)*pow(d.xy                  ,vec2(2) );i=mix(vec2(dot(vec2(
e.x,f.x)*g.x,d.xy),dot(vec2(e.z                  ,f.z)*g.y,d.xw)),vec2(dot(vec2(
e.y,f.y)*g.z,d.zy),dot(vec2(e.w                  ,f.w)*g.w,d.zw)),h.x);return//2
mix(i.x,i.y,h.y )*2.3;}float p(                  vec3 b,vec3 c){vec3 d=abs(b)-c;
return min(max(max(d.x,d.y),d.z                  ),0.)+length(max(d,0.));}vec2 q
(vec2 b,float c) {float  d,e;d=                  cos(c);e=sin(c);return vec2(b.x
*d-b.y* e,b.y*d+b.x*e);}vec3 r(                  vec3 b){return mix(vec3(1),///~
clamp(abs(mod(b.x*6.+vec3(0,4,2                  ),6.) -3.)-1.,0.,1.),b.y)*b.z;}
float s(float b,float c,float d                  ){float e= clamp(.5+.5*(c-b)/d,
0.,1.) ;return mix(c,b,e)-d*e*(                  1.-e) ;}vec2 t(vec3 b ){vec2 c;
float d,e;d=floor(b.x/1e2+.5);b                  -=vec3(d*1e2,0,0);e=1.-abs(sign
( d-1.) );a=o((b.xz*vec2(1.5,1)                  -vec2(mix(0.,-time/1.5,e) ,mix(
0.,time,e+1.-abs( sign(d-2.)))*                  mix(1.,.3,e))+b.y)*mix(.75,3.-d
,abs(sign(d-1.))) );if (d==0.){                  vec3 f,g;f=(b+vec3(0,1,0)-vec3(
0,.6,0))/vec3((1.-(b+vec3(0,1,0                  )).y)*.05-1.,2.5,2)/*4*/;g=mix(
b+vec3(0,1,0),mod(b+vec3(0,1,0)                  ,vec3(0,0,1)),vec3(0,0,1))-vec3
(0, 0,.5);vec2  h,i;h=vec2(s(s(                  length(vec2(length(f.xy)-1.,f.z
))-.1,length(b+vec3(0,1,0)+vec3                  (0,4.8,0))-5.,1.25),o((b+vec3(0
,1,0)).xz/2.)/mix(20.,2.,clamp(                  abs((b+vec3(0,1,0)).x/6.),0.,1.
))- -(b+vec3(0,1,0)).y,.2),1);i                  =vec2(p (vec3(q(g.xz,sin(((b-g)
.x+(b-g).z)*13.7)*.5),g.y).xzy,                  vec3(.4,.1,.4)),2);h=h.x<i.x?h:
i;i=vec2(max(length( (b+vec3(0,                  .3,0))  /vec3(1,2.5,1))-1.,p(b,
vec3(1,3,.04))),3);c=h.x<i.x?h:                  i;}else if(d==1.){b=b*vec3(//0_
1.+b.z*-.04,1,1)+vec3(0,b.z*.05                  ,0);vec3 f=mix(b,mod(b,vec3(//&
2.25,0,2.5)),vec3(1,0,1))-vec3(                  1.125,0,1.25);c=vec2(max(s(dot(
b+vec3(0,1,0),vec3(0,1,0)),s(p(                  vec3(q(f.xz,f.y*-sign(b.x)*//#]
1.5708+.3927),f.y).xzy+vec3(0,1                  ,0),vec3(.2,1.5,.2)),length(//1
f-vec3(0,1.25,0))-.4,.75),.75),                  p(b-vec3(0,0,20.2),vec3(1.5,20,
20))),4);}else c/*]*/=vec2(dot(                  b+vec3(0,b.z/4.,0),vec3(0,1,0))
+1.+clamp(a+sin(b.x+ (time+1.)*                  2.)*.1,-.3,.6)*.4,5);return//8*
c+vec2(a*.03 *abs(sign (d-1.))*                  abs(sign(c.y-3.)),0); }float u(
vec3 b,vec3 c){float d,f,g;vec3                  e=normalize(c-b) ;f=1.;g=length
(c-b);d=20.;for(int h=0;h<3;++h                  ){vec2 i=t(b+e*2.*f) ;d=min (d,
i.x);f+=max(abs(i.x),.1);if(abs                  (i.x)<.1||f>=g)break;}return//.
smoothstep(max(d,0.),0.,.15849)                  ;}Y v(vec3 b,vec3 c){float d,e,
f,h,i;vec2 g;d=.005;e=0.;h=.01;                  i=20.;f=0.;for(int j=0;j<40;++j
){g=t(b+c*d); if(g.x>20.)return                  Y(b,b,c,20.,e,Z(vec3(0), 0.,0.,
0.,0.,0.));if(g.x<=.005+.0025*d                  )if(g.y==3.){e+=d;d=0.;Z k=l(b,
0.,3.);f=k.e;b+=vec3(f*1e2,0,0)                  ;}else break;else{d+=abs(g.x)+h
;if(d>20.)return Y(b,b,c,20.,e,                  Z(vec3 (0),0.,0.,0.,0.,0.));if(
g.x>=i&&f==0.)h+=.0075;i=g.x;}}                  vec3 k=b+c*(d+g.x);return Y(k,b
,c,g.x,e,l(k,g.x,g.y));}float w                  (vec3 b,vec3 c, float d){return
smoothstep(0.,min ( t(b+c*d).x,                  20.),d /1.75);}vec3  x(vec3 b){
float c= max( time-8.,0.) /3.5;                  return vec3(q(b.xz,-(pow(clamp(
c,0.,1.),1.2)+max(c-1.,0.) ) ),                  b.y).xzy;}vec3 y(vec3 b){vec4 c
,d,e ;c=vec4(0 ,-.33333,.66667,                  -1.);d=b.g<b.b?vec4(b.bg,c.wz):
vec4(b.gb,c.xy);e=b.r<d.x?vec4(                  d.xyw,b.r):vec4(b.r,d.yzx);//~8
float f=e.x-min(e.w,e.y);return                  vec3(abs(e.z+(e.w-e.y)/(6.*//({
f+1e-5)),f/ ( e.x+1e-5), e.x);}                  vec3 z(Y b){float c,d,h,i;d=//7
floor(b.a.x/1e2+.5);vec3 e,f,g;                  e=normalize(vec3(t(b.a+vec3(//]
1e-5,0,0)).x-t(b.a+vec3(-1e-5,0                  ,0)).x,t(b.a+vec3(0,1e-5,0))//@
.x-t(b.a+vec3(0 ,-1e-5,0)).x,t(                  b.a+vec3(0,0,/*^/|*/1e-5)).x-t(
b.a+vec3(0,0,-1e-5)).x));f=vec3                  (d*1e2,5,mix(5.,-1.,abs(d-1.)))
;g=normalize(f-b.a);h=max(dot(g                  ,e),0.);c=0.;if(b.f.d!=0.&&h>0.
)c=pow(max(dot(normalize(g+b.b)                  ,e),0.),b.f.d)/length(f-b.a);i=
u(b.a,f) ;return n( r(y(b.f.a)*                  vec3(1,1,h*b.f.c*i+w(b.a,e,b.d)
*b.f.b+c*i+abs(e.x )*b.f.f)),b,                  length(b.b-b.a),0.);}void main(
){vec3 b,d;/*12(8,,,$0-2-*/b=x(                  /*^|77,)1@(3!*_#&7{\[=~-.~(~-*/
vec3(0,.5,-4.-pow(clamp(1.-time                  /10.,0.,1.),3.)*1e2+sin(max//69
(time-10.,0.)/4.)*.1) );vec2 c=(                gl_FragCoord.xy/resolution*2.-1.
)*vec2 (resolution.x/resolution.y              ,1);d/*3.1+{4@*/=normalize(x(vec3
(0,.5, -1.) )-b);Y e=v( b,normalize          (cross(d,vec3(0,1,0))*c.x+vec3(//_^
 0,1,0)*c.y+d*2.));if(e.d<.05)gl_FragColor=vec4(mix(vec3(1),z(e),clamp((time-.5
   )/2.,0.,1.)),1);else{vec3 f=m(e.b/*$9*/,e.c);gl_FragColor=vec4(mix(vec3(1)
     ,mix(f,n(f,e,20.,1.),min(e.e*20.,1.)),clamp((time-.5)/2.,0.,1.)),1);}}