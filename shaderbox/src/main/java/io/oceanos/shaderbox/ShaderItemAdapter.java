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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import io.oceanos.shaderbox.database.Shader;
import io.oceanos.shaderbox.database.ShaderDatabase;

import java.util.ArrayList;
import java.util.List;

public class ShaderItemAdapter extends BaseAdapter {
    private Context context;
    List<Shader> shaders = new ArrayList<Shader>();

    public ShaderItemAdapter(Context context) {
        this.context = context;
    }

    public void fetchData() {
        shaders.clear();
        ShaderDatabase database = new ShaderDatabase(context);
        Cursor cursor = database.findAll();
        while(cursor.moveToNext()) {
            shaders.add(Shader.getValues(cursor));
        }
        cursor.close();
        database.close();
        notifyDataSetChanged();
    }

    public void newShader() {
        ShaderDatabase database = new ShaderDatabase(context);
        long id = database.newShader();
        Cursor cursor = database.findById(id);
        if (cursor.moveToFirst()) shaders.add(0, Shader.getValues(cursor));
        cursor.close();
        database.close();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return shaders.size();
    }

    @Override
    public Object getItem(int position) {
        return shaders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return shaders.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = LayoutInflater.from(container.getContext()).inflate(R.layout.shader_item, container, false);
        }
        Shader shader = shaders.get(position);
        byte[] data = shader.getThumb();
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        ImageView img = (ImageView) convertView.findViewById(R.id.thumbnail);
        img.setImageBitmap(bm);
        ((TextView) convertView.findViewById(R.id.title)).setText(shader.getName());
        return convertView;
    }
}
