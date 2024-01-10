package com.example.projetopdm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.Manifest;
import android.widget.Toast;


public class Nota extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001; // Use any unique value
    private static final int IMAGE_CAPTURE_CODE = 1001;


    Boolean isCreating= false;
    ImageView imageView;
    Button img_btn;
    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota);

        imageView = (ImageView) findViewById(R.id.imageView);
        img_btn=(Button) findViewById(R.id.img_btn);

        if (getIntent().getStringExtra("GUID").equals("-1")){
            isCreating = true;
            imageView.setVisibility(View.INVISIBLE);
        }
        if (isCreating){

        }
        img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ){
                                if (!Environment.isExternalStorageManager()) {
                                    String [] permission={Manifest.permission.CAMERA};
                                    requestPermissions(permission, CAMERA_PERMISSION_REQUEST_CODE);
                                    checkExternalStoragePermission();
                                }else {
                                    //ja tem as permissoes
                                }
                            }else {
                                openCamera();
                            }

                    }
            }
        });

    }

    private void openCamera(){
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"testes");
        values.put(MediaStore.Images.Media.DESCRIPTION,"APP");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
    }
    private void checkExternalStoragePermission() {
        // Check for external storage management permission on Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            // Request external storage management permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        } else {
            // External storage management permission is granted or not needed
            Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();

        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // CAMERA permission granted, now check for external storage management permission
                checkExternalStoragePermission();
            } else {
                // Handle the case where CAMERA permission is denied
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(image_uri);
            imageView.setVisibility(View.VISIBLE);
        }
    }
}