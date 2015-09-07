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

package io.oceanos.shaderbox.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import io.oceanos.shaderbox.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;

public class Shader implements Serializable {
    private long id;
    private String name;
    private String text;
    private int vrmode;
    private int previewMode;
    private int resolution;
    private byte[] thumb;
    private String modified;


    public static Shader getValues(Cursor cursor) {
        Shader shader = new Shader();
        shader.setId(cursor.getLong(cursor.getColumnIndex(ShaderDatabase.COLUMN_ID)));
        shader.setName(cursor.getString(cursor.getColumnIndex(ShaderDatabase.COLUMN_NAME)));
        shader.setVrMode(cursor.getInt(cursor.getColumnIndex(ShaderDatabase.COLUMN_VRMODE)));
        shader.setPreviewMode(cursor.getInt(cursor.getColumnIndex(ShaderDatabase.COLUMN_PREVIEWMODE)));
        shader.setResolution(cursor.getInt(cursor.getColumnIndex(ShaderDatabase.COLUMN_RESOLUTION)));
        shader.setThumb(cursor.getBlob(cursor.getColumnIndex(ShaderDatabase.COLUMN_THUMB)));
        shader.setText(cursor.getString(cursor.getColumnIndex(ShaderDatabase.COLUMN_SHADER)));
        shader.setModified(cursor.getString(cursor.getColumnIndex(ShaderDatabase.COLUMN_MODIFIED)));
        return shader;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ShaderDatabase.COLUMN_ID, id );
        cv.put(ShaderDatabase.COLUMN_NAME, name );
        cv.put(ShaderDatabase.COLUMN_SHADER, text );
        cv.put(ShaderDatabase.COLUMN_VRMODE, vrmode);
        cv.put(ShaderDatabase.COLUMN_PREVIEWMODE, previewMode);
        cv.put(ShaderDatabase.COLUMN_RESOLUTION, resolution );
        cv.put(ShaderDatabase.COLUMN_THUMB, thumb);
        cv.put(ShaderDatabase.COLUMN_MODIFIED, modified);
        return cv;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getVrMode() {
        return vrmode;
    }

    public void setVrMode(int vrmode) {
        this.vrmode = vrmode;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public byte[] getThumb() {
        return thumb;
    }

    public void setThumb(byte[] thumb) {
        this.thumb = thumb;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public int getPreviewMode() {
        return previewMode;
    }

    public void setPreviewMode(int showfps) {
        this.previewMode = showfps;
    }

    public static String loadStringResource(Context context, int id ) {
        InputStream in = context.getResources().openRawResource(id);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];

        try {
            for( int r; (r = in.read( buf )) != -1; ) out.write( buf, 0, r );
        } catch (Exception e) {
            Log.e(MainActivity.TAG, e.toString());
        }

        return out.toString();
    }

    public static byte[] loadByteResource(Context context, int id ) {
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), id);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compress( Bitmap.CompressFormat.PNG, 100, out );
        return out.toByteArray();
    }

}
