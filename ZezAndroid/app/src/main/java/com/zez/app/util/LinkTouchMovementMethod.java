package com.zez.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Custom MovementMethod for chat bubbles that allows clicking URLs
 * to redirect directly to the browser, while letting non-link touch
 * events pass through to the parent view so that swipe-to-reply and
 * long-press selection work without interference.
 */
public class LinkTouchMovementMethod extends LinkMovementMethod {

    private static LinkTouchMovementMethod sInstance;

    public static LinkTouchMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new LinkTouchMovementMethod();
        }
        return sInstance;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            if (layout != null) {
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        try {
                            if (link[0] instanceof URLSpan) {
                                String url = ((URLSpan) link[0]).getURL();
                                if (url != null && !url.isEmpty()) {
                                    if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("mailto:") && !url.startsWith("tel:")) {
                                        url = "https://" + url;
                                    }
                                    Context ctx = widget.getContext();
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ctx.startActivity(intent);
                                    return true;
                                }
                            }
                            link[0].onClick(widget);
                        } catch (Exception ignored) {}
                    } else {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }
        }

        return false;
    }
}
