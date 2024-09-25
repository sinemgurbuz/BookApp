package com.sinemgrbz.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinemgrbz.bookapp.adapters.AdapterPdfAdmin;
import com.sinemgrbz.bookapp.databinding.ActivityPdfAddBinding;
import com.sinemgrbz.bookapp.databinding.ActivityPdfListAdminBinding;
import com.sinemgrbz.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {
    //arraylisle modelPdf 'deki verilerin listesini tutacak.
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfAdmin adapterPdfAdmin;
    private ActivityPdfListAdminBinding binding;
    private String categoryId,categoryTitle;

    private static final String TAG = "PDF_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //intent ile verileri al.AdapterCategory den
        Intent intent=getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        binding.subtitletv.setText(categoryTitle);

        loadPdfList();

        //arama işlemi
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //kullanıcı her bir harfi yazdığında arama yapsın
                try {
                    adapterPdfAdmin.getFilter().filter(s);

                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage() );
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void loadPdfList() {
        pdfArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelPdf model=ds.getValue(ModelPdf.class);
                            pdfArrayList.add(model);
                            Log.d(TAG, "onDataChange: "+model.getId()+" "+model.getTitle());
                        }
                        adapterPdfAdmin=new AdapterPdfAdmin(PdfListAdminActivity.this,pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdfAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}