package com.eiadmreh.uploadimagetest;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btnUpload, btnChooseImage, btnShow;
    private EditText editTextFileName,editTextprice,editTextcode;
    private ImageView loadedImage;
    private ArrayList<Tool> mTools;
    private Uri imageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask storageTask;
    private long MaxId=0,SerialCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        btnChooseImage = (Button) findViewById( R.id.btnChoose );
        btnUpload = (Button) findViewById( R.id.btnUpload );
        editTextFileName = (EditText) findViewById( R.id.editTextFileName );
        editTextprice=(EditText)findViewById(R.id.editTextPrice);
        editTextcode=(EditText)findViewById(R.id.editTextCode);
        loadedImage = (ImageView) findViewById( R.id.imgView );
        btnShow = (Button) findViewById( R.id.btnShow );
        mTools = new ArrayList<Tool>();
        mStorageRef = FirebaseStorage.getInstance().getReference("Tools");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Tools");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                     MaxId=dataSnapshot.getChildrenCount();
                SerialCode=123000+MaxId+1;
                editTextcode.setText("Serial Code: "+String.valueOf(SerialCode));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnChooseImage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextcode.setText("Serial Code: "+String.valueOf(SerialCode));
                editTextFileName.setText("");
                editTextprice.setText("");
                openFileChooser();
            }
        } );

        btnUpload.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(storageTask != null && storageTask.isInProgress()){
                    Toast.makeText( MainActivity.this,"Please wait until finish uplopad!",Toast.LENGTH_SHORT ).show();
                }else
                    uploadFile();
            }
        } );

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowSelectedTool();
            }
        });

    }

    private void uploadFile() {

        if(imageUri != null ){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final String imagePath = System.currentTimeMillis() +"." + getFileExtension( imageUri );
            StorageReference fileReference = mStorageRef.child(imagePath);
            storageTask = fileReference.putFile( imageUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    final Tool tool = new Tool( editTextFileName.getText().toString().trim(),SerialCode,Double.parseDouble(editTextprice.getText().toString().trim()) ,imagePath );
                    final Long uploadId = MaxId+1;
                    mDatabaseRef.child( String.valueOf(uploadId) ).setValue(tool);
                    Toast.makeText( MainActivity.this, "Upload done", Toast.LENGTH_SHORT ).show();

                    mDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Tool tool = postSnapshot.getValue(Tool.class);
                                mTools.add( tool );
                            }

                            try {

                                //showing static image (the image at position 0 )
                                StorageReference islandRef = mStorageRef.child(mTools.get( 0 ).getImageUrl());
                                final File localFile = File.createTempFile("images", "jpg");
                                islandRef.getFile(localFile).addOnSuccessListener( new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Bitmap bitmap = BitmapFactory.decodeFile( localFile.getAbsolutePath() );
                                       // loadedImage.setImageBitmap( bitmap );
                                    }
                                } );

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    } );


                }
            } ).addOnFailureListener( new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText( MainActivity.this,e.toString(),Toast.LENGTH_SHORT  ).show();

                }

            } );
        }else {
            Toast.makeText( this,"select an image ", Toast.LENGTH_SHORT ).show();
        }

    }
    private void ShowSelectedTool() {
        final long toolCode= Long.parseLong(editTextcode.getText().toString());
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Tools");

        Query q =myRef.orderByChild( "tCode" ).equalTo( toolCode );
        q.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                        Tool tool = ds.getValue(Tool.class);
                        editTextFileName.setText(tool.gettName());
                        editTextprice.setText("" + tool.gettPrice());

                       // Toast.makeText(MainActivity.this,"No Tool Found !!!,Try again.",Toast.LENGTH_SHORT).show();
                    try {
                        StorageReference riversRef = mStorageRef.child(tool.getImageUrl());
                        final File localFile = File.createTempFile("Tools", getImageType( tool.getImageUrl( )));
                        riversRef.getFile(localFile)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Successfully downloaded data to local file
                                        // ...
                                        Bitmap bitmap = BitmapFactory.decodeFile( localFile.getAbsolutePath() );
                                        loadedImage.setImageBitmap( bitmap );


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle failed download
                                // ...
                                Toast.makeText( MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG ).show();

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        } );


        }
private String getImageType(String name){
        String [] type = name.split( "\\." );
        return type[1];
        }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType( cR.getType( uri ) );
    }
    private void openFileChooser() {
        Intent intent = new Intent(  );
        intent.setType( "image/*" );
        intent.setAction( Intent.ACTION_GET_CONTENT );
        startActivityForResult( intent, PICK_IMAGE_REQUEST );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            loadedImage.setImageURI( imageUri );
           // Picasso.get().load(imageUri).into(loadedImage);


        }
    }
}

