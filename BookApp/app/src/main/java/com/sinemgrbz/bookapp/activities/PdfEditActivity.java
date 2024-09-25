package com.sinemgrbz.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinemgrbz.bookapp.databinding.ActivityPdfEditBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {
    private ActivityPdfEditBinding binding;
    private String bookId;
    private ArrayList<String> CategoryTitleArrayList,CategoryIdArrayList;
    public static final String TAG="BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bookId=getIntent().getStringExtra("bookId");

        LoadCategories();
        LoadBookInfo();

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryDialog();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //geri buttonu
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidateData();
            }
        });
    }

    private void LoadBookInfo() {
        Log.d(TAG, "LoadBookInfo: Kitap bilgisi yükleniyor.");
        DatabaseReference refBooks= FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SelectedCategoryId=""+snapshot.child("categoryId").getValue();
                        String description=""+snapshot.child("description").getValue();
                        String title=""+snapshot.child("title").getValue();

                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        DatabaseReference refBookCategory=FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(SelectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category=""+snapshot.child("category").getValue();
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String title="",description="";
    private void ValidateData(){
        title=binding.titleEt.getText().toString().trim();
        description=binding.descriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Başlık boş geçilemez...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Açıklama boş geçilemez...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(SelectedCategoryId)) {
            Toast.makeText(this, "Kategori boş geçilemez...", Toast.LENGTH_SHORT).show();
        }else{
            UpdateInfo();
        }
    }

    private void UpdateInfo() {
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("title",title);
        hashMap.put("description",description);
        hashMap.put("categoryId",SelectedCategoryId);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfEditActivity.this, "Kitap bilgileri güncellendi..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PdfEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private String SelectedCategoryId="",SelectedCategoryTitle="";
    private void CategoryDialog(){
        String[] categoriesArray=new String[CategoryTitleArrayList.size()];
        for(int i=0;i<CategoryTitleArrayList.size();i++){
            categoriesArray[i]=CategoryTitleArrayList.get(i);

        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Kategori Seçin")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SelectedCategoryId=CategoryIdArrayList.get(which);
                        SelectedCategoryTitle=CategoryTitleArrayList.get(which);

                        binding.categoryTv.setText(SelectedCategoryTitle);
                    }
                })
                .show();
    }
    private void LoadCategories() {
        Log.d(TAG, "LoadCategories: kategori yükleniyor.");
        CategoryIdArrayList=new ArrayList<>();
        CategoryTitleArrayList=new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CategoryIdArrayList.clear();
                CategoryTitleArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String id=""+ds.child("id").getValue();
                    String category=""+ds.child("category").getValue();
                    CategoryTitleArrayList.add(category);
                    CategoryIdArrayList.add(id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}