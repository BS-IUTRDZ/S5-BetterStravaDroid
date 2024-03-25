package iut.info3.betterstravadroid.tools;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
/**
 * A RecyclerView ItemDecoration class to add vertical spacing between items.
 */
public class SpacingItemDecorator extends RecyclerView.ItemDecoration {


    private final int verticalSpaceHeight;
    /**
     * Constructor for SpacingItemDecorator.
     *
     * @param verticalSpaceHeight The height of the vertical spacing to be added between items.
     */
    public SpacingItemDecorator(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    /**
     * Adds vertical spacing to the bottom of each item in the RecyclerView.
     *
     * @param outRect The outer rectangle of the item view.
     * @param view    The item view.
     * @param parent  The RecyclerView.
     * @param state   The current state of RecyclerView.
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.bottom = verticalSpaceHeight;
    }
}
