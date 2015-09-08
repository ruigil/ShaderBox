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

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.content.Intent;
import android.database.Cursor;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import io.oceanos.shaderbox.database.Shader;
import io.oceanos.shaderbox.database.ShaderDatabase;
import io.oceanos.shaderbox.dialog.ConfirmDeleteDialogFragment;
import io.oceanos.shaderbox.dialog.PropertiesDialogFragment;
import io.oceanos.shaderbox.dialog.ShaderDialogListener;
import io.oceanos.shaderbox.opengl.CompileResult;
import io.oceanos.shaderbox.opengl.ShaderGLView;
import io.oceanos.shaderbox.opengl.ShaderRenderer;

public class ShaderEditorActivity extends FragmentActivity implements ShaderDialogListener, Handler.Callback {
    private ShaderDatabase database;
    private ShaderEditor editor;
    private ShaderGLView shaderView;
    private ShaderRenderer renderer;
    private Shader shader;
    private TextView fps, viewError;
    private Thread tUpdateFPS, tUpdateShaderCode;
    private CompileResult result;
    private boolean compileRT;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        final View customActionBarView = inflater.inflate(R.layout.actionbar_view_save, null);
        final View shaderActionView = customActionBarView.findViewById(R.id.actionbar_view);
        final View shaderActionSave = customActionBarView.findViewById(R.id.actionbar_save);
        shaderActionView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (result.isSuccess()) onView(shader);
                        else {
                            Toast errorMsg = Toast.makeText(getBaseContext(),result.getError(),Toast.LENGTH_LONG);
                            errorMsg.getView().setBackgroundColor(getResources().getColor(R.color.editor_color_error));
                            errorMsg.show();
                        }
                    }
                });
        shaderActionSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSave(shader);
                    }
                });

        fps = (TextView)customActionBarView.findViewById(R.id.text_fps);
        viewError = (TextView)shaderActionView.findViewById(R.id.viewError);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        ShaderDatabase database = new ShaderDatabase(getBaseContext());
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                long id = database.newShader();
                Cursor cursor = database.findById(id);
                if (cursor.moveToFirst()) shader = Shader.getValues(cursor);
                cursor.close();
                if (sharedText != null) shader.setText(sharedText);
                else shader.setText("");
            }
        } else {
            long id = intent.getLongExtra("ID", 0);
            Cursor cursor = database.findById(id);
            if (cursor.moveToFirst()) shader = Shader.getValues(cursor);
            cursor.close();
        }
        database.close();

        setSymbolListener(R.id.action_tab, '\t');
        setSymbolListener(R.id.action_rpo, '(');
        setSymbolListener(R.id.action_rpc, ')');
        setSymbolListener(R.id.action_cpo, '{');
        setSymbolListener(R.id.action_cpc, '}');
        setSymbolListener(R.id.action_dotcoma, ';');
        setSymbolListener(R.id.action_coma, ',');
        setSymbolListener(R.id.action_dot, '.');
        setSymbolListener(R.id.action_plus, '+');
        setSymbolListener(R.id.action_minus, '-');
        setSymbolListener(R.id.action_times, '*');
        setSymbolListener(R.id.action_div, '/');
        setSymbolListener(R.id.action_equal, '=');
        setSymbolListener(R.id.action_spo, '[');
        setSymbolListener(R.id.action_spc, ']');


        final Handler uiHandler = new Handler(this);
        renderer = new ShaderRenderer(shader,uiHandler);

        shaderView = (ShaderGLView)findViewById(R.id.shader_view);
        shaderView.setRenderer(renderer);
        shaderView.setVRModeEnabled(false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int textSize = Integer.parseInt(prefs.getString("pref_editor_text_size", "12"));
        int opacity = Integer.parseInt(prefs.getString("pref_editor_opacity","127"));
        editor = (ShaderEditor)findViewById(R.id.editor);
        editor.setTextSize(textSize);
        editor.setBackgroundColor(opacity << 24);
        editor.setHorizontallyScrolling(true);
        editor.setHorizontalScrollBarEnabled(true);

        editor.setText(shader.getText());

        ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
        editor.setScrollView(scroll);

        compileRT = prefs.getBoolean("pref_compile_rt", true);
    }

    private void setSymbolListener(int id, final char c) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.getText().insert(editor.getSelectionStart(), String.valueOf(c));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_shader, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_shader: shaderDelete(); return true;
            case R.id.action_copy_shader: shaderCopy(); return true;
            case R.id.action_share_shader: shaderShare(); return true;
            case R.id.action_properties_shader: shaderProperties(); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        database.close();
        shaderView.onPause();
        editor.onPause();

        if (tUpdateFPS != null) tUpdateFPS.interrupt();
        if (tUpdateShaderCode != null) {
            editor.removeTextChangedListener((TextWatcher) tUpdateShaderCode);
            tUpdateShaderCode.interrupt();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        database = new ShaderDatabase(getBaseContext());
        shaderView.onResume();
        editor.onResume();

        if (shader.getPreviewMode() == 0) {
            shaderView.setRenderMode(ShaderGLView.RENDERMODE_WHEN_DIRTY);
            shaderView.requestRender();
        } else {
            shaderView.setRenderMode(ShaderGLView.RENDERMODE_CONTINUOUSLY);
            tUpdateFPS = new UpdateFPS();
            tUpdateFPS.start();
        }
        if (compileRT) {
            tUpdateShaderCode = new UpdateShaderCode();
            editor.addTextChangedListener((TextWatcher) tUpdateShaderCode);
            tUpdateShaderCode.start();
        }
    }

    private void shaderProperties() {
        PropertiesDialogFragment properties = new PropertiesDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("shader", shader);
        properties.setArguments(bundle);
        properties.show(getSupportFragmentManager(), "Properties");
    }

    private void shaderDelete() {
        Log.i(MainActivity.TAG, "delete Shader");
        ConfirmDeleteDialogFragment confirm = new ConfirmDeleteDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("shader", shader);
        confirm.setArguments(bundle);
        confirm.show(getSupportFragmentManager(), "Confirm Delete");
    }

    private void shaderCopy() {
        shader.setName("Copy of " + shader.getName());
        database.insert(shader.getContentValues());
        Toast.makeText(getBaseContext(),R.string.shader_copied,Toast.LENGTH_SHORT).show();
        finish();
    }

    private void shaderShare() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shader.getText());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Send shader text to..."));
    }

    public void onSave(Shader shader) {

        if ((shader.getPreviewMode() == 1) && (tUpdateFPS == null)) {
            tUpdateFPS = new UpdateFPS();
            tUpdateFPS.start();
        }
        if ((shader.getPreviewMode() == 0) && (tUpdateFPS != null)) {
            fps.setText("0.0 fps");
            tUpdateFPS.interrupt();
            tUpdateFPS = null;
        }

        shaderView.queueEvent(thumbRequest);
        shaderView.queueEvent(resetShader);
        // compile on save
        if (!compileRT) {
            shader.setText(editor.getText().toString());
            shaderView.queueEvent(compileRequest);
        }

    }

    public void onDelete(Shader shader) {
        database.delete(shader.getId());
        Toast.makeText(getBaseContext(),R.string.shader_deleted,Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onView(Shader shader) {
        Intent intent = new Intent(this,ShaderRenderActivity.class);
        intent.putExtra("shader", shader);
        startActivity(intent);
    }

    public void onCancel(Shader shader) {
        Log.i(MainActivity.TAG, "Dialog canceled");
    }

    @Override
    public boolean handleMessage(Message message) {

        switch (message.what) {
            case ShaderRenderer.FPS_RESULT:
                fps.setText(String.format("%.1f fps",(float)message.obj));
                return true;

            case ShaderRenderer.COMPILE_RESULT:
                result = (CompileResult) message.obj;
                if (!result.isSuccess()) {
                    viewError.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bug_shader, 0, 0, 0);
                } else {
                    viewError.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_view, 0, 0, 0);
                }
                editor.setErrorLine(result.getErrorLine());
                return true;

            case ShaderRenderer.THUMB_RESULT:
                shader.setThumb((byte[])message.obj);
                database.update(shader.getContentValues());
                // Render in preview is false
                if (shader.getPreviewMode() == 0) {
                    shaderView.setRenderMode(ShaderGLView.RENDERMODE_WHEN_DIRTY);
                    shaderView.requestRender();
                }
                // Render in preview is true
                if (shader.getPreviewMode() == 1)
                    shaderView.setRenderMode(ShaderGLView.RENDERMODE_CONTINUOUSLY);
                Toast.makeText(getBaseContext(),R.string.shader_saved,Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    private final Runnable resetShader = new Runnable() {
        @Override
        public void run() {
            renderer.resetShader();
        }
    };

    private final Runnable thumbRequest = new Runnable() {
        @Override
        public void run() {
            renderer.thumbRequest();
        }
    };

    private final Runnable compileRequest = new Runnable() {
        @Override
        public void run() {
            renderer.compileRequest();
        }
    };

    private final Runnable fpsRequest = new Runnable() {
        @Override
        public void run() {
            renderer.fpsRequest();
        }
    };

    private class UpdateFPS extends Thread {
        @Override
        public void run() {
            final int DELAY = 1000; // 1 second
            // request an fps value every second
            while (true) {
                shaderView.queueEvent(fpsRequest);
                try { Thread.sleep(DELAY); } catch (InterruptedException e) {return;}
            }
        }
    }

    private class UpdateShaderCode extends Thread implements TextWatcher {
        private boolean textChanged = false;
        private long changeTimestamp = 0;

        @Override
        public void run() {
            final int DELAY = 200; // 200ms
            final int TEXT_DELAY = 1000; // 1 seconds
            while (true) {
                long timelapse = SystemClock.elapsedRealtime() - changeTimestamp;
                // request a compilation of the shader 1 second after the last text changed
                if (textChanged && (timelapse > TEXT_DELAY)) {
                    shader.setText(editor.getText().toString());
                    if (compileRT) shaderView.queueEvent(compileRequest);
                    textChanged = false;
                }
                // sleep
                try { Thread.sleep(DELAY); } catch (InterruptedException e) {return;}
            }
        }

        // text listener interface
        public void afterTextChanged(Editable editable) {
            changeTimestamp = SystemClock.elapsedRealtime();
            textChanged = true;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

    }

}
