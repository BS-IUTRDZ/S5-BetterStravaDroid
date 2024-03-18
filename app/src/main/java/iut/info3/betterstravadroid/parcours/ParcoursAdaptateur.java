package iut.info3.betterstravadroid.parcours;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.RecyclerViewInterface;

public class ParcoursAdaptateur extends RecyclerView.Adapter<ParcoursViewHolder> {



    private OnBottomReachedListener onBottomReachedListener;
    private final RecyclerViewInterface recyclerViewInterface;

    private boolean bottomAlreadyReached;

    private List<ParcoursItem> lesParcours;

    public ParcoursAdaptateur(List<ParcoursItem> donnees, RecyclerViewInterface recyclerViewInterface) {
        lesParcours = donnees;
        this.recyclerViewInterface = recyclerViewInterface;
        bottomAlreadyReached = false;
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

        holder.cardView.setOnClickListener(v -> recyclerViewInterface.onItemClick(lesParcours.get(position)));

        holder.bind(myObject);

        if (!bottomAlreadyReached && position == lesParcours.size() - 1) {
            if (onBottomReachedListener != null) {
                bottomAlreadyReached = true;
                onBottomReachedListener.onBottomReached(position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return lesParcours.size();
    }

    public interface OnBottomReachedListener {

        void onBottomReached(int position);

    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public void resetBottomReached() {
        bottomAlreadyReached = false; // Réactiver l'événement onBottomReached
    }




}
