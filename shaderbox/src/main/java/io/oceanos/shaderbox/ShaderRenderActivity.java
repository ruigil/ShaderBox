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

package io.oceanos.shaderbox;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import io.oceanos.shaderbox.database.Shader;
import io.oceanos.shaderbox.opengl.ShaderGLView;
import io.oceanos.shaderbox.opengl.ShaderRenderer;

public class ShaderRenderActivity extends CardboardActivity {
    private ShaderGLView shaderView;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_render);
        shaderView = (ShaderGLView) findViewById(R.id.render_view);
        setCardboardView(shaderView);

        Shader shader = (Shader)getIntent().getSerializableExtra("shader");
        ShaderRenderer renderer = new ShaderRenderer(shader,new Handler());
        shaderView.setRenderer(renderer);
        shaderView.setVRModeEnabled(shader.getVrMode() == 1);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        shaderView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        shaderView.onResume();
    }

    @Override
    public void onCardboardTrigger() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        shaderView.onTouchEvent(
                MotionEvent.obtain(0,0,
                        MotionEvent.ACTION_DOWN,
                        size.x,size.y,
                        MotionEvent.AXIS_LTRIGGER));
        vibrator.vibrate(50);
    }


}
