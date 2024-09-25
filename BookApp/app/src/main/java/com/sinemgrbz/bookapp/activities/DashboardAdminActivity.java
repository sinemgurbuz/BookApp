package com.sinemgrbz.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinemgrbz.bookapp.adapters.AdapterCategory;
import com.sinemgrbz.bookapp.databinding.ActivityDashboardAdminBinding;
import com.sinemgrbz.bookapp.models.ModelCategory;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    //yönetici kontrol paneli
    private ActivityDashboardAdminBinding binding;
    private FirebaseAuth firebaseAuth;

    //arraylist içinde categorileri saklayalım.
    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadCategories();//kategorileri görüntüleyelim

        binding.addpdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DashboardAdminActivity.this, PdfAddActivity.class);
                startActivity(intent);
            }
        });


        //kategori arama işlemi
        //kullanıcının metin girdiğinde adapterCategory adlı bir filtreyi çalıştırır.
        // Bu filtre, metin değişikliklerine göre veri listesini güncellemek için kullanılabilir.
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            //TextWatcher:metin değişikliklerini dinlemek ve buna tepki vermek için kullanılır.
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Bu metot, metin değişikliği öncesinde çağrılır.
                //s: Değişiklik öncesi metin (CharSequence türünde).
                //start: Değişikliğin başladığı indeks.
                //count: Değiştirilen karakter sayısı.
                //after: Değişiklik sonrası metin uzunluğu.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               // Bu metot, metin değişikliği esnasında çağrılır.
                try {
                    adapterCategory.getFilter().filter(s);
                    //ListView veya RecyclerView gibi veri listelerini dinamik olarak filtrelemek için kullanılır.

                }catch (Exception e){
                }
                //adapterCategory, bir adaptör nesnesini temsil eder. Bu adaptör, veri listesini yönetir ve arayüzle veri arasındaki
                // köprüdür.
                //getFilter(), adaptörün içinde bulunan bir metottur. Bu metot, veri listesini filtrelemek için kullanılır.
                //filter(s) ifadesi, kullanıcının girdiği metni temsil eden s parametresi ile veri listesini filtreler.
                //Filtreleme sonucunda, adaptörün içindeki veri listesi güncellenir ve arayüz otomatik olarak yeniden çizilir.
            }
            @Override
            public void afterTextChanged(Editable s) {
                //Bu metot, metin değişikliği sonrasında çağrılır. Parametre:
                //s: Değişiklik sonrası metin (Editable türünde).
            }
        });


        //oturum kapatmayı yönet
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();//kullanıcı oturumdan cıkar.
                checkUser();
            }
        });

        //kategory ekleme ekranını açalım.
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DashboardAdminActivity.this, CategoryAddActivity.class);
                startActivity(intent);
                //finish();
            }
        });
        binding.profilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,ProfileActivity.class));
            }
        });
    }

    private void loadCategories() {
        //Arraylist'i görüntüleyelim
        categoryArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            //metodu, veritabanındaki veri değişikliklerini dinlemek için kullanılır.
            //Bu metot, veritabanındaki veri her değiştiğinde çalışır.
            //Veri eklendiğinde, güncellendiğinde veya silindiğinde bu metot tetiklenir.
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //veri bilgisini eklemeden önce arraylist'i temizleme
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){// ifadesi, veritabanındaki her çocuk düğümü için ds adında bir DataSnapshot oluşturur. Bu ds nesnesi, çocuk düğümünün altındaki verilere erişmenizi sağlar.
                    //firebase Realtime Database’den veri çekmek için kullanılır.
                    // Bu yapı, veritabanındaki her çocuk düğümünü (child node) dolaşarak işlem yapmanızı sağlar.
                    //snapshot bir DataSnapshot nesnesidir ve veritabanından alınan verileri temsil eder.
                    //getChildren() metodu, snapshot içindeki tüm çocuk düğümleri döndürür.

                    //datayı alalım
                    ModelCategory model=ds.getValue(ModelCategory.class);

                    //arraylist'e ekleyelim.
                    categoryArrayList.add(model);

                }
                //adapter'i kuralım.
                adapterCategory=new AdapterCategory(DashboardAdminActivity.this,categoryArrayList);
                //adaptörü Recylerview'e baglayalım
                binding.CategoriesRv.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
                //metodu, veritabanındaki veri değişikliklerini dinlemek için kullanılır.
        //Bu metot, veritabanındaki veri her değiştiğinde çalışır.
        //Veri eklendiğinde, güncellendiğinde veya silindiğinde bu metot tetiklenir.
    }

    private void checkUser() {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            //giriş yapmadınız, ana ekrana gidin
            Intent intent=new Intent(DashboardAdminActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            //giriş yaptı, kullanıcı bilgilerini al
            String email=firebaseUser.getEmail();
            //mail'in toolbar kısmında görünmesi için;
        }
    }
}