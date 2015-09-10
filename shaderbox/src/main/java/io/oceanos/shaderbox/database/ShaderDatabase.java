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

import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.oceanos.shaderbox.MainActivity;
import io.oceanos.shaderbox.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShaderDatabase extends SQLiteOpenHelper {

	public static final String TABLE_SHADERS = "shaders";
	public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
	public static final String COLUMN_SHADER = "shader"; // text
    public static final String COLUMN_VRMODE = "vrmode"; // 0,1
	public static final String COLUMN_PREVIEWMODE = "previewmode"; // 0,1
	public static final String COLUMN_RESOLUTION = "resolution"; //1,2,4 res factor
	public static final String COLUMN_THUMB = "thumb";
	public static final String COLUMN_CREATED = "created";
	public static final String COLUMN_MODIFIED = "modified";

	private static final String DATABASE_NAME = "shaderbox.db";
	private static final int DATABASE_VERSION = 23;
	private Context context = null;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_SHADERS  	+ "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME    	+ " text not null,"
			+ COLUMN_SHADER  	+ " text not null,"
            + COLUMN_VRMODE 	+ " integer,"
			+ COLUMN_PREVIEWMODE + " integer,"
			+ COLUMN_RESOLUTION	+ " integer,"
			+ COLUMN_THUMB   	+ " blob,"
			+ COLUMN_CREATED 	+ " datetime,"
			+ COLUMN_MODIFIED	+ " datetime);";

    private static final String DATABASE_INSERT = "insert into "
            + TABLE_SHADERS + "("
            + COLUMN_NAME +","
            + COLUMN_SHADER +","
            + COLUMN_VRMODE +","
			+ COLUMN_PREVIEWMODE +","
			+ COLUMN_RESOLUTION +","
            + COLUMN_THUMB +","
            + COLUMN_CREATED +","
            + COLUMN_MODIFIED +")"
            + " values(?,?,?,?,?,?,?,?);";

	private static final String DATABASE_QUERY = "select "
			+ COLUMN_ID +","
			+ COLUMN_NAME +","
			+ COLUMN_SHADER +","
			+ COLUMN_VRMODE +","
			+ COLUMN_PREVIEWMODE +","
			+ COLUMN_RESOLUTION +","
			+ COLUMN_THUMB +","
			+ COLUMN_CREATED +","
			+ COLUMN_MODIFIED +" from "
			+ TABLE_SHADERS;

	public ShaderDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
        String time = currentTime();
        database.execSQL(DATABASE_INSERT, new Object[]{
				"New Shader",
				Shader.loadStringResource(context,R.raw.new_shader),
				0,
				1,
				1,
				Shader.loadByteResource(context,R.drawable.new_shader),
				time,
				time
		});
        database.execSQL(DATABASE_INSERT, new Object[]{
                "Color Wave",
                Shader.loadStringResource(context,R.raw.color_wave),
                0,
                1,
                1,
                Shader.loadByteResource(context,R.drawable.new_shader),
                time,
                time
        });
        database.execSQL(DATABASE_INSERT, new Object[]{
                "Seascape",
				Shader.loadStringResource(context,R.raw.seascape),
                0,
				1,
				4,
				Shader.loadByteResource(context,R.drawable.new_shader),
                time,
                time
        });
		database.execSQL(DATABASE_INSERT, new Object[]{
				"ShaderBox",
				Shader.loadStringResource(context,R.raw.shader_box),
				0,
				1,
				1,
				Shader.loadByteResource(context,R.drawable.new_shader),
				time,
				time
		});
		database.execSQL(DATABASE_INSERT, new Object[]{
				"Fractal 3D",
				Shader.loadStringResource(context,R.raw.fractal3d),
				0,
				1,
				2,
				Shader.loadByteResource(context,R.drawable.new_shader),
				time,
				time
		});
		database.execSQL(DATABASE_INSERT, new Object[]{
				"Frozen Wasteland",
				Shader.loadStringResource(context,R.raw.frozen),
				0,
				1,
				4,
				Shader.loadByteResource(context,R.drawable.new_shader),
				time,
				time
		});

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(MainActivity.TAG,
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SHADERS);
		onCreate(database);
	}

	public Cursor findAll() {
		return getReadableDatabase().rawQuery(
				DATABASE_QUERY + " order by " + COLUMN_MODIFIED + " desc", null);
	}

	public Cursor findById( long id ) {
        return getReadableDatabase().rawQuery(
					DATABASE_QUERY + " where " + COLUMN_ID + " = " + id + ";", null);
	}

	public long insert(ContentValues cv) {
		String now = currentTime();
		cv.put( COLUMN_MODIFIED, now );
		cv.put(COLUMN_CREATED, now );
		cv.remove(COLUMN_ID);
        return getWritableDatabase().insert(TABLE_SHADERS, null, cv);
	}

	public void update(ContentValues cv) {
		cv.put( COLUMN_MODIFIED, currentTime());
		long id = cv.getAsLong(COLUMN_ID);
        getWritableDatabase().update(TABLE_SHADERS, cv, COLUMN_ID + "=" + id, null);
	}

	public void delete(long id) {
        getWritableDatabase().delete(TABLE_SHADERS, COLUMN_ID + "=" + id, null);
	}

    public long newShader() {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_NAME, "New Shader" );
		cv.put(COLUMN_SHADER, Shader.loadStringResource(context, R.raw.new_shader));
		cv.put(COLUMN_VRMODE, 0 );
		cv.put(COLUMN_PREVIEWMODE, 1 );
		cv.put(COLUMN_RESOLUTION, 1);
		cv.put(COLUMN_THUMB, Shader.loadByteResource(context, R.drawable.new_shader));

        return insert(cv);
    }

	private static String currentTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
	}



}
