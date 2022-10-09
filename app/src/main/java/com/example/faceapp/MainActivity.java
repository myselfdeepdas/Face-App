package com.example.faceapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.faceapp.deepHelper.deepGraphicOverlay;
import com.example.faceapp.deepHelper.deepRectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.IOException;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    CameraView cameraView;
    deepGraphicOverlay deepgraphicOverlay;
    Button btn;
    private final int GALLERY_INTENT_CODE = 993;
    ImageView camera_face_change,photo_import_from_gallery,gallery_photo_show;
    AlertDialog waitingdialog;
    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView=findViewById(R.id.camera);
        camera_face_change = findViewById(R.id.camera_face_change);
        gallery_photo_show = findViewById(R.id.gallery_photo_show);
        photo_import_from_gallery=findViewById(R.id.photo_import_from_gallery);
        deepgraphicOverlay=findViewById(R.id.graphic_overlay);
        btn=findViewById(R.id.open);
        waitingdialog=new SpotsDialog.Builder().setContext(this)
                .setMessage("please wait")
                .setCancelable(false)
                .build();

        photo_import_from_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(
                                intent,
                                "Select Image from here..."),
                        GALLERY_INTENT_CODE);
            }
        });

        camera_face_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.isFacingBack()) cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                else cameraView.setFacing(CameraKit.Constants.FACING_BACK);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gallery_photo_show.setVisibility(View.GONE);
                cameraView.setVisibility(View.VISIBLE);
                cameraView.start();
                cameraView.captureImage();
                deepgraphicOverlay.clear();
            }
        });
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingdialog.show();
                Bitmap bitmap=cameraKitImage.getBitmap();
                bitmap=Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();

                runFaceDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void runFaceDetector(Bitmap bitmap) {
        FirebaseVisionImage image=FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions options=new FirebaseVisionFaceDetectorOptions.Builder()
                .build();
        FirebaseVisionFaceDetector detector= FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        processFaceResult(firebaseVisionFaces);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {
        int count=0;
        for(FirebaseVisionFace face : firebaseVisionFaces)
        {
            Rect bounds=face.getBoundingBox();
            deepRectOverlay rect=new deepRectOverlay(deepgraphicOverlay,bounds);
            deepgraphicOverlay.add(rect);
            count++;
        }
        waitingdialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setMessage(String.format("Detected %d faces in image",count));
        builder.show();
    }

    //Photo Import from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = (Uri) data.getData();
            // Image received from gallery
            if (requestCode == GALLERY_INTENT_CODE) {
                try {
                    // Converting the image uri to bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    waitingdialog.show();
                    bitmap=Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                    gallery_photo_show.setVisibility(View.VISIBLE);
                    cameraView.setVisibility(View.GONE);
                    gallery_photo_show.setImageBitmap(bitmap);
                    runFaceDetector(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}