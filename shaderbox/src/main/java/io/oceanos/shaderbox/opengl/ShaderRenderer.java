/*
 *    Copyright 2015 Rui Gil
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.oceanos.shaderbox.opengl;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import io.oceanos.shaderbox.MainActivity;
import io.oceanos.shaderbox.database.Shader;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ShaderRenderer implements CardboardView.StereoRenderer {
    public static final int FPS_RESULT = 0;
    public static final int COMPILE_RESULT = 1;
    public static final int THUMB_RESULT = 2;

    public static final String VERTEX_SHADER =
            "attribute vec2 position; void main() { gl_Position = vec4( position, 0., 1. ); } ";
    private static final String VERTEX_TEX_SHADER =
            "attribute vec2 a_position;" +
            "attribute vec2 a_textcoords;" +
            "varying vec2 v_textcoords;" +
            "void main() { gl_Position = vec4( a_position, 0., 1. ); v_textcoords = a_textcoords; } ";
    private static final String FRAGMENT_TEX_SHADER =
            "precision mediump float;" +
            "varying vec2 v_textcoords;" +
            "uniform sampler2D u_textid;" +
            "void main() { gl_FragColor = vec4( texture2D(u_textid, v_textcoords).rgb,1.0); }";

    private ByteBuffer vertexBuffer;
    private ByteBuffer textcoordsBuffer;
    private int timeLoc;
    private int resolutionLoc;
    private int mouseLoc;
    private int eyeLoc;
    private int positionLoc;
    private int position2Loc;
    private int textcoordsLoc;
    private int textsamplerLoc;
    private float resolution[] = new float[]{ 0, 0 };
    private float touch[] = new float[]{ 0, 0 };
    private long startTime;
    private Shader shader = null;
    private int defaultFramebufferId;
    private int frameBufferId = 0;
    private int offscreenTextureId;
    private int[] temp = new int[1];
    private int programShaderId;
    private int programTextureId;
    private long frameCount = 0;
    private String error;
    private int width,height;
    private float[] eyeView;
    private Handler uiHandler;

    public ShaderRenderer(Shader shader, Handler uiHandler) {
        this.shader = shader;
        this.uiHandler = uiHandler;
    }

// ------------- interface stereo renderer methods -------------------------
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        frameCount++;
    }

    @Override
    public void onDrawEye(Eye eye) {
        if (programShaderId != 0) {
            final long now = SystemClock.elapsedRealtime();

            //-- save current default framebuffer                                  1
            GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, temp, 0);
            defaultFramebufferId = temp[0];
            //----------------- render shader to texture---------------------------------
            //---------------------------------------------------------------------------
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
            //-- the size of the offscreen texture
            GLES20.glViewport(0, 0, (int) resolution[0], (int) resolution[1]);

            //-- use shader
            GLES20.glUseProgram(programShaderId);

            GLES20.glVertexAttribPointer(positionLoc, 2, GLES20.GL_BYTE, false, 0, vertexBuffer);
            GLES20.glEnableVertexAttribArray(positionLoc);

            //-- eye.getView gives affine transformation in world space
            //-- inversion to have affine transformation in eyeView space
            android.opengl.Matrix.invertM(eyeView, 0, eye.getEyeView(), 0);

            if( timeLoc > -1 ) GLES20.glUniform1f(timeLoc, (int) (now - startTime) / 1000f) ;

            if( resolutionLoc > -1 ) GLES20.glUniform2fv(resolutionLoc, 1, resolution, 0);

            if( mouseLoc > -1) GLES20.glUniform2fv(mouseLoc, 1, touch, 0);

            if( eyeLoc > -1) GLES20.glUniformMatrix4fv(eyeLoc, 1, false, eyeView, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            //----------------- render texture to screen-------------------------------
            //-------------------------------------------------------------------------
            Viewport view = eye.getViewport();
            //-- restore default framebuffer
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, defaultFramebufferId);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferId);
            GLES20.glViewport(view.x, view.y, view.width, view.height);
            // -- draw texture
            GLES20.glUseProgram(programTextureId);
            GLES20.glVertexAttribPointer(position2Loc, 2, GLES20.GL_BYTE, false, 0, vertexBuffer);
            GLES20.glEnableVertexAttribArray(position2Loc);

            GLES20.glVertexAttribPointer(textcoordsLoc, 2, GLES20.GL_BYTE, false, 0, textcoordsBuffer);
            GLES20.glEnableVertexAttribArray(textcoordsLoc);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(textsamplerLoc, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        } else {
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        resetShader();
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        startTime = SystemClock.elapsedRealtime();
        final byte coords[] = {-1, 1, -1, -1, 1, 1, 1, -1 };
        vertexBuffer = ByteBuffer.allocateDirect(8);
        vertexBuffer.put( coords ).position(0);

        final byte textcoords[] = {0, 1, 0, 0, 1, 1, 1, 0 };
        textcoordsBuffer = ByteBuffer.allocateDirect(8);
        textcoordsBuffer.put( textcoords ).position(0);

        eyeView = new float[16];

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        // ------ initialize offscreen framebuffer
        GLES20.glGenFramebuffers(1, temp, 0);
        frameBufferId = temp[0];
        // ------ initialize texture space
        GLES20.glGenTextures(1, temp, 0);
        offscreenTextureId = temp[0];

        compileRequest();

        programTextureId = compileShaderProgram(VERTEX_TEX_SHADER, FRAGMENT_TEX_SHADER);
        position2Loc = GLES20.glGetAttribLocation(programTextureId, "a_position");
        textcoordsLoc = GLES20.glGetAttribLocation(programTextureId, "a_textcoords");
        textsamplerLoc = GLES20.glGetUniformLocation(programTextureId, "u_textid");
    }

    @Override
    public void onRendererShutdown() {
        Log.i(MainActivity.TAG, "on renderer shutdown delete");
        // somehow eGL gives me an error that there is no GL context here
        // although it seems a nice place to clean up...
        GLES20.glDeleteProgram(programTextureId);
        checkGlError("glDeleteProgram");
        GLES20.glDeleteProgram(programShaderId);
        checkGlError("glDeleteProgram");
        temp[0] = offscreenTextureId;
        GLES20.glDeleteTextures(1, temp, 0);
        checkGlError("glDeleteTextures");
        temp[0] = frameBufferId;
        GLES20.glDeleteFramebuffers(1, temp, 0);
        checkGlError("glDeleteFrameBuffers");
    }

    // ------------- public utility methods ------------------------
    public void resetShader() {

        resolution[0] = this.width/shader.getResolution();
        resolution[1] = this.height/shader.getResolution();
        //Log.i(MainActivity.TAG,"resx,resy:"+resolution[0]+","+resolution[1]);

        // config texture parameters
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, offscreenTextureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, (int) resolution[0], (int) resolution[1], 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // attach texture to framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, offscreenTextureId, 0);

        // reset
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // reset fps
        startTime = SystemClock.elapsedRealtime();
        frameCount = 0;
    }

    public void onTouch( float x, float y ) {
        float xscale = x/shader.getResolution();
        float yscale = y/shader.getResolution();

        touch[0] = xscale/resolution[0];
        touch[1] = 1-yscale/resolution[1];
    }

    public void fpsRequest() {
        float fps = frameCount*1000f/(SystemClock.elapsedRealtime()-startTime);
        uiHandler.sendMessage(uiHandler.obtainMessage(FPS_RESULT, fps));
    }

    public void compileRequest() {
        CompileResult result = null;
        String fragmentShader = shader.getText();
        int program = compileShaderProgram(VERTEX_SHADER, fragmentShader);

        if (program == 0) result = new CompileResult(false,error);
        else {
            int previousProgram = programShaderId;
            programShaderId = program;
            positionLoc = GLES20.glGetAttribLocation(programShaderId, "position");
            timeLoc = GLES20.glGetUniformLocation(programShaderId, "time");
            resolutionLoc = GLES20.glGetUniformLocation(programShaderId, "resolution" );
            mouseLoc = GLES20.glGetUniformLocation(programShaderId, "mouse");
            eyeLoc = GLES20.glGetUniformLocation(programShaderId, "eye");
            GLES20.glDeleteProgram(previousProgram);
            checkGlError("deleteProgram");

            result = new CompileResult(true,"");
        }

        uiHandler.sendMessage(uiHandler.obtainMessage(COMPILE_RESULT,result));
    }

    public void thumbRequest() {
        int width = (int)resolution[0];
        int height = (int)resolution[1];
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        checkGlError("bindFrameBuffer");
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        checkGlError("readPixels");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, defaultFramebufferId);
        checkGlError("bindFrameBuffer");
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        // create new matrix for bitmap transformation
        Matrix matrix = new Matrix();
        // flip vertical
        matrix.preScale((1.0f / width) * 400f, -(1.0f / height) * 400f);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true).compress(Bitmap.CompressFormat.PNG, 100, out);

        uiHandler.sendMessage(uiHandler.obtainMessage(THUMB_RESULT,out.toByteArray()));
    }

    // ------------- private GL utility methods ------------------------

    private int compileShaderProgram(String vertexShader, String fragmentShader) {
        int[] temp = new int[1];
        int program;
        int vs = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        int fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        program = GLES20.glCreateProgram();
        if ((program != 0) && (fs != 0) && (vs != 0)) {
            GLES20.glAttachShader(program, vs);
            checkGlError("attachShader");
            GLES20.glAttachShader(program, fs);
            checkGlError("attachShader");
            GLES20.glLinkProgram(program);
            checkGlError("linkProgram");
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, temp, 0);
            checkGlError("getProgramiv");
            if (temp[0] == 0) {
                error = GLES20.glGetProgramInfoLog(program);
                GLES20.glDeleteProgram(program);
                checkGlError("deleteProgram");
                return 0;
            }
            // link success. mark shaders for deletion
            GLES20.glDeleteShader(vs);
            checkGlError("deleteShader");
            GLES20.glDeleteShader(fs);
            checkGlError("deleteShader");
            return program;
        } else {
            if (program != 0) GLES20.glDeleteProgram(program);
            checkGlError("deleteProgram");
            if (vs != 0) GLES20.glDeleteShader(vs);
            checkGlError("deleteShader");
            if (fs != 0) GLES20.glDeleteShader(fs);
            checkGlError("deleteShader");
            return 0;
        }
    }

    private int loadShader( int type, String src ) {
        int sid = GLES20.glCreateShader(type);
        checkGlError("createShaderType");

        GLES20.glShaderSource(sid, src);
        checkGlError("shaderSource");
        GLES20.glCompileShader(sid);
        checkGlError("compileShader");

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(sid, GLES20.GL_COMPILE_STATUS, compiled, 0);
        checkGlError("getShaderiv");

        if( compiled[0] == 0 ) {
            error = GLES20.glGetShaderInfoLog(sid);
            checkGlError("getShaderInfoLog");
            GLES20.glDeleteShader(sid);
            checkGlError("deleteShader");
            return 0;
        }

        return sid;
    }

    private static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(MainActivity.TAG, glOperation + ": glError " + error);
            //throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
