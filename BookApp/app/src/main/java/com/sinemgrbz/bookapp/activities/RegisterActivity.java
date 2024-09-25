package com.sinemgrbz.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sinemgrbz.bookapp.databinding.ActivityRegisterBinding;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    //kayıt ol sayfası
    private ActivityRegisterBinding binding;
    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //firebase kimlik doğrulaması
        firebaseAuth=FirebaseAuth.getInstance();


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //geri buttonu
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //kayıt ol buttonu
                validateData();
            }
        });
    }

    private String name="",email="",password="",cPassword="";
    private void validateData() {
        //hesap oluşturmadan önce verilerin doğruluğunu kontrol ettim.
        //veri alalım.
        //trim():baştaki ve sondaki boşlukları kaldırmak için kullanılır.
        name=binding.nameEt.getText().toString().trim();
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString().trim();
        cPassword=binding.cPasswordEt.getText().toString().trim();

        //verileri doğrulayalım
        if(TextUtils.isEmpty(name)){
            //TextUtils.isEmpty(name)-->Eğer name null veya boş bir metin dizisi ise true, aksi takdirde false döner.
            Toast.makeText(this,"Enter your name...",Toast.LENGTH_SHORT).show();
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //email'in eposta formatına uygun olup olmadığını kontrol eder.! işareti oldugunda uygunsa false dönderir.
            Toast.makeText(this,"Invalid email model...",Toast.LENGTH_SHORT).show();
            //kullanıcıya epostanız formata uygun degil dedim.
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,"Enter your password...",Toast.LENGTH_SHORT).show();
            //şifre bos olamaz kontrolunu yaptım.
        } else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this,"Confirm password...",Toast.LENGTH_SHORT).show();
            //şifre bos olamaz kontrolunu yaptım.
        }else if (!password.equals(cPassword)){
            Toast.makeText(this,"Password does not match...",Toast.LENGTH_SHORT).show();
            //şifre ve sifre onayı esleşmiyor ise
        }else {
            createUserAccount();
        }

    }

    private void createUserAccount() {
        //firebase auth da kullanıcı oluşturalım.
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // firebase gerçek zamanlı veritabanını ekleyelim.
                        UpdateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(RegisterActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
        //createUserWithEmailAndPasswprd:Bu yöntem, verilen e-posta adresi ve parola ile yeni bir kullanıcı hesabı oluşturur.
        //eger işlem başarılı olursa addOnSuccessListener içindeki işlem gerçekleştirilir.
        //eger işlem başarısız olursa AddOnFailureListener içindeki işlem gerçekleşir.

    }

    private void UpdateUserInfo() {
        AlertDialog.Builder alertdialog=new AlertDialog.Builder(this);
        //kullanıcı bilgilerini kaydetme
        alertdialog.setMessage("Saving user info...");
        //zaman damgası
        Long timestamp=System.currentTimeMillis();
        //geçerli kullanıcı kimliğini al,kullanıcı kayıtlı olduğundan şimdi alabiliriz
        String uid=firebaseAuth.getUid();
        //veri tabanına veri kurulumu
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("profileImage","");//sonra dönücem
        hashMap.put("userType","user");//olası değerler kullanıcı, yöneticidir
        hashMap.put("timesTamp",timestamp);
        //HashMap:Java’da kullanıcıların anahtar-değer çiftleri şeklinde verileri depolamak için kullandığı bir veri yapısıdır.

        //verileri veritabanına ayarlama
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //veritabanına eklenen veriler
                        alertdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=getIntent();
                            }
                        });
                        //kullanıcı hesabı oluşturulduktan sonra kullanıcının kontrol panelini başlatın
                        Toast.makeText(RegisterActivity.this,"Account created...",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(RegisterActivity.this, DashboardUserActivity.class);
                        startActivity(intent);
                        finish();



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //veri veritabanına eklenemedi
                        alertdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=getIntent();
                                Toast.makeText(RegisterActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

}