package iut.info3.betterstravadroid.parcours;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import iut.info3.betterstravadroid.R;

public class ParcoursAdaptateur extends RecyclerView.Adapter<ParcoursViewHolder> {


    private List<ParcoursItem> lesParcours;

    public ParcoursAdaptateur(List<ParcoursItem> donnees) {
        lesParcours = donnees;
    }

    @NonNull
    @Override
    public ParcoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parcour_item,
                                        parent,false);
        return new ParcoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcoursViewHolder holder, int position) {
        ParcoursItem myObject = lesParcours.get(position);
        holder.bind(myObject);
    }

    @Override
    public int getItemCount() {
        return lesParcours.size();
    }


}
