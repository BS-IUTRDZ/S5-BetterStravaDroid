package iut.info3.betterstravadroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.interfaces.RecyclerViewInterface;
import iut.info3.betterstravadroid.tools.path.PathItem;
import iut.info3.betterstravadroid.tools.path.PathViewHolder;
/**
 * Adapter class for the RecyclerView displaying paths.
 */
public class PathAdapter extends RecyclerView.Adapter<PathViewHolder> {

    private OnBottomReachedListener onBottomReachedListener;
    private final RecyclerViewInterface recyclerViewInterface;

    private boolean bottomAlreadyReached;

    private List<PathItem> lesParcours;
    /**
     * Constructor for the PathAdapter.
     *
     * @param donnees              The list of path items.
     * @param recyclerViewInterface The interface for handling RecyclerView item clicks.
     */
    public PathAdapter(List<PathItem> donnees, RecyclerViewInterface recyclerViewInterface) {
        lesParcours = donnees;
        this.recyclerViewInterface = recyclerViewInterface;
        bottomAlreadyReached = false;
    }

    @NonNull
    @Override
    public PathViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_path_item,
                                        parent,false);
        return new PathViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PathViewHolder holder, int position) {
        PathItem myObject = lesParcours.get(position);

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
    /**
     * Interface for listening to the bottom reached event.
     */
    public interface OnBottomReachedListener {
        /**
         * Called when the bottom of the RecyclerView is reached.
         *
         * @param position The position of the last visible item.
         */
        void onBottomReached(int position);

    }
    /**
     * Sets the listener for bottom reached event.
     *
     * @param onBottomReachedListener The listener for bottom reached event.
     */
    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }
    /**
     * Resets the bottom reached flag.
     * Call this method to re-enable the onBottomReached event.
     */
    public void resetBottomReached() {
        bottomAlreadyReached = false; // Réactiver l'événement onBottomReached
    }




}
