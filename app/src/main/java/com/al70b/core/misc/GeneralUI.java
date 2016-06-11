package com.al70b.core.misc;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.al70b.R;

import java.util.List;

/**
 * Created by Naseem on 8/13/2015.
 */
public class GeneralUI {

    /**
     * build the table rows for the interested purpose, this way the table can grow or shrink
     * automatically depending only on the data from the server
     *
     * @param tableLayout
     */
    public static void buildInterestedPurposeLayout(Context context, TableLayout tableLayout, int colNum,
                                                    List<CheckBox> listOfCheckBoxes, List<String> listOfString) {
        int dp4 = (int) (4 / context.getResources().getDisplayMetrics().density);

        // create first row with appropriate layout
        TableRow tableRow = new TableRow(context.getApplicationContext());
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(rowParams);
        tableRow.setPadding(dp4, dp4, dp4, dp4);

        // add it to the table
        tableLayout.addView(tableRow);

        int i = 0;
        for (String s : listOfString) {
            if (i != 0 && i % colNum == 0) {
                // create a new row when colNum items were added to the previous one
                tableRow = new TableRow(context);
                tableRow.setLayoutParams(rowParams);
                tableRow.setPadding(dp4, dp4, dp4, dp4);
                tableLayout.addView(tableRow);
            }

            // create appropriate check box with title
            CheckBox chkBox = createCheckBox(context, s);

            // add it to the row
            tableRow.addView(chkBox);

            // add check box to the list of checkboxes for further use
            listOfCheckBoxes.add(chkBox);

            // set userID for this check box to use with the map
            chkBox.setId(listOfCheckBoxes.indexOf(chkBox));

            // increment i for dividing into rows of colNum
            i++;
        }

        for (; i % colNum != 0; i++) {
            CheckBox chkBox = createCheckBox(context, "");
            chkBox.setVisibility(View.INVISIBLE);
            tableRow.addView(chkBox);
        }
    }

    private static CheckBox createCheckBox(Context context, String s) {
        CheckBox chkBox = (CheckBox) ((Activity) (context)).getLayoutInflater().inflate(R.layout.checkbox, null);
        chkBox.setText(s);
        return chkBox;
    }
}
