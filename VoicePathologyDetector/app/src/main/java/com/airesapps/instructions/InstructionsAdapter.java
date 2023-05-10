package com.airesapps.instructions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class InstructionsAdapter extends ArrayAdapter<Instruction> {
    private Context context;
    private List<Instruction> instructions;

    public InstructionsAdapter(Context context, List<Instruction> instructions) {
        super(context, 0, instructions);
        this.context = context;
        this.instructions = instructions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Instruction instruction = instructions.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tvInstruction = convertView.findViewById(android.R.id.text1);
        tvInstruction.setText(instruction.getText());

        return convertView;
    }
}
