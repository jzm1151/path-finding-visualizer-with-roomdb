package com.example.pathfinding;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class LoadGraphDialog extends DialogFragment {
    private String graphName;
    private String graph;
    private static final String graphNameKey = "GRAPH_NAME_KEY";
    private static final String graphKey = "GRAPH_KEY";
    private LoadGraphInterface listener;

    public interface LoadGraphInterface {
        void sendResult(String graph);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            graphName = savedInstanceState.getString(graphNameKey);
            graph = savedInstanceState.getString(graphKey);
        }

        Resources resources = getResources();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(resources.getText(R.string.loading_graph)+" "+ graphName)
                .setPositiveButton(resources.getText(R.string.confirm), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.sendResult(graph);
                    }
                })
                .setNegativeButton(resources.getText(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return dialog.create();
    }

    public void setGraphDetails(String graph, String graphName) {
        this.graph = graph;
        this.graphName = graphName;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(graphNameKey, graphName);
        outState.putString(graphKey, graph);
        super.onSaveInstanceState(outState);
    }

    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (LoadGraphInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Must implement LoadGraphInterface");
        }
    }
}
