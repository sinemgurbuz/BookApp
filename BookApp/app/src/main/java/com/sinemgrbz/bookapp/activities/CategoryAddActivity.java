package com.sinemgrbz.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sinemgrbz.bookapp.databinding.ActivityCategoryAddBinding;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    private ActivityCategoryAddBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        //geri buttonunu aktif edelim.
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //geri buttonu
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        //kategori ekleme buttonu aktif edelim
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    //ekleme yapmadan önce verileri dogrulayalım.
    private String category="";
    private void validateData() {
        category=binding.categoryEt.getText().toString().trim();
        //boş değilse verileri doğrula
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this,"Please Enter Category...",Toast.LENGTH_SHORT).show();
        }else{
            //boş degil ise
            addCategoryFirebase();
            binding.categoryEt.setText("");
        }
    }

    private void addCategoryFirebase() {

        long timestamp=System.currentTimeMillis();
        //System.currentTimeMillis() metodu, sistem saatini milisaniye cinsinden döndürür.
        //Bu kod, şu anda geçerli olan sistem saatinden elde edilen zaman damgasını timestamp değişkenine atar.

        //veritabanına eklemeden önceki hazırlıklar
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //veritabanına ekleme yapalım. Kategoriler->KategoriId,Kategoribilgileri
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    //Biography
                    public void onSuccess(Void unused) {
                        //kategoriyi eklenirse
                        Toast.makeText(CategoryAddActivity.this,"Category Added Successfully...",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //kategori eklenmez ise
                        Toast.makeText(CategoryAddActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }
}