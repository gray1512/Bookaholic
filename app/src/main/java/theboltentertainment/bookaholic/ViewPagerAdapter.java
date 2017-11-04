package theboltentertainment.bookaholic;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static android.support.constraint.ConstraintSet.BOTTOM;


public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return ScanFragment.newInstance();
            case 1: return BookshelfFragment.newInstance();
            case 2: return ShopFragment.newInstance();
            default: return ScanFragment.newInstance();
        }
    }

    public static RecyclerViewAdapter getCurrentAdapter(int item) {
        switch (item) {
            case 0:
                return (RecyclerViewAdapter) ScanFragment.recyclerView.getAdapter();
            case 1:
                return (RecyclerViewAdapter) BookshelfFragment.bookshelfView.getAdapter();
            default: return null;
        }
    }

    public static class ScanFragment extends Fragment {
        private static RecyclerView recyclerView;
        private static RecyclerViewAdapter adapter;
        private static ImageView startTips;
        private static Context c;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.scan_fragment, container, false);

            recyclerView = (RecyclerView) v.findViewById(R.id.quotes_view);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);

            adapter = new RecyclerViewAdapter(getContext(), MainActivity.quoteList);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);

            startTips = v.findViewById(R.id.scan_start_tip);
            if (MainActivity.quoteList.size() != 0) {
                startTips.setVisibility(View.GONE);
            }

            return v;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        public static ScanFragment newInstance() {
            return new ScanFragment();
        }

        public static void notifyDataSetChanged() {
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new RecyclerViewAdapter(c, MainActivity.quoteList);
            recyclerView.setAdapter(adapter);

            if (MainActivity.quoteList.size() != 0) {
                startTips.setVisibility(View.GONE);
            }
        }
    }

    public static class BookshelfFragment extends Fragment {
        private static Context c;

        static RecyclerView bookshelfView;
        static ImageView startTips;
        static int columns;
        public static final String EMPTY_ITEM = "Empty shelf item";
        static final int numberOfRows = 4;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.bookshelf_fragment, container, false);

            View item = inflater.inflate(R.layout.bookshelf_item, null);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int itemWidth = (int) (item.findViewById(R.id.cover).getLayoutParams().width * 1.2);

            columns = (displayMetrics.widthPixels / (itemWidth));
            Log.e("Bookshelf cols", ":" + columns);

            bookshelfView = (RecyclerView) v.findViewById(R.id.bookshelf_view);
            GridLayoutManager layoutManager = new GridLayoutManager(c, columns);
            bookshelfView.setLayoutManager(layoutManager);
            bookshelfView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    outRect.left = -1;
                    outRect.right = -1;
                    outRect.top = 0;
                    outRect.bottom = 0;
                }
            });

            int emptyCells = 0;
            if (MainActivity.bookList.size() % columns != 0) {
                emptyCells = columns - MainActivity.bookList.size() % columns;
            }
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(c, MainActivity.bookList, columns, emptyCells);
            bookshelfView.setAdapter(adapter);

            startTips = v.findViewById(R.id.bookshelf_start_tip);
            if (MainActivity.bookList.size() != 0) {
                startTips.setVisibility(View.GONE);
            }
            return v;
        }

        public static BookshelfFragment newInstance() {
            return new BookshelfFragment();
        }

        public static void notifyDataSetChanged() {
            int emptyCells = 0;
            if (MainActivity.bookList.size() % columns != 0) {
                emptyCells = columns - MainActivity.bookList.size() % columns;
            }
            if (MainActivity.bookList.size() != 0) {
                startTips.setVisibility(View.GONE);
            }
            bookshelfView.setAdapter(new RecyclerViewAdapter(c, MainActivity.bookList, columns, emptyCells));
        }
    }

    public static class ShopFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.shop_fragment, container, false);
            return v;
        }

        public static ShopFragment newInstance() {
            return new ShopFragment();
        }
    }
}
