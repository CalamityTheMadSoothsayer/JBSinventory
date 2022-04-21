        package com.rockwell.jbsinv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import android.widget.Button;
import android.widget.Toast;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class chemical extends Activity implements OnClickListener{

    String WHproducts =
            "";


    String found = "";

    HSSFWorkbook globalWork;
    HSSFSheet globalSheet;

    Date date = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    String strDate = dateFormat.format(date) + ".xls";
    String lastfile = strDate;
    Boolean doOnce = false;

    Cell descCell;
    Cell locCell;
    Cell whCell;
    Cell tfCell;
    Cell partialCell;

    String bookmarked = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chemical);

        /* Instantiate objects */
        View readExcelButton = findViewById(R.id.readExcel2);
        readExcelButton.setOnClickListener(this);
        View updateButton = findViewById(R.id.updateBTN2);
        updateButton.setOnClickListener(this);
        View leftButton = findViewById(R.id.leftBtn2);
        leftButton.setOnClickListener(this);
        View rightButton = findViewById(R.id.rightBtn2);
        rightButton.setOnClickListener(this);
        View bookmarkButton = findViewById(R.id.bookmarkBtn2);
        bookmarkButton.setOnClickListener(this);
        View recallButton = findViewById(R.id.recallBtn2);
        recallButton.setOnClickListener(this);

        TextView whTxt2 = (TextView) findViewById(R.id.whTotal2);
        whTxt2.setOnClickListener(this);

        WHproducts = readProducts();
        String[] proArr = WHproducts.split(";");

        EditText proNum = (EditText) findViewById(R.id.productNUM2);
        proNum.setText(proArr[0]);

        lastfile = getExternalFilesDir(null) + "/" + lastfile;
        readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
        readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
        Log.d("IN CREATE::::: ", globalSheet.getSheetName());
    }


    public void onClick(View v) {
        Button recallBtn = (Button) findViewById(R.id.recallBtn2);
        TextView bookmarked = (TextView) findViewById(R.id.bookLbl2);

        TextView whTxt2 = (TextView) findViewById(R.id.whTotal2);

        if(bookmarked.getText().toString() == ""){
            recallBtn.setEnabled(false);
        }else{
            recallBtn.setEnabled(true);
        }

        // Check that findCell did run and load cell data before a button is pressed
        if(activeC != null){
            Log.w("IN chem ON CLICK", activeC.toString());
            int activeCol = activeC.getColumnIndex();

            descCell = activeR.getCell(activeCol - 1);
            locCell = activeR.getCell(activeCol + 3);
            whCell = activeR.getCell(activeCol + 4);
            EditText proNum = (EditText) findViewById(R.id.productNUM2);

            switch (v.getId()) {
                case R.id.readExcel2:
                    readExcelFile(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
                    break;
                case R.id.updateBTN2:
                    updateCount(this, lastfile, descCell, locCell, whCell, tfCell, partialCell);
                    break;
                case R.id.leftBtn2:
                    doCycle(0, proNum.getText().toString());
                    break;
                case R.id.rightBtn2:
                    doCycle(1, proNum.getText().toString());
                    break;
                case R.id.bookmarkBtn2:
                    bookmark("save");
                    break;
                case R.id.recallBtn2:
                    bookmark("recall");
                    break;
                case R.id.whTotal:
                    if(whTxt2.getText().toString().equals("0")){
                        whTxt2.setText("");
                    }
            }


            // if for some reason findCell failed, do it again
        }else{
            findCell(globalSheet);
        }
    }

    private void bookmark(String operation){
        Button recallBtn = (Button) findViewById(R.id.recallBtn2);
        EditText proNum = (EditText) findViewById(R.id.productNUM2);
        TextView bookmarkLabel = (TextView) findViewById(R.id.bookLbl2);

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
        EditText proNum = (EditText) findViewById(R.id.productNUM2);
        Button leftBtn = (Button) findViewById(R.id.leftBtn2);
        Button rightBtn = (Button) findViewById(R.id.rightBtn2);

        WHproducts = readProducts();
        String[] proArr = WHproducts.split(";");

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
            globalSheet = globalWork.getSheetAt(1);

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
        TextView proNumDisplay = (TextView) findViewById(R.id.proLBL2);
        TextView descDisplay = (TextView) findViewById(R.id.descLBL2);
        TextView whTot = (TextView) findViewById(R.id.whTotal2);
        Button updateBtn = (Button) findViewById(R.id.updateBTN2);

        String whOperation = whTot.getText().toString();
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");

        hideKeyboard();

        Integer whresult = 0;

        try {
            if(!whOperation.equals("")) {
                whresult = Math.round(Float.parseFloat(engine.eval(whOperation).toString()));
            }

            whTot.setText(whresult.toString());

            Log.d("Calculator", "Operation: " + whOperation + " result: " + whresult);
        } catch (ScriptException e) {
            Log.d("Calculator", " ScriptEngine error: " + e.getMessage());
        }


        if (activeC != null) {

            if (!whTot.getText().toString().equals("")) {
                wh.setCellValue(whresult);
            }

            saveExcelFile(this, lastfile, globalWork);
            Toast.makeText(getBaseContext(), "Changes saved", Toast.LENGTH_SHORT).show();
        }

    }

    private void readExcelFile(Context context, String filename, Cell desc, Cell loc, Cell wh, Cell tf, Cell part) {
        TextView proNumDisplay = (TextView) findViewById(R.id.proLBL2);
        TextView descDisplay = (TextView) findViewById(R.id.descLBL2);
        TextView whTot = (TextView) findViewById(R.id.whTotal2);
        EditText proNum = (EditText) findViewById(R.id.productNUM2);
        // clear any previous entries
        whTot.setText("");

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
                Button leftBtn = (Button) findViewById(R.id.leftBtn2);
                Button rightBtn = (Button) findViewById(R.id.rightBtn2);
                Button searchBtn = (Button) findViewById(R.id.readExcel2);
                Button updateBtn = (Button) findViewById(R.id.updateBTN2);
                Button bookmarkBtn = (Button) findViewById(R.id.bookmarkBtn2);

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
                descCell = activeR.getCell(activeCol - 1);
                locCell = activeR.getCell(activeCol + 3);
                whCell = activeR.getCell(activeCol + 4);

                DataFormatter fmt = new DataFormatter();

                int i = 0;
                for (Cell cells:activeR) {
                    Log.w("=======================", fmt.formatCellValue(activeR.getCell(i)) + " : " + activeR.getRowNum() + " : " + activeR.getFirstCellNum() );
                    i += 1;
                }

                // UPDATE DISPLAY ITEMS
                String temp = "Product Number: " + activeC.getStringCellValue();
                proNumDisplay.setText(temp);

                temp = "Description: " + descCell.getStringCellValue();
                descDisplay.setText(temp);

                // UPDATE COUNT DISPLAY ITEMS
                whTot.setText(String.valueOf((int) whCell.getNumericCellValue()));

            }else if(found == "not found"){

                // disable buttons accordingly to prevent crash
                Button leftBtn = (Button) findViewById(R.id.leftBtn2);
                Button rightBtn = (Button) findViewById(R.id.rightBtn2);
                Button searchBtn = (Button) findViewById(R.id.readExcel2);
                Button updateBtn = (Button) findViewById(R.id.updateBTN2);
                Button bookmarkBtn = (Button) findViewById(R.id.bookmarkBtn2);

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
        TextView PO = (TextView) findViewById(R.id.productNUM2);
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

    private String readProducts(){
        File file = new File(getExternalFilesDir(null) + "/chemicals.txt");
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