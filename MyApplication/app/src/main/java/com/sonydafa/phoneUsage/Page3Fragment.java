package com.sonydafa.phoneUsage;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class Page3Fragment extends Fragment {
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.page3, container, false);
        return view;
    }
    @Override
    public void onStart() {

        Log.i("demo","是第三个视图 notify");
        super.onStart();
    }
}
