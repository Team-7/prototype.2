package au.edu.monash.fit2081.scrollViewDemo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

// main (and only) Activity class for the Scroll View Demo app
public class ScrollViewDemoActivity extends Activity {
    // create reference variables (not pointed yet)
    private SharedPreferences savedURLs; 	// favorite search queries persistent data file

    private TableLayout queryTableLayout; 		// will contain favorite search query rows
    private EditText queryEditText; 			// where the user enters a search query
    private EditText tagEditText; 				// where the user enters a search query's tag

    // called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 							// let super class do its stuff
        setContentView(R.layout.main); 	// inflates the initial layout (GUI)

        // set a ref to SharedPreferences that contains the user's saved searches
        savedURLs = getSharedPreferences("searches", MODE_PRIVATE);

        // set a reference to the queryTableLayout
        queryTableLayout = (TableLayout) findViewById(R.id.queryTableLayout); //THIS IS THE TABLELAYOUT INSIDE THE SCROLLVIEW!!!

        // set references to the two EditTexts
        queryEditText = (EditText) findViewById(R.id.queryEditText);
        tagEditText = (EditText) findViewById(R.id.tagEditText);

        // register listeners for the Save and Clear Tags Buttons
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);

        Button clearTagsButton = (Button) findViewById(R.id.clearTagsButton);
        clearTagsButton.setOnClickListener(clearTagsButtonListener);

        refreshButtons(null); // add previously saved search queries to GUI
    }

    // GUI only
    // if called with null parameter make and display TableRows and buttons for existing tag/search pairs in memory (i.e. an initial task - see OnCreate)
    // if called with non-null parameter make and display TableRow and buttons for a new tag/search pair (i.e. a non initial task - see makeTag)
    private void refreshButtons(String newTag) {
        // store saved tags (not search queries) in the tags array
        String[] tags = savedURLs.getAll().keySet().toArray(new String[0]); //String array will expand to accommodate
        Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER); // sort by tag

        if (newTag != null) // newTag is not null so call makeTagGUI JUST for the newTag
            makeTagGUI(newTag, Arrays.binarySearch(tags, newTag)); //array must be sorted for sensible results of a binary search (returns index of first found)
        else{ // newTag is null so call makeTagGUI for all existing tags
            for (int index = 0; index < tags.length; ++index)
                makeTagGUI(tags[index], index);
        }
    }

    // GUI + MEMORY
    // Update memory and if tag is new or edited update GUI (via refreshButtons(tag)
    private void makeTag(String query, String tag) {
        // originalQuery = null if can't find tag in savedURLs
        // 2nd param of getString is return value if can't find tag
        String originalQuery = savedURLs.getString(tag, null);

        // null could result from totally new tag or edited existing tag
        // either way the tag is new
        // note: edited query + unedited tag will NOT result in null
        //       this is OK since GUI does not require a refresh

        // whatever has been updated tag and or query memory needs to be updated
        SharedPreferences.Editor preferencesEditor = savedURLs.edit();
        // ***the following either adds new OR edited tag's tag/search pair or
        // ***updates a found tag's query
        preferencesEditor.putString(tag, query);
        preferencesEditor.apply(); // store the new or updated tag/search pair

        // only update GUI if tag is new or has changed (query not visible so changes to it do not require a GUI update)
        if (originalQuery == null) //i.e. couldn't find tag amongst saved tags
            refreshButtons(tag);
    }


    // GUI only
    //CREATE NEW TABLEROW for tag at index, ADD TO TABLELAYOUT IN SCROLLVIEW
    // i.e. visibly update the GUI but not memory (savedURLs)
    private void makeTagGUI(String tag, int index) {
        // get a reference to the LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate new_tag_view.xml to create new row and set up the contained Buttons
        View newTagView = inflater.inflate(R.layout.new_tag_view, null);

        // get newTagButton reference, set its text and register its listener
        Button newTagButton = (Button) newTagView.findViewById(R.id.newTagButton);
        newTagButton.setText(tag);

        newTagButton.setOnClickListener(queryButtonListener);

        // get newEditButton reference and register its listener
        Button newEditButton = (Button) newTagView.findViewById(R.id.newEditButton);

        newEditButton.setOnClickListener(editButtonListener);

        // add new tag and edit buttons to queryTableLayout at specified row number (index)
        queryTableLayout.addView(newTagView, index);
    }

    // clear all TableRows (each containing a tag and edit button pair)
    // from the TableLayout in the ScrollView
    private void clearButtons() {
        queryTableLayout.removeAllViews();
    }


    // REFERENCE VARIABLE DECLARATION CONTAINING ANONYMOUS INNER CLASS DEFINITION
    // THAT IMPLEMENTS THE ONE METHOD VIEW.ONCLICKLISTENER INTERFACE
    // This implements the Save button which saves any tag/search changes including additions
    public OnClickListener saveButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // create tag if both queryEditText and tagEditText are not empty
            if (queryEditText.getText().length() > 0 && tagEditText.getText().length() > 0) {
                // makeTag(...) updates memory + calls refreshButtons(�) to update GUI if necessary
                makeTag(queryEditText.getText().toString(), tagEditText.getText().toString());

                //job done (added new tag/search) so clear fields for consistency
                queryEditText.setText("");
                tagEditText.setText("");

                // hide the soft keyboard displayed to fill out edit fields
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
            }
            else{ // display message asking user to provide both a query and a tag
                // create a new AlertDialog Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(ScrollViewDemoActivity.this); //parameter is an Activity instance which is the current context

                // use builder to set dialog's properties
                builder.setTitle(R.string.missingTitle);
                builder.setPositiveButton(R.string.OK, null); // Android handles click, closes dialog
                builder.setMessage(R.string.missingMessage);

                // use builder to create and show dialog
                AlertDialog errorDialog = builder.create();
                errorDialog.show(); // display the Dialog
                // builder.show(); // this is used instead of the above 2 statements elsewhere.
                // the only reason to use the 2 statements is if there is additional processing to be done
                // after creation but before showing. Not applicable here so just use show. Doco says all of this.
            }
        } // end onClick()
    }; //end class definition, end ref var declaration

    // REFERENCE VARIABLE DECLARATION CONTAINING ANONYMOUS INNER CLASS DEFINITION
    // THAT IMPLEMENTS THE ONE METHOD VIEW.ONCLICKLISTENER INTERFACE
    // This implements the Clear Tags button which removes all tag/search pairs from memory and GUI
    // *** contains an anonymous class definition (interface OnClickListener)
    // *** which itself contains an anonymous class definition (interface DialogInterface.OnClickListener)
    public OnClickListener clearTagsButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(ScrollViewDemoActivity.this);

            //use builder to set dialog's properties
            builder.setTitle(R.string.confirmTitle); // title bar string

            // provide an OK button that simply dismisses the dialog
            // 2nd param not null but an inner inner anonymous class used to auto create
            // a listener object that executes when the dialog's button is clicked
            builder.setPositiveButton(R.string.erase, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int button) {
                    // update GUI
                    clearButtons();

                    // update memory
                    SharedPreferences.Editor preferencesEditor = savedURLs.edit();

                    preferencesEditor.clear(); // remove all tag/search pairs
                    preferencesEditor.apply(); // do it!
                } // end dialog's onClick(...)
            } // end dialog's inner listener class
            ); // end dialog's setPositiveButton call

            builder.setNegativeButton(R.string.cancel, null); //no listener for a response just let Android close the dialog
            builder.setCancelable(true); // backspace key hard key for dialog cancel button

            // set the message to display
            builder.setMessage(R.string.confirmMessage);

            // use builder to create and show dialog
            AlertDialog confirmDialog = builder.create();
            confirmDialog.show(); // display the Dialog
        } // end onClick()
    }; //end class definition, end ref var declaration

    // REFERENCE VARIABLE DECLARATION CONTAINING ANONYMOUS INNER CLASS DEFINITION
    // THAT IMPLEMENTS THE ONE METHOD VIEW.ONCLICKLISTENER INTERFACE
    // this is registered to each and every tag button in makeTagGUI
    // This implements the tag button which "intents" the tag's twitter search in a browser
    public OnClickListener queryButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the search query by using the clicked tag button�s (v's) text as a key
            String buttonText = ((Button) v).getText().toString();
            String query = savedURLs.getString(buttonText, ""); //2nd param if not found

            String encQuery = "";

            // create the URL corresponding to the touched Button's query
            try {
                encQuery = URLEncoder.encode(query, "UTF-8"); //who knows what non URL characters the user has entered as a query
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String urlString = getString(R.string.searchURL) + encQuery;

            // generically asks to display the data referenced by the 2nd parameter,
            // in this case since its a URL it uses a browser to display it
            // (e.g. if it referenced a contacts entry it would use the contacts books
            // to display the references data)
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));

            startActivity(webIntent); // execute the Intent
        } // end onClick()
    }; //end class definition, end ref var declaration

    // REFERENCE VARIABLE DECLARATION CONTAINING ANONYMOUS INNER CLASS DEFINITION
    // THAT IMPLEMENTS THE ONE METHOD VIEW.ONCLICKLISTENER INTERFACE	// create a new OnClickListener for editing the tag associated with the clicked Edit button
    // this is registered to each and every edit button in makeTagGUI
    // This implements the Edit button which allows editing of any tag/search
    public OnClickListener editButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // get all necessary GUI components
            TableRow buttonTableRow = (TableRow) v.getParent();

            // get tag button text from tag button in same TableRow as clicked edit button
            Button searchButton = (Button) buttonTableRow.findViewById(R.id.newTagButton); //bad name searchButton, should be tagButton
            String tag = searchButton.getText().toString();

            // flush EditTexts to match the chosen tag and its query
            tagEditText.setText(tag);
            queryEditText.setText(savedURLs.getString(tag, ""));
        } // end onClick
    }; //end class definition, end ref var declaration

} // end class ScrollViewDemoActivity

/*************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and * Pearson Education,
 * Inc. All Rights Reserved. * * DISCLAIMER: The authors and publisher of this
 * book have used their * best efforts in preparing the book. These efforts
 * include the * development, research, and testing of the theories and programs
 * * to determine their effectiveness. The authors and publisher make * no
 * warranty of any kind, expressed or implied, with regard to these * programs
 * or to the documentation contained in these books. The authors * and publisher
 * shall not be liable in any event for incidental or * consequential damages in
 * connection with, or arising out of, the * furnishing, performance, or use of
 * these programs. *
 *************************************************************************/
