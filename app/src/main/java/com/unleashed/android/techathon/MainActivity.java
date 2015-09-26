package com.unleashed.android.techathon;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.unleashed.android.techathon.databases.ListingsDB;
import com.unleashed.android.techathon.locationtracker.GpsLocationTracker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {


    private String mLocationSelected;
    private String mCategorySelected;
    private String mSubCategorySelected;
    private String mDescription;
    private String mProductImageLocation;
    private String mPrice;


    private ImageView imgViewProductPic;
    private Button btnSelectImage;
    private Button btnPopulateDescription;
    private Button btnSubmit;
    private ImageButton btnLocation;
    private EditText etDescription;
    private EditText etPrice;
    private Spinner spinner_location;


    private ListingsDB listingDb;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


// Set member variables.
        imgViewProductPic = (ImageView)findViewById(R.id.iv_productPhoto);
        etDescription = (EditText)findViewById(R.id.et_description);
        etPrice = (EditText)findViewById(R.id.et_price);


        mContext = getApplicationContext();


        initializeLocationSpinner(null);
        initializeCategorySpinner();

        initializeDB(mContext);


        // Button to Select Image
        btnSelectImage = (Button) findViewById(R.id.btn_selectImage);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call the select Image routine.
                selectImage();

            }
        });

        // Button to populate description
        btnPopulateDescription = (Button) findViewById(R.id.populateDescription);
        btnPopulateDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{

                    if(etPrice.getText().toString().isEmpty()){
                        Toast.makeText(mContext, "Please fill in required fields before you AutoGenerate Description.", Toast.LENGTH_LONG).show();
                        return;
                    }


                    etDescription.setText("");

                    String p1 = "Dear Reader,\n";
                    String p2 = "I wish to Sell my ";
                    String p3 = " which is of make ";
                    String p4 = "\n.I Reside in  ";
                    String p5 = "\n.I am selling this product at a nominal price of  ";
                    String p6 = mPrice = etPrice.getText().toString();

                    String generatedDescritionMesg = p1 + p2 + mCategorySelected + p3 + mSubCategorySelected + p4 + mLocationSelected + p5 + p6;
                    etDescription.setText(generatedDescritionMesg);

                    mDescription = generatedDescritionMesg;
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });

        // Button to save data to database
        btnSubmit = (Button)findViewById(R.id.btn_Submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etDescription.getText().toString().isEmpty()){
                    Toast.makeText(mContext, "Please Fill In All Fields And Then Click Submit.", Toast.LENGTH_LONG).show();
                    return;
                }


                try{
                    listingDb.insertRecord(mDescription, mProductImageLocation, mLocationSelected, mCategorySelected, mSubCategorySelected , mPrice );
                    Toast.makeText(mContext, "Listing Added Successfully.", Toast.LENGTH_SHORT).show();
                    resetUI();
                }catch (Exception ex){
                    Toast.makeText(mContext, "Error Writing to Listing. Try Again.", Toast.LENGTH_SHORT).show();
                }



            }
        });



        btnLocation = (ImageButton)findViewById(R.id.imgbtn_getLocation);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{

                    GpsLocationTracker mGpsLocationTracker = new GpsLocationTracker(MainActivity.this);

                    /**
                     * Set GPS Location fetched address
                     */
                    if (mGpsLocationTracker.canGetLocation())
                    {

                        Double latitude = mGpsLocationTracker.getLatitude();
                        Double longitude = mGpsLocationTracker.getLongitude();


                        mLocationSelected = getAddress(latitude, longitude);


                        initializeLocationSpinner(mLocationSelected);


                    }
                    else
                    {
                        mGpsLocationTracker.showSettingsAlert();
                    }

                }catch (Exception ex){
                    Toast.makeText( getApplicationContext(),"Error Retrieving Location. Try Again.",Toast.LENGTH_SHORT).show();

                }


            }
        });



    }


    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        String address = "Address Not Found";

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            address = obj.getAddressLine(0);
//            GUIStatics.currentAddress = obj.getSubAdminArea() + ","
//                    + obj.getAdminArea();
//            GUIStatics.latitude = obj.getLatitude();
//            GUIStatics.longitude = obj.getLongitude();
//            GUIStatics.currentCity= obj.getSubAdminArea();
//            GUIStatics.currentState= obj.getAdminArea();
//            add = add + "\n" + obj.getCountryName();
//            add = add + "\n" + obj.getCountryCode();
//            add = add + "\n" + obj.getAdminArea();
//            add = add + "\n" + obj.getPostalCode();
//            add = add + "\n" + obj.getSubAdminArea();
//            add = add + "\n" + obj.getLocality();
//            add = add + "\n" + obj.getSubThoroughfare();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        return address;
    }

    private void resetUI()
    {


        etDescription.setText("");
        etPrice.setText("");
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.select_none);
        imgViewProductPic.setImageBitmap(bm);

    }

    private void initializeDB(Context context) {
        // Create Database during
        listingDb = new ListingsDB(context);
    }

    private void initializeCategorySpinner() {

        // Location Spinner element
        Spinner spinner_category = (Spinner) findViewById(R.id.spinnerMainCategory);
        final Spinner spinner_subcategory = (Spinner) findViewById(R.id.spinnerSubCategory);


        List<String> mainCategory = new ArrayList<String>();
        mainCategory.add("Mobiles");
        mainCategory.add("Cars");
        mainCategory.add("Electronics");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mainCategory);


        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner_category.setAdapter(dataAdapter);

        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                // On selecting a spinner item
                String category_selected = parent.getItemAtPosition(position).toString();

                // Set the member variable with the categroy selected.
                mCategorySelected = category_selected;

                List<String> subCategory = new ArrayList<String>();

                switch (category_selected) {
                    case "Mobiles":
                        subCategory.add("Windows");
                        subCategory.add("Nokia");
                        subCategory.add("Iphone");
                        subCategory.add("Blackberry");
                        subCategory.add("Android");
                        break;


                    case "Cars":
                        subCategory.add("Fiat");
                        subCategory.add("Honda");
                        subCategory.add("Maruti");
                        subCategory.add("Hyundai");
                        subCategory.add("Volkswagon");
                        subCategory.add("Skoda");
                        break;


                    case "Electronics":
                        subCategory.add("Air Conditioner");
                        subCategory.add("TV");
                        subCategory.add("Refrigerator");
                        subCategory.add("Camera");
                        subCategory.add("Washing Machine");
                        break;
                }

                // Creating adapter for spinner
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, subCategory);

                // Drop down layout style - list view with radio button
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // attaching data adapter to spinner
                spinner_subcategory.setAdapter(dataAdapter);

                spinner_subcategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // On selecting a spinner item
                        String subcategory_selected = parent.getItemAtPosition(position).toString();

                        // Set the member variable with the categroy selected.
                        mSubCategorySelected = subcategory_selected;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private void initializeLocationSpinner(String text) {
        // Location Spinner element
        spinner_location = (Spinner) findViewById(R.id.spinnerLocation);

        // Location Spinner click listener
        //spinner_location.setOnItemSelectedListener(this);

        List<String> locations = new ArrayList<String>();
        if(text != null){
            locations.add(text);
        }
        locations.add("Delhi");
        locations.add("Mumbai");
        locations.add("Bangalore");
        locations.add("Kolkata");



        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locations);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner_location.setAdapter(dataAdapter);
        spinner_location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();

                // Set the location selected.
                mLocationSelected = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    // Store the path of file
                    mProductImageLocation = f.getAbsolutePath();

                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    imgViewProductPic.setImageBitmap(bitmap);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {

                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);

                // Set Picture Path
                mProductImageLocation = picturePath;

                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

                imgViewProductPic.setImageBitmap(thumbnail);
            }
        }
    }
}
