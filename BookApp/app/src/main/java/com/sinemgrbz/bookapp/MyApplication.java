package com.sinemgrbz.bookapp;

import static com.sinemgrbz.bookapp.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.sinemgrbz.bookapp.adapters.AdapterPdfAdmin;
import com.sinemgrbz.bookapp.models.ModelPdf;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
    //Zaman damgasını uygun tarih formatına dönüştürmek için statik bir yöntem oluşturdum, böylece onu projenin her yerde kullanabiliriz,
    // tekrar yazmaya gerek yok
    public static final String formatTimestamp(long timestamp){
        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        //Calendar nesnesi oluşturur ve bu nesneyi geçerli sistem saati ve tarihine ayarlar
        cal.setTimeInMillis(timestamp);
        String date= DateFormat.format("dd/MM/yyyy",cal).toString();
        return date;
    }
    public static void DeleteBook(Context context,String bookId,String bookurl,String bookTitle) {

        String TAG="DELETE_BOOK_TAG";

        StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(bookurl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: veritabanından sil");
                        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Kitap Başarı ile Silindi...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(context,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "silinemedi...", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }
    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG="PDF_SİZE_TAG";
        //PDF dosyasının boyutunu almak için kullanılır
        // Bu metod, verilen ModelPdf nesnesinin içindeki URL’yi kullanarak Firebase Storage’dan PDF dosyasının metadata bilgilerini alır.
        //Alınan metadata bilgileri içinde dosyanın boyutu (byte cinsinden) bulunur.

        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //byte cinsinden boyutu al
                        double bytes=storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle+" "+bytes);
                        double kb=bytes/1024;
                        double mb=kb/1024;

                        if (mb>=1){
                            sizeTv.setText(String.format("%.2f",mb)+" MB");
                        } else if (kb>=1) {
                          sizeTv.setText(String.format("%.2f",kb)+" KB");
                        }
                        else {
                            sizeTv.setText(String.format("%.2f",bytes)+" Bytes");
                        }
                        //Bu boyut bilgisini KB veya MB olarak dönüştürerek, holder.sizeTv adlı bir metin görünümüne yazdırır.

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }
    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar,TextView pagesTv) {
        String TAG="PDF_LOAD_SINGLE_TAG";
        //Bu metod, verilen ModelPdf nesnesinin içindeki URL’yi kullanarak Firebase Storage’dan PDF dosyasını alır.
        //Alınan PDF dosyası, byte dizisi olarak döndürülür.
        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+pdfTitle+ "Dosya başarı ile alındı.");

                        //url'den pdf yükle
                        pdfView.fromBytes(bytes)
                                //PDF görüntüleyiciye bu baytları yükleriz.
                                .pages(0)//yanlızca ilk sayfayı göstersin.
                                .spacing(0)//sayfalar arasındaki boşluğu ayarlarız
                                .swipeHorizontal(false) //yatay kaydırmayı devre dışı bırakırız.
                                .enableSwipe(false) // kaydırmayı devre dışı bırakırız.
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf yüklendi");

                                        if(pagesTv!=null){
                                            pagesTv.setText(""+nbPages);
                                        }
                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure:url'den dosya alınamadı."+e.getMessage());
                    }
                });
    }
    public static void loadCategory(String categoryId,TextView categoryTv) {
        //kategoriId kullanarak kategorileri alalım.
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category=""+snapshot.child("category").getValue();

                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }




    public static void AddToFavorite(Context context,String bookid){
        //yalnızca kullanıcı oturum açmışsa ekleyebiliriz favoriye
        //kullanıcının oturum açıp açmadığını kontrol edicez.
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Toast.makeText(context, "Oturum açmanız gerekli...", Toast.LENGTH_SHORT).show();
        }
        else {
            long timestamp= System.currentTimeMillis();
            HashMap<String, Object>hashMap=new HashMap<>();
            hashMap.put("bookId",""+bookid);
            hashMap.put("timestamp",""+timestamp);

            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookid)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Favorilere eklendi...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Ekleme işlemi başarısız..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    public static void removeFromFavorite(Context context,String bookid){
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Toast.makeText(context, "Oturum açmanız gerekli...", Toast.LENGTH_SHORT).show();
        }
        else {


            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookid)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Favorilerden kaldırıldı...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Kaldırma işlemi başarısız..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
