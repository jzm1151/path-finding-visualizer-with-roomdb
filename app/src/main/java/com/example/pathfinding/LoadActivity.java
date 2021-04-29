package com.example.pathfinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pathfinding.db.Graph;
import com.example.pathfinding.db.GraphViewModel;

import java.util.List;

public class LoadActivity extends AppCompatActivity implements LoadGraphDialog.LoadGraphInterface {
    private GraphViewModel graphViewModel;
    public static final String loadGraphResult = "LOAD_GRAPH_ACTIVITY_RESULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        Log.d("testing", "onCreate");

        RecyclerView recyclerView = findViewById(R.id.listGraphs);
        GraphListAdapter adapter = new GraphListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        graphViewModel = new ViewModelProvider(this).get(GraphViewModel.class);
        graphViewModel.getAll().observe(this, adapter::setGraphs);
    }

    private void displayPopUp(Graph graph) {
        LoadGraphDialog loadGraphDialog = new LoadGraphDialog();
        loadGraphDialog.setGraphDetails(graph.graph, graph.graphName);
        loadGraphDialog.show(getSupportFragmentManager(), "LoadGraphDialog");
    }

    @Override
    public void sendResult(String graph) {
        Intent result = new Intent();
        result.putExtra(loadGraphResult, graph);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    public class GraphListAdapter extends RecyclerView.Adapter<GraphListAdapter.GraphViewHolder> {

        class GraphViewHolder extends RecyclerView.ViewHolder {
            private Graph graph;
            private TextView graphName;

            // Note that this view holder will be used for different items -
            // The callbacks though will use the currently stored item
            private GraphViewHolder(View itemView) {
                super(itemView);
                graphName = itemView.findViewById(R.id.textViewGraphName);
                graphName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (graph != null)
                            displayPopUp(graph);
                    }
                });
                Log.d("here", "testing graph viewholder");
            }
        }

        private final LayoutInflater layoutInflater;
        private List<Graph> graphs; // Cached copy of jokes

        GraphListAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public GraphViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.list_item, parent, false);
            Log.d("here", "testing onCreateViewHolder");
            return new GraphViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(GraphViewHolder holder, int position) {
            if (graphs != null && position < graphs.size()) {
                Graph curr = graphs.get(position);
                holder.graph = curr;
                holder.graphName.setText(curr.graphName);
            }

            Log.d("testing", "onBindViewHolder");
        }

        void setGraphs(List<Graph> graphs){
            Log.d("testing", "testing set graphs" + graphs.size());

            this.graphs = graphs;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            if (graphs != null)
                return graphs.size();
            else return 0;
        }
    }
}