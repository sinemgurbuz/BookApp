package com.sinemgrbz.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinemgrbz.bookapp.MyApplication;
import com.sinemgrbz.bookapp.activities.PdfDetailActivity;
import com.sinemgrbz.bookapp.databinding.RowPdfFavoriteBinding;
import com.sinemgrbz.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {
    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RowPdfFavoriteBinding binding;

    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowPdfFavoriteBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        ModelPdf model=pdfArrayList.get(position);
        loadBookDetails(model,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",model.getId());
                context.startActivity(intent);
            }
        });
        holder.RemoveFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context,model.getId());
            }
        });

    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavorite holder) {
        String bookId= model.getId();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bookTitle=""+snapshot.child("title").getValue();
                        String description=""+snapshot.child("description").getValue();
                        String categoryId=""+snapshot.child("categoryId").getValue();
                        String bookUrl=""+snapshot.child("url").getValue();
                        String timesTamp=""+snapshot.child("timestamp").getValue();
                        String uid=""+snapshot.child("uid").getValue();
                        String viewsCount=""+snapshot.child("viewsCount").getValue();

                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        model.setTimestamp(Long.parseLong(timesTamp));
                        model.setCategoryId(categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);

                        String date= MyApplication.formatTimestamp(Long.parseLong(timesTamp));

                        MyApplication.loadCategory(categoryId,holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle,holder.pdfView,holder.progressBar,null);
                        MyApplication.loadPdfSize(""+bookUrl,""+bookTitle,holder.sizeTv);

                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.DateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavorite extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,DateTv;
        ImageButton RemoveFavBtn;

        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);
            pdfView=binding.pdfView;
            progressBar=binding.progessbar;
            titleTv=binding.titleTV;
            RemoveFavBtn =binding.removeFavBtn;
            descriptionTv=binding.descriptionTv;
            categoryTv=binding.categoryTv;
            sizeTv=binding.sizeTv;
            DateTv=binding.dateTv;


        }
    }
}
