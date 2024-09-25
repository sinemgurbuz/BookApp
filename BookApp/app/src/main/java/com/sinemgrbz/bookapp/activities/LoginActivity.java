package com.sinemgrbz.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinemgrbz.bookapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    //firebase auth
    private FirebaseAuth firebaseAuth;

    //alertdialog şifreyi yanlıs girince kapanmıyor hata mesajı vermiyor.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //firebase kimlik doğrulaması
        firebaseAuth = FirebaseAuth.getInstance();

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //giriş buttonu
                validateData();
            }
        });
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        binding.forgotTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });
    }

    private String email = "", password = "";

    private void validateData() {
        //giriş yapmadan önce verilerin doğruluğunu kontrol ettim.
        //veri alalım.
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //verileri doğrulayalım
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //email'in eposta formatına uygun olup olmadığını kontrol eder.! işareti oldugunda uygunsa false dönderir.
            Toast.makeText(this, "Invalid email model...", Toast.LENGTH_SHORT).show();
            //kullanıcıya epostanız formata uygun degil dedim.
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter your password...", Toast.LENGTH_SHORT).show();
            //şifre bos olamaz kontrolunu yaptım.
        } else{
            //veriler doğrulandı giriş yapılabilir, aşağıdaki fonksiyon çağrılabilir.
            loginuser();
        }
    }

    private void loginuser() {
        //kullanıcı girişi
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //giriş başarılı ise ama kullanıcı mı actı admin mi onun kontrolü yapılmalı
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //giriş başarısız ise
                    }
                });
    }
    private void checkUser() {
        //gerçek zamanlı veritabanından kullanıcının kullanıcı mı yoksa yönetici mi olduğunu kontrol edelim.
        //mevcut kullanıcıyı alalım
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        //kontrol kısmı
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    //addListenerForSingleValueEvent:belirtilen yoldaki verilerdeki değişiklikleri dinlemek için kullanılır ve
                    // bu listener sadece bir kez tetiklenir.
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //kullanıcı türünü alalım.
                        String usertype= ""+snapshot.child("userType").getValue();
                        //Firebase Realtime Database’den veri çekme işlemi gerçekleştiren bir kod parçasıdır.
                        //kontrol işlemini yapalım
                        System.out.println(usertype);
                        if(usertype.equals("user")){
                            //equals() metodu, nesnelerin içeriklerini karşılaştırmak için kullanılır.
                            //eger kullanıcı ise kullanıcı kontrol paneli açılsın
                            Intent intent=new Intent(LoginActivity.this, DashboardUserActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (usertype.equals("admin")) {
                            //eger admin ise admin kontrol paneli açılsın.
                            Intent intent=new Intent(LoginActivity.this, DashboardAdminActivity.class);
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