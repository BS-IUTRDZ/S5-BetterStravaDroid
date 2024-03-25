package iut.info3.betterstravadroid.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import iut.info3.betterstravadroid.R;
/**
 * An abstract class to provide swipe functionality for RecyclerView items.
 */
public abstract class SwipeHelper extends ItemTouchHelper.SimpleCallback {

    public static final int BUTTON_WIDTH = 500;
    private RecyclerView recyclerView;
    private List<UnderlayButton> buttons;
    private GestureDetector gestureDetector;
    private int swipedPos = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<UnderlayButton>> buttonsBuffer;
    private Queue<Integer> recoverQueue;
    /**
     * Listener for detecting gestures on the RecyclerView item.
     */
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        /**
         * Called when a single tap is confirmed on the view.
         *
         * @param e The motion event for the tap.
         * @return True if the tap is consumed, false otherwise.
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (UnderlayButton button : buttons) {
                if (button.onClick(e.getX(), e.getY()))
                    break;
            }

            return true;
        }
    };
    /**
     * Touch listener for handling touch events on the RecyclerView item.
     */
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        /**
         * Called when a touch event is dispatched to the view.
         *
         * @param view The view that received the touch event.
         * @param e    The motion event.
         * @return True if the touch event is consumed, false otherwise.
         */
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            if (swipedPos < 0) return false;
            Point point = new Point((int) e.getRawX(), (int) e.getRawY());

            try {
                RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
                View swipedItem = swipedViewHolder.itemView;
                Rect rect = new Rect();
                swipedItem.getGlobalVisibleRect(rect);

                if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP
                        || e.getAction() == MotionEvent.ACTION_MOVE) {
                    if (rect.top < point.y && rect.bottom > point.y)
                        gestureDetector.onTouchEvent(e);
                    else {
                        recoverQueue.add(swipedPos);
                        swipedPos = -1;
                        recoverSwipedItem();
                    }
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    };

