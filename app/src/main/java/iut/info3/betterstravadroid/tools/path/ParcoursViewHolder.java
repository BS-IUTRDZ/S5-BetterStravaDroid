package iut.info3.betterstravadroid.tools.path;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import iut.info3.betterstravadroid.R;

public class ParcoursViewHolder extends RecyclerView.ViewHolder {

    TextView dateTextView;
    TextView titreTextView;
    TextView descriptionTextView;
    public CardView cardView;

    public ParcoursViewHolder(@NonNull View itemView) {
        super(itemView);
        dateTextView = itemView.findViewById(R.id.parcour_date);
        titreTextView = itemView.findViewById(R.id.tv_titre);
        descriptionTextView = itemView.findViewById(R.id.tv_description);
        cardView = itemView.findViewById(R.id.item_container);
    }

    public void bind(ParcoursItem parcoursItem) { 
        dateTextView.setText(parcoursItem.getDate());
        titreTextView.setText(parcoursItem.getTitre());
        descriptionTextView.setText(parcoursItem.getDescription());
    }
}
