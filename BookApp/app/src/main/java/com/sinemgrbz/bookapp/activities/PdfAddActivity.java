package com.sinemgrbz.bookapp.activities;

import static android.content.Intent.ACTION_GET_CONTENT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sinemgrbz.bookapp.databinding.ActivityPdfAddBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private ArrayList<String> categoryTitleArrayList,categoryIdArraylist;

    //secilen pdf'in urisi
    private Uri pdfUri = null;

    private static final int PDF_PICK_CODE = 1000;

    //hata ayıklama etiketi
    private static final String TAG = "AD_PDF_TAG";
    private ActivityPdfAddBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        //ımage buttona basılınca pdf ekleme
        binding.attackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PdfPickIntent();
            }
        });
        //kategori seç
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryPickDialog();
            }
        });
        //tıklanınca pdfi kaydetme
        binding.submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }
    private String title="", description="";

    private void validateData() {

        Log.d(TAG, "validateData: veriler dogrulanıyor...");

        title=binding.titleEt.getText().toString().trim();
        description=binding.descriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Başlık boş geçilemez...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Açıklama boş geçilemez...", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(SelectedCategoryTitle)){
            Toast.makeText(this, "kategori boş geçilemez...", Toast.LENGTH_SHORT).show();
        } else if (pdfUri==null) {
            Toast.makeText(this, "pdf yüklemelisin", Toast.LENGTH_SHORT).show();
        }
        else {
            ///tüm veriler doğrulandı artık pdf yükleyebilir kullanıcı
            UploadPdfToStorage();

        }
    }

    private void UploadPdfToStorage() {
        Log.d(TAG, "UploadPdfToStorage: veritabanına ekle...");

        long timestamp=System.currentTimeMillis();

        String filePathAndName="Books/" + timestamp;

        StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: pdf yükleniyor...");
                        Log.d(TAG, "onSuccess: pdf url'si alma...");

                        //pdf url'sini al
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl=""+uriTask.getResult();
                        
                        //firebase veritabanına ekleme
                        uploadPdfInfoTodb(uploadedPdfUrl,timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: pdf yüklenmedi..."+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "pdf yüklenemedi..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfoTodb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG, "uploadPdfInfoTodb: PDF'i veritabanına ekle...");

        String uid=firebaseAuth.getUid();

        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+SelectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp",timestamp);
        hashMap.put("viewsCount",0);
        hashMap.put("dowloadsCount",0);

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Başarı ile yüklendi...");
                        Toast.makeText(PdfAddActivity.this, "Başarı ile yüklendi...", Toast.LENGTH_SHORT).show();
                        binding.titleEt.setText("");
                        binding.descriptionEt.setText("");
                        binding.categoryTv.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d(TAG, "onFailure: kitap eklenemedi...."+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "kitap eklenemedi...."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: pdf kategorileri yükleniyor...");

        categoryTitleArrayList =new ArrayList<>();
        categoryIdArraylist=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            // veritabanındaki verileri tek bir kez okumak için bir değer dinleyici (value event listener) ekler.
            // Bu dinleyici, veritabanındaki verilerde herhangi bir değişiklik olduğunda tetiklenir.
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //veritabanındaki veriler değiştiğinde çağrılır.
                categoryTitleArrayList.clear();//data eklemeden önce temizliyoruz.
                categoryIdArraylist.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String categoryId =""+ds.child("id").getValue();
                    String categoryTitle=""+ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArraylist.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // veritabanı işlemi sırasında bir hata oluştuğunda çağrılır.
            }
        });
    }

    private String SelectedCategoryId, SelectedCategoryTitle;
    private void CategoryPickDialog() {
        //kategori ekleme
        Log.d(TAG, "CategoryPickDialog: kategori seçme kutusu gösteriliyor...");
        //arraylistten kategori listesini al
        String[] categoriesarray=new String[categoryTitleArrayList.size()];
        for(int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesarray[i]= categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Kategori Seç")
                .setItems(categoriesarray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SelectedCategoryTitle=categoryTitleArrayList.get(which);
                        SelectedCategoryId=categoryIdArraylist.get(which);
                        binding.categoryTv.setText(SelectedCategoryTitle);
                        //kategoriyi metin görünümüne ayarla

                        Log.d(TAG, "onClick: Kategori sec: "+SelectedCategoryId+""+SelectedCategoryTitle);
                    }
                }).show();
    }

    private void PdfPickIntent() {
        Log.d(TAG, "PdfPickIntent: Starting pdf pick intent");
        Intent intent = new Intent();
        //bir uygulama içinde kullanıcıya PDF dosyası seçme seçeneği sunmak
        intent.setType("application/pdf");
        intent.setAction(ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Pdf"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // kullanıcı bir dosya seçtiyse, bu blok çalışır.
            if (requestCode == PDF_PICK_CODE) {
                //kullanıcının seçtiği dosya bir PDF ise, bu blok çalışır.
                Log.d(TAG, "onActivityResult: PDF Picked...");
                //kullanıcının seçtiği dosyanın URI’sini alır ve pdfUri adlı bir değişkene atar.
                // Bu URI, seçilen dosyanın konumunu temsil eder.
                pdfUri = data.getData();
                Log.d(TAG, "onActivityResult: URI:" + pdfUri);
            }

        } else {
            Log.d(TAG, "onActivityResult: CANCELLED PICKING PDF..");
            Toast.makeText(this, "CANCELLED PICKING PDF", Toast.LENGTH_SHORT).show();
        }

    }
}