package com.sinemgrbz.bookapp.filters;

import android.widget.Filter;

import com.sinemgrbz.bookapp.adapters.AdapterCategory;
import com.sinemgrbz.bookapp.adapters.AdapterPdfAdmin;
import com.sinemgrbz.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    //arraylist'i görüntüliycez.
    ArrayList<ModelPdf>filterList;//filtreleme işleminin uygulanacağı orijinal veri listesini temsil eder.

    AdapterPdfAdmin adapterPdfAdmin;//bu filtrenin uygulanacağı AdapterCategory adlı bir adaptörü temsil eder.

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        //filtreleme işlemini gerçekleştirir.
        // Parametre olarak kullanıcının girdiğini temsil eden constraint alır.

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
        adapterPdfAdmin.pdfArrayList=(ArrayList<ModelPdf>)results.values;
        //results.values:filtrelenmiş veri listesini içerir.

        adapterPdfAdmin.notifyDataSetChanged();
        //veri değişikliğini bildirerek arayüzü günceller.
    }
}
