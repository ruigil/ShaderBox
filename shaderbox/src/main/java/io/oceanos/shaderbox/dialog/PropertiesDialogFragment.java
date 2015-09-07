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


package io.oceanos.shaderbox.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import io.oceanos.shaderbox.R;
import io.oceanos.shaderbox.database.Shader;

public class PropertiesDialogFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final Shader shader = (Shader) getArguments().getSerializable("shader");

        View view = inflater.inflate(R.layout.dialog_properties, null);
        final EditText nameView = (EditText)view.findViewById(R.id.name);
        Switch vrMode = (Switch) view.findViewById(R.id.vr_mode);
        Switch preview = (Switch) view.findViewById(R.id.preview);
        RadioGroup resolutionRadio = (RadioGroup)view.findViewById(R.id.resolution);

        nameView.setText(shader.getName());
        int resid = shader.getResolution() == 1 ? R.id.high_res :
                    shader.getResolution() == 2 ? R.id.medium_res : R.id.low_res;
        resolutionRadio.check(resid);

        vrMode.setChecked(shader.getVrMode() == 1);
        vrMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                shader.setVrMode(isChecked ? 1 : 0);
            }
        });

        preview.setChecked(shader.getPreviewMode() == 1);
        preview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                shader.setPreviewMode(isChecked ? 1 : 0);
            }
        });

        resolutionRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i) {
                    case R.id.high_res: shader.setResolution(1); break;
                    case R.id.medium_res: shader.setResolution(2); break;
                    case R.id.low_res: shader.setResolution(4); break;
                }
            }
        });

        builder.setTitle("Properties")
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        shader.setName(nameView.getText().toString());
                        mListener.onSave(shader);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCancel(shader);
                    }
                });

        return builder.create();
    }

    ShaderDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ShaderDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ShaderDialogListener");
        }
    }

}