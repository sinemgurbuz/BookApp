package com.sinemgrbz.bookapp.filters;

import android.widget.Filter;

import com.sinemgrbz.bookapp.adapters.AdapterPdfUser;
import com.sinemgrbz.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {
    ArrayList<ModelPdf> filterList;
    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        if(constraint!= null && constraint.length()>0){
            //Eğer constraint değeri null değilse ve uzunluğu 0’dan büyükse, filtreleme işlemi yapılır.

            //Büyük/küçük harf duyarlılığını önler tüm harfleri büyük yapar.
            constraint=constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels =new ArrayList<>();


            for(int i=0;i<filterList.size();i++){
                //validate/dogrulama
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                    // Veri listesindeki her öğe, kullanıcının girdisi ile karşılaştırılır.
                    // Eğer öğe metin içeriyorsa, filtrelenmiş modellere eklenir.
                }
            }

            //Sonuçlar FilterResults nesnesine atanır.
            results.count=filteredModels.size();
            results.values=filteredModels;
        }
        else{
            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //filtre sonuçlarını kullanıcı arayüzüne uygular.
        adapterPdfUser.pdfArrayList=(ArrayList<ModelPdf>)results.values;
        //results.values:filtrelenmiş veri listesini içerir.

        adapterPdfUser.notifyDataSetChanged();
        //veri değişikliğini bildirerek arayüzü günceller.

    }
}
