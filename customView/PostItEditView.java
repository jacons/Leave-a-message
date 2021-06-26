package project.leaveamessage.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import project.leaveamessage.R;

public class PostItEditView extends androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView {

    public PostItEditView(Context context) {
        super(context);
    }
    public PostItEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public PostItEditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable d = getResources().getDrawable(R.drawable.vectorpaint, null);
        d.setBounds(0, 0, getWidth(),getHeight());
        d.draw(canvas);

    }
}
