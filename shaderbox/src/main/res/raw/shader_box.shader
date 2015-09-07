precision highp float;

// time in ms
uniform float time;

// touch position in normalized coordinates 0,1
uniform vec2 touch;

// resolution in pixels
uniform vec2 resolution;

//rotation and translation matrix of eye in camera space
uniform mat4 camera;

#define eps 0.001
#define pi 3.1415

struct Ray {
  vec3 origin;
  vec3 position;
  vec3 direction;
  vec3 normal;
  vec3 view;
  float penumbra;
};

struct Camera {
  vec3 eye;
  vec3 lookat;
};

float plane(vec3 position) {
   return position.y;
}

float sphere(vec3 position, float r) {
   return length(position) - r;
}

float box( vec3 position, vec3 b ) {
  vec3 d = abs(position) - b;
  return min(max(d.x, max(d.y, d.z)), 0.0) + length(max(d, 0.0));
}

vec3 oRepeat(vec3 position, vec3 translation) {
   return mod(position,translation) - 0.5*translation;
}

vec2 oUnion(vec2 o1, vec2 o2) {
  return o1.x < o2.x ? o1 : o2;
}

vec2 oInter(vec2 o1, vec2 o2) {
  return o1.x < -o2.x ? vec2(-o2.x,o2.y) : o1;
}

vec2 scene(vec3 position) {

  vec2 sphere = vec2(sphere(position-vec3(.0,-4.0,0.0),1.0),2.0);
  vec2 plane = vec2(plane(position-vec3(.0,-4.0,0.0)),1.0);
  //vec2 box2 = vec2(box(position-vec3(1.0,0.0,1.0),vec3(1.0)),2.0);
  //vec2 box = vec2(box(position-vec3(0.0,1.0,0.0),vec3(1.0)),2.0);

   //return oUnion(plane,oInter(oInter(box,sphere),box2));
   return oUnion(plane,sphere);
}

vec3 getNormal(vec3 position) {
    vec3 e = vec3(eps,0.0,0.0);

    return normalize(vec3(
       scene(position + e.xyz).x - scene(position - e.xyz).x,
       scene(position + e.yxz).x - scene(position - e.yxz).x,
       scene(position + e.yzx).x - scene(position - e.yzx).x));
}

void material(inout Ray ray, vec2 object, inout vec3 acolor, inout vec3 dcolor, inout vec3 scolor) {

    scolor = vec3(1.0);
    acolor = vec3(0.2);

  if (object.y == 2.0) {
    dcolor = vec3(0.5,0.5,0.5);
    acolor = vec3(.0,0.4,0.);
    //acolor = eye[3].xyz;
  }
  if (object.y == 1.0) {
      float f = mod( floor(ray.position.z) + floor(ray.position.x), 2.0);
      dcolor = vec3( 0.5 + f);
      //scolor = vec3(0.0);
      acolor = vec3(0.0);

  }
  if (object.y == 0.0) {
    acolor = vec3(.0,.0,.2);
    dcolor = vec3(.0,0.0,0.2);
    //scolor = vec3(0.0);
  }

}

vec2 shootRay(inout Ray ray) {
  float raystep = 0.0;
  float maxdis = 30.0;
  ray.penumbra = 1.0;

  vec2 object;

  for (int i=0; i<64; i++) {
    ray.position = ray.origin + ray.direction * raystep;
    object = scene(ray.position);
    // penumbra factor for soft shadows
    //ray.penumbra = min(ray.penumbra,8.0*object.x/raystep);
    raystep += object.x;
    if (object.x<eps || raystep>maxdis) break;
  }

  // sky material
  object.y = raystep < maxdis ? object.y : 0.0;

  return object;
}

float shadow(inout Ray ray, vec3 dlight) {
  ray.origin = ray.position + dlight;
  ray.direction = dlight;
  vec2 object = shootRay(ray);
  return 0.5 + smoothstep(ray.penumbra,.0,0.5);
}

vec3 light(vec2 object, inout Ray ray) {

  vec3 acolor = vec3(0.5,0.0,0.0);
  vec3 dcolor = vec3(0.0);
  vec3 scolor = vec3(0.0);

  material(ray, object, acolor, dcolor, scolor);

  vec3 color = acolor;
  if (object.y != 0.0) {

     vec3 plight = vec3(4.*sin(time),4.0,4.0*cos(time));

      vec3 dlight = normalize(plight - ray.position);
     float lambert = max(dot(dlight, ray.normal),0.0);
     vec3 halfv = normalize(dlight + ray.view);
     float specular = pow(max(dot(halfv,ray.normal),0.0),128.0);

     color += lambert * dcolor;
     color += specular * scolor;
     color = mix(color,vec3(0.0,0.0,0.2),1.0-exp(-length(ray.position)*0.07));

     // if lambert factor is zero there is no light path
     color *= lambert != 0.0 ? shadow(ray,dlight) : 1.0;
  }
  //color = mix(color,vec3(0.0,0.0,0.2),1.0-exp(-length(ray.position)*0.07));
  //color = pow(color,vec3(0.45));
  return color;
}

Ray setupRay() {
  Ray ray;
  vec2 uv = -1.0 + 2.0 * gl_FragCoord.xy / resolution;
  uv.x *= resolution.x / resolution.y;

  vec3 right = camera[0].xyz;
  vec3 up = camera[1].xyz;
  vec3 forward = -camera[2].xyz;

  vec3 eyep = camera[3].xyz;

  ray.position =  eyep + forward + ((right * uv.x) + (up * uv.y));
  ray.direction = normalize(ray.position - eyep);
  ray.origin = eyep;

  return ray;
}

void main() {

    Ray ray = setupRay();
    vec2 object = shootRay(ray);

    ray.normal = getNormal(ray.position);
    ray.view = -ray.direction;

    vec3 color = light(object,ray);

    gl_FragColor = vec4(color,1.0);
}