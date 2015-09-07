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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.google.vrtoolkit.cardboard.CardboardView;

public class ShaderGLView extends CardboardView {
    ShaderRenderer renderer;

    public ShaderGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRenderer(ShaderRenderer renderer) {
        super.setRenderer(renderer);
        this.renderer = renderer;
    }

    @Override
    public boolean onTouchEvent( MotionEvent e ) {
        final float x = e.getX();
        final float y = e.getY();

        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.onTouch(x,y);
            }
        });
        return true;
    }



}
