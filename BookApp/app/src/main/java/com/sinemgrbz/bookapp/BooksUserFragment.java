package com.sinemgrbz.bookapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinemgrbz.bookapp.adapters.AdapterPdfUser;
import com.sinemgrbz.bookapp.databinding.FragmentBooksUserBinding;
import com.sinemgrbz.bookapp.models.ModelPdf;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BooksUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BooksUserFragment extends Fragment {

    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<ModelPdf>pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

    private FragmentBooksUserBinding binding;


    public BooksUserFragment() {
        // Required empty public constructor
    }

    public static BooksUserFragment newInstance(String categoryId, String category,String uid) {
        BooksUserFragment fragment = new BooksUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId",categoryId);
        args.putString("category", category);
        args.putString("uid",uid);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid=getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentBooksUserBinding.inflate(LayoutInflater.from(getContext()),container,false);
        if(category.equals("Tüm Kitaplar")){
            loadAllBook();
            
        } else if (category.equals("En çok Görüntülenen")) {
            loadMostViewedDownLoadedBooks("viewsCount");
            
        }else if (category.equals("En çok indirilenler")){
            loadMostViewedDownLoadedBooks("dowloadsCount");
        }else {
            loadCategorizedBooks();
        }
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterPdfUser.getFilter().filter(s);

                }catch (Exception e){
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return binding.getRoot();

    }

    private void loadAllBook() {
        pdfArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPdf model=ds.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                adapterPdfUser=new AdapterPdfUser(getContext(),pdfArrayList);
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadMostViewedDownLoadedBooks(String orderBy) {
        pdfArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPdf model=ds.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                adapterPdfUser=new AdapterPdfUser(getContext(),pdfArrayList);
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadCategorizedBooks() {
        pdfArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ModelPdf model=ds.getValue(ModelPdf.class);
                            pdfArrayList.add(model);
                        }
                        adapterPdfUser=new AdapterPdfUser(getContext(),pdfArrayList);
                        binding.booksRv.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}