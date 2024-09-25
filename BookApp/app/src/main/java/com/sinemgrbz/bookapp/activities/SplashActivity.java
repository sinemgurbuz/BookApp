package com.sinemgrbz.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinemgrbz.bookapp.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        //2 saniye sonra mainactivity açılacak.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkUser();
                }
            },2000);//2 saniye demek
    }

    private void checkUser() {
        //mevcut kullanıcıyı alalım,giriş yaptı ise
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            Intent intent=new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            //kullanıcı giriş yaptı kullanıcı türünü kontrol et
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        //addListenerForSingleValueEvent:belirtilen yoldaki verilerdeki değişiklikleri dinlemek için kullanılır ve
                        // bu listener sadece bir kez tetiklenir.
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            //kullanıcı türünü alalım.
                            String usertype=""+snapshot.child("userType").getValue();
                            //Firebase Realtime Database’den veri çekme işlemi gerçekleştiren bir kod parçasıdır.
                            //kontrol işlemini yapalım
                            if(usertype.equals("user")){
                                //equals() metodu, nesnelerin içeriklerini karşılaştırmak için kullanılır.
                                //eger kullanıcı ise kullanıcı kontrol paneli açılsın
                                Intent intent=new Intent(SplashActivity.this, DashboardUserActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (usertype.equals("admin")) {
                                //eger admin ise admin kontrol paneli açılsın.
                                Intent intent=new Intent(SplashActivity.this, DashboardAdminActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
        }

    }
}