package me.giskard.dust.mod.android.dev;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.mod.android.gui.DustAndroidGuiConsts;

public class DustAndroidDevGuiUtils implements DustAndroidGuiConsts {
    public static void fillTable(TableLayout tl, int rowCount, String... cols) {
        TableRow tr;
        TextView cell;

        Context ctx = tl.getContext();

        tl.setBackgroundColor(Color.BLACK);

        if (0 < rowCount) {
            tr = new TableRow(ctx);

            cell = addCell(tr, "#");
            cell.setMinimumWidth(100);
            cell.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);

            for (String c : cols) {
                TextView v = addCell(tr, c);
                v.setTextColor(Color.WHITE);
                v.setBackgroundColor(Color.BLUE);
                v.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            }
            tl.addView(tr);

            for (int r = 0; r < rowCount; ++r) {
                tr = new TableRow(ctx);
                TextView v = addCell(tr, DustUtils.toString(r));
                v.setTextColor(Color.WHITE);
                v.setBackgroundColor(Color.BLUE);

                for (int c = 0; c < cols.length; ++c) {
                    addCell(tr, DustUtils.sbAppend(null, "", true, "Value (", r, ", ", cols[c], ")").toString());
                }
                tl.addView(tr);
            }
        } else {
            for (String c : cols) {
                tr = new TableRow(ctx);
                addCell(tr, c);
                addCell(tr, c + " value", true);
                tl.addView(tr);
            }
        }

        tl.requestLayout();
    }

    public static <RetType extends View> RetType addCell(TableRow tr, String val) {
        return addCell(tr, val, false);
    }

    public static <RetType extends View> RetType addCell(TableRow tr, String val, boolean edit) {
        View v;
        Context ctx = tr.getContext();

        if (edit) {
            EditText et;
            v = et = new EditText(ctx);
            et.setHint(val);
        } else {
            TextView cell;
            v = cell = new TextView(ctx);
            cell.setText(val);
        }

        v.setPadding(3, 3, 3, 3);
        v.setBackgroundColor(Color.WHITE);

        tr.addView(v);

        return (RetType) v;
    }
}