    /**
     * Constructor for SwipeHelper.
     * @param context The context.
     * @param recyclerView The RecyclerView associated with this SwipeHelper.
     */
    public SwipeHelper(Context context, RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
        this.buttons = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);
        this.recyclerView.setOnTouchListener(onTouchListener);
        buttonsBuffer = new HashMap<>();
        recoverQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };

        attachSwipe();
    }


    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }


    /**
     * Overrides the onSwiped method of ItemTouchHelper.Callback to handle RecyclerView item swiping.
     *
     * @param viewHolder The ViewHolder of the swiped item.
     * @param direction  The direction of the swipe action.
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();

        if (swipedPos != pos)
            recoverQueue.add(swipedPos);

        swipedPos = pos;

        if (buttonsBuffer.containsKey(swipedPos))
            buttons = buttonsBuffer.get(swipedPos);
        else
            buttons.clear();

        buttonsBuffer.clear();
        swipeThreshold = 0.5f * buttons.size() * BUTTON_WIDTH;
        recoverSwipedItem();
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }


    /**
     * Overrides the onChildDraw method of ItemTouchHelper.Callback to customize the drawing of RecyclerView item while swiping.
     *
     * @param c                 The canvas on which to draw the item.
     * @param recyclerView      The RecyclerView associated with this swipe action.
     * @param viewHolder        The ViewHolder of the swiped item.
     * @param dX                The horizontal displacement caused by the swipe action.
     * @param dY                The vertical displacement caused by the swipe action.
     * @param actionState       The current action state of the swipe action.
     * @param isCurrentlyActive True if the swiped item is currently active, false otherwise.
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if (pos < 0) {
            swipedPos = pos;
            return;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                List<UnderlayButton> buffer = new ArrayList<>();

                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateUnderlayButton(viewHolder, buffer);
                    buttonsBuffer.put(pos, buffer);
                } else {
                    buffer = buttonsBuffer.get(pos);
                }

                translationX = dX * buffer.size() * BUTTON_WIDTH / itemView.getWidth();
                drawButtons(c, itemView, buffer, pos, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }
    /**
     * Recovers the swiped RecyclerView item by notifying the adapter of item changes.
     * This method is synchronized to ensure thread safety.
     */
    private synchronized void recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            int pos = recoverQueue.poll();
            if (pos > -1) {
                recyclerView.getAdapter().notifyItemChanged(pos);
            }
        }
    }

    /**
     * Draws the underlay buttons on the canvas when a RecyclerView item is swiped.
     *
     * @param c         The canvas on which to draw the buttons.
     * @param itemView  The view representing the RecyclerView item.
     * @param buffer    The list of underlay buttons to be drawn.
     * @param pos       The position of the RecyclerView item.
     * @param dX        The horizontal displacement caused by the swipe action.
     */
    private void drawButtons(Canvas c, View itemView, List<UnderlayButton> buffer, int pos, float dX) {
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX / buffer.size();
        final int supLength = 50;

        for (UnderlayButton button : buffer) {
            float left = right - dButtonWidth;
            button.onDraw(
                    c,
                    new RectF(
                            left + supLength,
                            itemView.getTop() + supLength,
                            right + supLength,
                            itemView.getBottom() + supLength
                    ),
                    pos
            );

            right = left;
        }
    }
    /**
     * Attaches swipe functionality to the RecyclerView.
     */
    public void attachSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Abstract method to instantiate underlay buttons for a particular RecyclerView item.
     * @param viewHolder The ViewHolder for the RecyclerView item.
     * @param underlayButtons List to store the underlay buttons.
     */
    public abstract void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons);
    /**
     * Class representing an underlay button for swiping.
     */
    public static class UnderlayButton {
        private int imageResId;
        private int color;
        private int pos;
        private RectF clickRegion;
        private UnderlayButtonClickListener clickListener;

        private Context context;

        public UnderlayButton(Context context, int imageResId, int color, UnderlayButtonClickListener clickListener) {
            this.imageResId = imageResId;
            this.color = color;
            this.clickListener = clickListener;
            this.context = context;
        }

        /**
         * Checks if the button is clicked.
         * @param x The x-coordinate of the click.
         * @param y The y-coordinate of the click.
         * @return True if the click is within the button's region, false otherwise.
         */
        public boolean onClick(float x, float y) {
            if (clickRegion != null && clickRegion.contains(x, y)) {
                clickListener.onClick(pos);
                return true;
            }

            return false;
        }

        /**
         * Draws the underlay button on the canvas.
         * @param c The canvas to draw on.
         * @param rect The bounding rectangle of the button.
         * @param pos The position of the button.
         */
        public void onDraw(Canvas c, RectF rect, int pos) {
            Paint p = new Paint();

            // Draw background
            p.setColor(color);
            c.drawRect(rect, p);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.trash);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 7, bitmap.getHeight() / 7, true);
            resizedBitmap = changeColor(resizedBitmap, Color.BLACK, Color.WHITE);
            // Draw Text
            p.setColor(Color.WHITE);
            p.setTextSize(100);

            float left = rect.left + (rect.width() - resizedBitmap.getWidth()) / 2f;
            float top = rect.top + (rect.height() - resizedBitmap.getHeight()) / 2f;
            c.drawBitmap(resizedBitmap, left, top, p);

            clickRegion = rect;
            this.pos = pos;
            bitmap.recycle();
            resizedBitmap.recycle();
        }
    }

    /**
     * Listener triggered when a underlay button is clicked.
     */
    public interface UnderlayButtonClickListener {
        /**
         * Called when an underlay button is clicked.
         * @param pos The position of the button.
         */
        void onClick(int pos);
    }

    /**
     * Switch one color of a bitmap to another color.
     * @param bitmap bitmap that has color to change.
     * @param oldColor color to replace
     * @param newColor new color that replace the oldColor
     * @return the bitmap with the switched color
     */
    public static Bitmap changeColor(Bitmap bitmap, int oldColor,
                                     int newColor) {
        if (bitmap == null) {
            return bitmap;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int x = 0; x < pixels.length; ++x) {
            pixels[x] = (pixels[x] == oldColor) ? newColor : pixels[x];
        }
        Bitmap newBitmap = Bitmap.createBitmap(width, height,
                bitmap.getConfig());
        newBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBitmap;
    }
}