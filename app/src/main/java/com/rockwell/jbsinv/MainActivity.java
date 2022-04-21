package com.rockwell.jbsinv;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import android.widget.Button;
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class MainActivity extends Activity implements OnClickListener{
    // used string to cycle through products in preferred order

    // string for found product in search function
    String found = "";

    // string retrieved from server later
    String WHproducts = "";

    // define global excel workbook and sheet
    HSSFWorkbook globalWork;
    HSSFSheet globalSheet;

    // get date and use date as filename for produced sheet
    Date date = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    String strDate = dateFormat.format(date) + ".xls";
    String lastfile = strDate;
    Boolean doOnce = false;
    Boolean do1 = false;

    // define reference cells
    // Description cell
    Cell descCell;
    // location cell
    Cell locCell;
    // warehouse total cell
    Cell whCell;
    // trayformer total cell
    Cell tfCell;
    // partial total cell
    Cell partialCell;

    // string for storing bookmarked product number
    String bookmarked = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File file1 = new File(getExternalFilesDir(null), "InventoryJBS.xls");
        if(file1.exists()){
            do1 = true;
        }else{
            Toast.makeText(getBaseContext(), " Update before using.", Toast.LENGTH_LONG).show();
        }

        /* Instantiate objects */
        // 'Search' Button
        View readExcelButton = findViewById(R.id.readExcel);
        readExcelButton.setOnClickListener(this);
        // 'Update count' Button
        View updateButton = findViewById(R.id.updateBTN);
        updateButton.setOnClickListener(this);
        // 'Back' Button
        View leftButton = findViewById(R.id.leftBtn);
        leftButton.setOnClickListener(this);
        // 'Forw' Button
        View rightButton = findViewById(R.id.rightBtn);
        rightButton.setOnClickListener(this);
        // 'Bookmark' Button
        View bookmarkButton = findViewById(R.id.bookmarkBtn);
        bookmarkButton.setOnClickListener(this);
        // 'Recall' Button
        View recallButton = findViewById(R.id.recallBtn);
        recallButton.setOnClickListener(this);
        // Chemical wh button
        View chemBtn = findViewById(R.id.chemBtn);
        chemBtn.setOnClickListener(this);
        //update xls button
        View updateXLS = findViewById(R.id.updateXL);
        updateXLS.setOnClickListener(this);

        TextView whTxt = (TextView) findViewById(R.id.whTotal);
        TextView paTxt = (TextView) findViewById(R.id.partialTotal);
        TextView tfTxt = (TextView) findViewById(R.id.tfTotal);
        whTxt.setOnClickListener(this);
        paTxt.setOnClickListener(this);
        tfTxt.setOnClickListener(this);

        WHproducts = readProducts();
        // split string into array
        String[] proArr = WHproducts.split(";");

        // product number textbox
        EditText proNum = (EditText) findViewById(R.id.productNUM);
        proNum.setText(proArr[0]);

        // Asterisk display text
        TextView aster = (TextView) findViewById(R.id.asterisk);
        aster.setVisibility(View.INVISIBLE);



        if(do1 == true) {
            /* Create copy of excel file as to not overwrite it */
            // lastfile = date.xls
            File file = new File(getExternalFilesDir(null), lastfile);
            if (file.exists()) {
                Toast.makeText(getBaseContext(), lastfile + " Already exists, ready to take inventory.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), "Copying sheet....", Toast.LENGTH_LONG).show();
                // copy excel sheet with new name
                copyFileAsset(getExternalFilesDir(null) + "/" + "InventoryJBS.xls");
            }

            lastfile = getExternalFilesDir(null) + "/" + lastfile;
            // only works when done twice.....................................................................
            readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
            readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
        }
    }


    public void onClick(View v) {
        Button recallBtn = (Button) findViewById(R.id.recallBtn);
        Button updateXL = (Button) findViewById(R.id.updateXL);
        TextView bookmarked = (TextView) findViewById(R.id.bookLbl);

        TextView whTxt = (TextView) findViewById(R.id.whTotal);
        TextView paTxt = (TextView) findViewById(R.id.partialTotal);
        TextView tfTxt = (TextView) findViewById(R.id.tfTotal);

        if(bookmarked.getText().toString() == ""){
            recallBtn.setEnabled(false);
        }else{
            recallBtn.setEnabled(true);
        }

        // Check that findCell did run and load cell data before a button is pressed
        if(activeC != null){

            int activeCol = activeC.getColumnIndex();

            descCell = activeR.getCell(activeCol + 1);
            locCell = activeR.getCell(activeCol + 6);
            whCell = activeR.getCell(activeCol + 3);
            tfCell = activeR.getCell(activeCol + 4);
            partialCell = activeR.getCell(activeCol + 5);
            EditText proNum = (EditText) findViewById(R.id.productNUM);

            switch (v.getId()) {
                case R.id.readExcel:
                    readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
                    break;
                case R.id.updateBTN:
                    updateCount(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
                    break;
                case R.id.leftBtn:
                    doCycle(0, proNum.getText().toString());
                    break;
                case R.id.rightBtn:
                    doCycle(1, proNum.getText().toString());
                    break;
                case R.id.bookmarkBtn:
                    bookmark("save");
                    break;
                case R.id.recallBtn:
                    bookmark("recall");
                    break;
                case R.id.chemBtn:
                    startActivity(new Intent(MainActivity.this, chemical.class));
                    break;
                case R.id.whTotal:
                    if(whTxt.getText().toString().equals("0")){
                        whTxt.setText("");
                    }
                    break;
                case R.id.partialTotal:
                    if(paTxt.getText().toString().equals("0")){
                        paTxt.setText("");
                    }
                    break;
                case R.id.tfTotal:
                    if(tfTxt.getText().toString().equals("0")){
                        tfTxt.setText("");
                    }
                    break;
                case R.id.updateXL:
                        startActivity(new Intent(this, updater.class));
                        break;
                }


        // if for some reason findCell failed, do it again
        }else{
            if(do1) {
                findCell(globalSheet);
            }
            switch (v.getId()) {
                case R.id.updateXL:
                    startActivity(new Intent(this, updater.class));
                    break;
            }
        }
    }

    private void bookmark(String operation){
        Button recallBtn = (Button) findViewById(R.id.recallBtn);
        EditText proNum = (EditText) findViewById(R.id.productNUM);
        TextView bookmarkLabel = (TextView) findViewById(R.id.bookLbl);

        if(operation == "save"){
            bookmarked = proNum.getText().toString();
            bookmarkLabel.setText(bookmarked);
            recallBtn.setEnabled(true);
        }
        if(operation == "recall"){
            proNum.setText(bookmarked);
            recallBtn.setEnabled(false);
            bookmarkLabel.setText("");
            readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
        }
    }

    private void doCycle(Integer direction, String productNumber) {
        WHproducts = readProducts();
        // split string into array
        String[] proArr = WHproducts.split(";");

        EditText proNum = (EditText) findViewById(R.id.productNUM);
        Button leftBtn = (Button) findViewById(R.id.leftBtn);
        Button rightBtn = (Button) findViewById(R.id.rightBtn);

        //find product number in array and get its index
        int index = -1;
        for (int i = 0; i < proArr.length; i++) {
            if (proArr[i].equals(productNumber)) {
                index = i;
                break;
            }
        }

        leftBtn.setEnabled(true);
        rightBtn.setEnabled(true);

        if (direction == 0) {
            // prevent user from going out of range on array
            if(index != 0) {
                Log.w("------------------", "left hit " + String.valueOf(index));
                // get previous array entry
                proNum.setText(proArr[index - 1]);
                // auto search when cycle buttons are used
                readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
            }else if(index == 0){
                leftBtn.setEnabled(false);
            }
        } else if (direction == 1) {
            // prevent user from going out of range on array
            if(index != proArr.length-1) {
                Log.w("------------------", "right hit " + String.valueOf(index));
                // get previous array entry
                proNum.setText(proArr[index + 1]);
                // auto search when cycle buttons are used
                readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
            }else if(index == proArr.length-1){
                rightBtn.setEnabled(false);
            }
        }
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    private void recycle(){
        /* Load Excel file for use */
        try {
            // Creating Input Stream
            File file = new File(lastfile);
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            globalWork = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            globalSheet = globalWork.getSheetAt(0);

            /* Run findcell to initially fill activeC variable */
            findCell(globalSheet);

            doOnce = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean saveExcelFile(Context context, String fileName, HSSFWorkbook globalWork) {

        // check if storage available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            return false;
        }
        // reset success condition
        boolean success = false;

        try
        {
            //Write the workbook to file system
            FileOutputStream out = new FileOutputStream(lastfile);
            globalWork.write(out);
            out.close();
            System.out.println(lastfile + " saved.");
            success = true;
            recycle();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return success;
    }

    // Currently active cell
    Cell activeC;
    // current row cell is on
    Row activeR;

    private void updateCount(Context context, String fileName, Cell desc, Cell loc, Cell wh, Cell tf, Cell part){
        Log.w("IN UPDATE", String.valueOf(whCell.getColumnIndex()) + ":" + String.valueOf(whCell.getRowIndex()));
        TextView proNumDisplay = (TextView) findViewById(R.id.proLBL);
        TextView descDisplay = (TextView) findViewById(R.id.descLBL);
        TextView locDisplay = (TextView) findViewById(R.id.localeLBL);
        TextView whTot = (TextView) findViewById(R.id.whTotal);
        TextView tfTot = (TextView) findViewById(R.id.tfTotal);
        TextView partTot = (TextView) findViewById(R.id.partialTotal);
        Button updateBtn = (Button) findViewById(R.id.updateBTN);

        String whOperation = whTot.getText().toString();
        String tfOperation = tfTot.getText().toString();
        String partOperation = partTot.getText().toString();
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");

        hideKeyboard();

        Integer whresult = 0;
        Integer tfresult = 0;
        Integer partresult = 0;

        try {
            if(!whOperation.equals("")) {
                whresult = Math.round(Float.parseFloat(engine.eval(whOperation).toString()));
            }
            if(!tfOperation.equals("")) {
                tfresult = Math.round(Float.parseFloat(engine.eval(tfOperation).toString()));
            }
            if(!partOperation.equals("")) {
                partresult = Math.round(Float.parseFloat(engine.eval(partOperation).toString()));
            }

            whTot.setText(whresult.toString());
            tfTot.setText(tfresult.toString());
            partTot.setText(partresult.toString());

            Log.d("Calculator", "Operation: " + whOperation + " result: " + whresult);
        } catch (ScriptException e) {
            Log.d("Calculator", " ScriptEngine error: " + e.getMessage());
        }


        if (activeC != null) {

                if (!whTot.getText().toString().equals("")) {
                    wh.setCellValue(whresult);
                }

                if (!tfTot.getText().toString().equals("")) {
                    tf.setCellValue(tfresult);
                }

                if (!partTot.getText().toString().equals("")){
                    part.setCellValue(partresult);
                }

                saveExcelFile(this, lastfile, globalWork);
            Toast.makeText(getBaseContext(), "Changes saved", Toast.LENGTH_SHORT).show();
            }

    }

    private void readExcelFile(Context context, String filename, Cell desc, Cell loc, Cell wh, Cell tf, Cell part) {
        TextView proNumDisplay = (TextView) findViewById(R.id.proLBL);
        TextView descDisplay = (TextView) findViewById(R.id.descLBL);
        TextView locDisplay = (TextView) findViewById(R.id.localeLBL);
        TextView whTot = (TextView) findViewById(R.id.whTotal);
        TextView tfTot = (TextView) findViewById(R.id.tfTotal);
        TextView partTot = (TextView) findViewById(R.id.partialTotal);
        EditText proNum = (EditText) findViewById(R.id.productNUM);
        TextView aster = (TextView) findViewById(R.id.asterisk);
        // clear any previous entries
        whTot.setText("");
        tfTot.setText("");

        // check if storage available or read-only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.w("FileUtils", "Storage not available or read only");
            return;
        }


        try{

            found = "";

            // find product number user has entered
            found = findCell(globalSheet);
            Log.w("In ReadExcel:::: ", found);

            // only do this if cell was found
            if(found == "cell found") {

                // Update button use accordingly
                Button leftBtn = (Button) findViewById(R.id.leftBtn);
                Button rightBtn = (Button) findViewById(R.id.rightBtn);
                Button searchBtn = (Button) findViewById(R.id.readExcel);
                Button updateBtn = (Button) findViewById(R.id.updateBTN);
                Button bookmarkBtn = (Button) findViewById(R.id.bookmarkBtn);

                leftBtn.setClickable(true);
                leftBtn.setEnabled(true);

                rightBtn.setClickable(true);
                rightBtn.setEnabled(true);

                searchBtn.setClickable(true);
                searchBtn.setEnabled(true);

                updateBtn.setClickable(true);
                updateBtn.setEnabled(true);

                bookmarkBtn.setClickable(true);
                bookmarkBtn.setEnabled(true);

                // logging
                String cellGrid = String.valueOf(activeC.getRowIndex()) + " : " +  String.valueOf(activeC.getColumnIndex());
                Log.w(found, cellGrid);

                // set cells
                int activeCol = activeC.getColumnIndex();

                activeR = activeC.getRow();
                desc = activeR.getCell(activeCol + 1);
                loc = activeR.getCell(activeCol + 6);
                wh = activeR.getCell(activeCol + 3);
                tf = activeR.getCell(activeCol + 4);
                part = activeR.getCell(activeCol + 5);

                DataFormatter fmt = new DataFormatter();

                int i = 0;
                for (Cell cells:activeR) {
                    Log.w("=======================", fmt.formatCellValue(activeR.getCell(i)) + " : " + activeR.getRowNum() + " : " + activeR.getFirstCellNum() );
                    i += 1;
                }

                // UPDATE DISPLAY ITEMS
                String temp = "Product Number: " + activeC.getStringCellValue();
                proNumDisplay.setText(temp);

                temp = "Description: " + desc.getStringCellValue();
                descDisplay.setText(temp);

                temp = "Location: " + loc.getStringCellValue();
                locDisplay.setText(temp);

                // UPDATE COUNT DISPLAY ITEMS
                if((int) wh.getNumericCellValue() == 0){
                    whTot.setText("");
                }else {
                    whTot.setText(String.valueOf((int) wh.getNumericCellValue()));
                }

                if((int) tf.getNumericCellValue() == 0){
                    tfTot.setText("");
                }else {
                    tfTot.setText(String.valueOf((int) tf.getNumericCellValue()));
                }

                if((int) part.getNumericCellValue() == 0){
                    partTot.setText("");
                }else {
                    partTot.setText(String.valueOf((int) part.getNumericCellValue()));
                }

                aster.setVisibility(View.INVISIBLE);
                switch (proNum.getText().toString()){
                    case "600-179427":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "600-179343":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "600-179342":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "500-277627":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "600-173223":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "600-173245":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "600-180862":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "600-142744":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "600-142745":
                        aster.setVisibility(View.VISIBLE);
                        break;
                    case "600-142278":
                        aster.setVisibility(View.VISIBLE);
                        break;
                }
            }else if(found == "not found"){

                // disable buttons accordingly to prevent crash
                Button leftBtn = (Button) findViewById(R.id.leftBtn);
                Button rightBtn = (Button) findViewById(R.id.rightBtn);
                Button searchBtn = (Button) findViewById(R.id.readExcel);
                Button updateBtn = (Button) findViewById(R.id.updateBTN);
                Button bookmarkBtn = (Button) findViewById(R.id.bookmarkBtn);

                leftBtn.setClickable(false);
                leftBtn.setEnabled(false);

                rightBtn.setClickable(false);
                rightBtn.setEnabled(false);

                updateBtn.setClickable(false);
                updateBtn.setEnabled(false);

                bookmarkBtn.setClickable(false);
                bookmarkBtn.setEnabled(false);

                // user still needs to be able to search, make sure its possible
                searchBtn.setClickable(true);
                searchBtn.setEnabled(true);
            }
        }catch (Exception e){e.printStackTrace(); }
        recycle();
        return;
    }

    private String findCell(HSSFSheet sheet) {
        //Get user entered product number
        TextView PO = (TextView) findViewById(R.id.productNUM);
        String POtext = PO.getText().toString();

        //if no number entered or sent from create method
        if(POtext.equals("")){
            POtext = "600-173154";
        }

        // Search for cell with product number and set as ACTIVE
        Log.w("searching for: ", POtext);
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    if (cell.getRichStringCellValue().getString().trim().equals(POtext)) {
                        activeC = cell;
                        activeR = row;
                        activeC.setAsActiveCell();
                        return "cell found";
                    }
                }
            }
        }
        return "not found";
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private void copyFileAsset(String path) {
        // get writable directory and name copy
        File file = new File(getExternalFilesDir(null), lastfile);
        try {
            // copy blank sheet and write it to directory as 'lastfile'.xls

            InputStream in = new FileInputStream(path);
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }

            Log.w("================", "Copied as: " + lastfile);
            // close file streams
            out.close();
            in.close();
            // include writable directory in variable
            //lastfile = getExternalFilesDir(null) + "/" + lastfile;
            Toast.makeText(getBaseContext(), lastfile + " Created", Toast.LENGTH_SHORT).show();
            Log.w("================", "Copied to: " + lastfile);
        } catch (IOException e) {
            Log.w("================", "failed: " + lastfile);
        }
    }

    private String readProducts(){
        File file = new File(getExternalFilesDir(null) + "/products.txt");
        StringBuilder text = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null){
                text.append(line);
            }
            br.close();
        }catch(IOException e){

        }
        return text.toString();
    }
}

