package com.cdap.safetyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cdap.safetyapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class gurdian extends AppCompatActivity {

    EditText name,email,number,password;
    Button update;
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    String userId;
    String dispalyname;
    FirebaseUser user;
    URI uri;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;
    DocumentReference documentReference;
    FirebaseFirestore db  = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gurdian);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();
        documentReference = db.collection("users").document(currentuid);





        name=(EditText)findViewById(R.id.txtname);
        email=(EditText)findViewById(R.id.txtemail);
        number=(EditText)findViewById(R.id.txtphoneno);
        update=(Button)findViewById(R.id.update);

        fauth=FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();
        user=fauth.getCurrentUser();

        userId=fauth.getCurrentUser().getUid();

        DocumentReference documentReference=fstore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                name.setText(value.getString("name"));
                email.setText(value.getString("email"));
                number.setText(value.getString("telephoneNumber"));

            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();

            }
        });





    }

    private void update() {
         final String n = name.getText().toString();
        final String e = email.getText().toString();
        final String t = number.getText().toString();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid= user.getUid();
        final  DocumentReference sDoc = db.collection("users").document(currentuid);
        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(sDoc);

                transaction.update(sDoc,"name",n);
                transaction.update(sDoc,"email",e);
                transaction.update(sDoc,"telephoneNumber",t);


                // Success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(gurdian.this, "Successfully updated", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(gurdian.this, "Profile Did't Update", Toast.LENGTH_SHORT).show();
                    }
                });




    }

    }

