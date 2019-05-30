package fr.apocalypse.cluedo;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

class MyAdapter implements SpinnerAdapter {
    ArrayList<TextView> textViews = new ArrayList<>();
    ArrayList<DataSetObserver> observers = new ArrayList<>();
    Context baseContext;

    public MyAdapter(Context baseContext) {
        super();
        this.baseContext = baseContext;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return textViews.get(position);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        observers.remove(observer);
    }

    @Override
    public int getCount() {
        return textViews.size();
    }

    @Override
    public String getItem(int position) {
        return textViews.get(position).getText().toString();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return textViews.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return textViews.size()==0;
    }

    public void addItem(String item){
        TextView t = new TextView(baseContext);
        t.setText(item);
        textViews.add(t);

        Iterator<DataSetObserver> it = observers.iterator();
        while(it.hasNext())
        {
            it.next().onChanged();
        }
    }
    public void removeAll()
    {
        textViews.clear();
        Iterator<DataSetObserver> it = observers.iterator();
        while(it.hasNext())
        {
            it.next().onChanged();
        }
    }
}
