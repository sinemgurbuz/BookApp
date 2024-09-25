package com.sinemgrbz.bookapp.filters;

import android.widget.Filter;

import com.sinemgrbz.bookapp.adapters.AdapterCategory;
import com.sinemgrbz.bookapp.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    //arraylist'i görüntüliycez.
    ArrayList<ModelCategory>filterList;//filtreleme işleminin uygulanacağı orijinal veri listesini temsil eder.

    AdapterCategory adapterCategory;//bu filtrenin uygulanacağı AdapterCategory adlı bir adaptörü temsil eder.

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
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
            ArrayList<ModelCategory> filteredModels =new ArrayList<>();


            for(int i=0;i<filterList.size();i++){
                //validate/dogrulama
                if(filterList.get(i).getCategory().toUpperCase().contains(constraint)){
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
        adapterCategory.categoryArrayList=(ArrayList<ModelCategory>)results.values;
        //results.values:filtrelenmiş veri listesini içerir.

        adapterCategory.notifyDataSetChanged();
        //veri değişikliğini bildirerek arayüzü günceller.
    }
}
