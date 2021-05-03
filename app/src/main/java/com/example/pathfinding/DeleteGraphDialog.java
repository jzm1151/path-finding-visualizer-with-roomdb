package com.example.pathfinding;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class DeleteGraphDialog extends DialogFragment {
    private String graphName;
    private int graphID;
    private static final String graphNameKey = "GRAPH_NAME_KEY";
    private static final String graphIDKey = "GRAPH_ID_KEY";
    private DeleteGraphInterface listener;

    public interface DeleteGraphInterface {
        void confirmDelete(int graphID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            graphID = savedInstanceState.getInt(graphIDKey);
            graphName = savedInstanceState.getString(graphNameKey);
        }

        Resources resources = getResources();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(resources.getText(R.string.deleting_graph)+" "+ graphName)
                .setPositiveButton(resources.getText(R.string.confirm), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.confirmDelete(graphID);
                    }
                })
                .setNegativeButton(resources.getText(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return dialog.create();
    }

    public void setGraphDetails(int graphID, String graphName) {
        this.graphID = graphID;
        this.graphName = graphName;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(graphNameKey, graphName);
        outState.putInt(graphIDKey, graphID);
        super.onSaveInstanceState(outState);
    }

    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DeleteGraphInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Must implement DeleteGraphInterface");
        }
    }
}
