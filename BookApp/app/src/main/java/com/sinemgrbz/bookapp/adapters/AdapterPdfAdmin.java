package com.sinemgrbz.bookapp.adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.sinemgrbz.bookapp.MyApplication;
import com.sinemgrbz.bookapp.activities.PdfDetailActivity;
import com.sinemgrbz.bookapp.activities.PdfEditActivity;
import com.sinemgrbz.bookapp.databinding.RowPdfAdminBinding;
import com.sinemgrbz.bookapp.filters.FilterPdfAdmin;
import com.sinemgrbz.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    //row_pdf_admin için bir tutucu sınıf ekleyelim
    private Context context;
    public ArrayList<ModelPdf>pdfArrayList, filterlist;
    private RowPdfAdminBinding binding;
    private static final String TAG="PDF_ADAPTER_TAG";
    private FilterPdfAdmin filter;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterlist=pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {

        ModelPdf model=pdfArrayList.get(position);
        String pdfId= model.getId();
        String categoryId=model.getCategoryId();
        String title=model.getTitle();
        String description=model.getDescription();
        String pdfUrl=model.getUrl();
        long timestamp=model.getTimestamp();

        String formattedDate= MyApplication.formatTimestamp(timestamp);
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );

        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );

        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );

        holder.morebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreOptionsDialog(model,holder);
            }
        });
        //kitapların açıklamasının daha ayrıntılı görünmesi için
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void MoreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId= model.getId();
        String bookurl= model.getUrl();
        String bookTitle=model.getTitle();

        String[] options={"Düzenle","Sil"};
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Seçenekler")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            //düzenleme buttonuna tıklanınca ne olsun?
                            Intent intent=new Intent(context,PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);
                        }
                        else if(which==1){
                            //silme butonuna basınca ne olsun?
                            MyApplication.DeleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookurl,
                                    ""+bookTitle
                            );
                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        //Android uygulamalarında bir RecyclerView içindeki öğe sayısını belirlemek için kullanılır
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterPdfAdmin(filterlist,this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        ImageButton morebtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView=binding.pdfView;
            progressBar=binding.progessbar;
            titleTv=binding.titleTv;
            descriptionTv=binding.descriptionTv;
            categoryTv=binding.categoryTv;
            sizeTv=binding.sizeTv;
            dateTv=binding.dateTv;
            morebtn=binding.morebtn;
        }
    }
    
}
