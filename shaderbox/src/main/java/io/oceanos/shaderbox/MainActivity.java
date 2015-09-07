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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import io.oceanos.shaderbox.dialog.AboutDialogFragment;

public class MainActivity extends FragmentActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = "ShaderBox";
    private ShaderItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ShaderItemAdapter(getBaseContext());
        adapter.fetchData();

        GridView mGridView = (GridView) findViewById(R.id.list);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.fetchData();
    }

    @Override
    public void onItemClick(AdapterView<?> container, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this, ShaderEditorActivity.class);
        intent.putExtra("ID", id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_shader:
                adapter.newShader();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                AboutDialogFragment about = new AboutDialogFragment();
                about.show(getSupportFragmentManager(), "ShaderBox");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

 }
