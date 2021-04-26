package com.example.pathfinding;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class SaveGraphDialog extends DialogFragment {
    private SaveGraphInterface listener;
    private static String graphNameKey = "GRAPH_NAME";
    private EditText nameSavedGraph;

    public interface SaveGraphInterface {
        void saveGraph(String graphName);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        nameSavedGraph = new EditText(getActivity());
        nameSavedGraph.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        if (savedInstanceState != null) nameSavedGraph.setText(savedInstanceState.getString(graphNameKey));

        Resources resources = getResources();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(resources.getText(R.string.save_graph))
        .setPositiveButton(resources.getText(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.saveGraph(nameSavedGraph.getText().toString());
                    }
                })
                .setNegativeButton(resources.getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setView(nameSavedGraph);


        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (SaveGraphInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Must implement SaveGraphInterface");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(graphNameKey, nameSavedGraph.getText().toString());
    }
}
