package theboltentertainment.bookaholic;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {
    private Context ctx;

    private int _activity;
    public final int MAIN_ACTIVITY_RECENTLY = 0;
    public final int MAIN_ACTIVITY_BOOKSHELF = 1;
    public final int GALERY_ACTIVITY = 2;
    public final int BOOK_DETAIL_ACTIVITY = 3;

    // Galery Activity
    private View parentView;
    private static String imgPath;
    private int viewWidth, viewHeight;
    private File parentFile;
    private ArrayList<File> picDirs = new ArrayList<>();
    private int chose = -1;

    // Main Activity Recently
    private ArrayList<QuoteClass> quoteList = new ArrayList<>();
    private QuoteFilter filter;
    private String[] packageShareNames = {"com.facebook.katana", "com.instagram.android", "com.twitter.android", "com.tumblr", "OTHER"};

    // Main Activity Bookshelf
    private ArrayList<BookClass> bookList = new ArrayList<>();
    private int numOfCols = 0;
    private int emptyItems = 0;

    // Book Detail Activity
    private ArrayList<QuoteClass> bookQuotesList = new ArrayList<>();

    private String currentDate;
    private int currentIndex;


    private Handler handler = new Handler();

    // CONSTRUCTOR
    RecyclerViewAdapter(Context c, File pic_dir) {
        _activity = GALERY_ACTIVITY;

        parentFile = pic_dir;
        File[] picFile = pic_dir.listFiles();
        Collections.sort(Arrays.asList(picFile), new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && !o2.isDirectory()) {
                    return -1; // negative val means 1st arg is less than the 2nd
                }

                Date lastModDate1 = new Date(o1.lastModified());
                Date lastModDate2 = new Date(o2.lastModified());
                return - lastModDate1.compareTo(lastModDate2);
            }
        });

        picDirs = new ArrayList<>(Arrays.asList(picFile));
        ctx = c;
    }

    RecyclerViewAdapter(Context c, ArrayList<QuoteClass> quote_list) {
        _activity = MAIN_ACTIVITY_RECENTLY;
        quoteList = quote_list;
        ctx = c;
    }

    RecyclerViewAdapter(Context c, ArrayList<BookClass> book_list, int cols, int emptys) {
        _activity = MAIN_ACTIVITY_BOOKSHELF;
        ctx = c;
        numOfCols = cols;
        emptyItems = emptys;
        bookList = book_list;
    }

    RecyclerViewAdapter(ArrayList<QuoteClass> quote_list, Context c) {
        _activity = BOOK_DETAIL_ACTIVITY;
        ctx = c;
        bookQuotesList = quote_list;
    }

    ArrayList<QuoteClass> getDataset() {
        return quoteList;
    }

    ArrayList<BookClass> getBookDataset() {
        return bookList;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new QuoteFilter();
        }
        return filter;
    }

    void resetFilter() {
        filter = null;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parentView = (View) recyclerView.getParent();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (_activity) {
            case MAIN_ACTIVITY_RECENTLY:
                View v0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.quote_item, parent, false);
                return new ViewHolder(v0);

            case GALERY_ACTIVITY:
                View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.pic, parent, false);
                viewWidth = v1.findViewById(R.id.pic).getLayoutParams().width;
                viewHeight = v1.findViewById(R.id.pic).getLayoutParams().height;

                return new ViewHolder(v1);

            case MAIN_ACTIVITY_BOOKSHELF:
                View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookshelf_item, parent, false);
                viewWidth = v2.findViewById(R.id.cover).getLayoutParams().width;
                viewHeight = v2.findViewById(R.id.cover).getLayoutParams().height;
                return new ViewHolder(v2);

            case BOOK_DETAIL_ACTIVITY:
                View v3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_quote_item, parent, false);
                return new ViewHolder(v3);
        }
        return null;
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.clearAnimation();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        switch (_activity) {
            case MAIN_ACTIVITY_RECENTLY: {
                final QuoteClass quote = quoteList.get(position);

                holder.quote.setText(quote.get_quote());
                holder.author.setText(quote.get_author());
                holder.title.setText(quote.get_title());

                final View[] views = {holder.facebook, holder.instagram, holder.twitter, holder.tumblr, holder.other};
                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareBtnClicked(views, holder.share, quote);
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        areYouSure(v, quote);
                    }
                });

                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Todo: To EditActivity
                        Intent intent = new Intent(ctx, EditActivity.class);
                        intent.putExtra(MainActivity.ACTIVITY, MainActivity.MAIN_ACTIVITY);
                        intent.putExtra(MainActivity.QUOTE_EDIT, quote);
                        ctx.startActivity(intent);
                    }
                });

                Animation slideInAnim = AnimationUtils.loadAnimation(ctx, android.R.anim.slide_in_left);
                holder.itemView.startAnimation(slideInAnim);
                break;
            }

            case GALERY_ACTIVITY: {
                final File file = picDirs.get(position);

                if (file.isDirectory()) {
                    holder.name.setText(file.getName());
                    holder.name.setVisibility(View.VISIBLE);

                    holder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            picDirs = new ArrayList<>(Arrays.asList(file.listFiles()));
                            parentFile = file;
                            if (chose != -1) {
                                ((RecyclerView) parentView.findViewById(R.id.galery_view)).getChildAt(chose)
                                        .findViewById(R.id.stt_checked).setVisibility(View.GONE);
                            }
                            GaleryActivity.actionBarDone.setVisibility(View.INVISIBLE);
                            GaleryActivity.actionBarTitle.setText(file.getName());
                            notifyDataSetChanged();
                        }
                    });
                    holder.img.setImageResource(R.drawable.album);

                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.with(ctx).load(file).resize(viewWidth, viewHeight).centerInside().into(holder.img);
                        }
                    });

                    holder.name.setVisibility(View.GONE);

                    holder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setImgPath(file.getAbsolutePath());

                            if (chose != -1) {
                                ((RecyclerView) parentView.findViewById(R.id.galery_view)).getChildAt(chose)
                                        .findViewById(R.id.stt_checked).setVisibility(View.GONE);
                            }
                            chose = position;
                            holder.stt.setVisibility(View.VISIBLE);
                            GaleryActivity.actionBarDone.setVisibility(View.VISIBLE);
                        }
                    });
                }
                break;
            }

            case MAIN_ACTIVITY_BOOKSHELF: {
                if (position < bookList.size()) {
                    final BookClass book = bookList.get(position);
                    String coverPath = book.get_cover();
                    holder.coverTitle.setVisibility(View.VISIBLE);
                    holder.cover.setVisibility(View.VISIBLE);

                    if (new File(coverPath).exists()) {
                        holder.coverTitle.setVisibility(View.GONE);
                        Picasso.with(ctx).load(new File(coverPath)).resize(viewWidth, viewHeight).centerInside().into(holder.cover);

                    } else {
                        if (coverPath.equals(QuoteClass.DEFAULT_COVER)) {
                            holder.coverTitle.setText("Unknown");
                        } else holder.coverTitle.setText(coverPath);
                        holder.cover.setImageResource(R.drawable.book_cover);
                    }
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ctx, BookDetailActivity.class);
                            intent.putExtra(BookDetailActivity.BOOK, book);
                            ctx.startActivity(intent);
                        }
                    });
                } else {
                    holder.cover.setVisibility(View.INVISIBLE);
                    holder.coverTitle.setVisibility(View.INVISIBLE);
                }

                if (position % numOfCols == 0) {
                    holder.shelf.setImageResource(R.drawable.bookshelf_item_startmdpi);
                } else if (position % numOfCols == numOfCols - 1) {
                    holder.shelf.setImageResource(R.drawable.bookshelf_item_endmdpi);
                } else {
                    holder.shelf.setImageResource(R.drawable.bookshelf_item_middlemdpi);
                }

                // TODO: start BookDetailActivity
                break;
            }

            case BOOK_DETAIL_ACTIVITY: {
                final QuoteClass quote = bookQuotesList.get(position);

                if (!quote.get_date().equals(currentDate)) {
                    currentDate = quote.get_date();
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(currentDate);
                }

                holder.quote.setText(quote.get_quote());
                holder.author.setText(quote.get_author());
                holder.title.setText(quote.get_title());

                final View[] views = {holder.facebook, holder.instagram, holder.twitter, holder.tumblr, holder.other};
                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareBtnClicked(views, holder.share, quote);
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        areYouSure(v, quote);
                    }
                });

                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Todo: To EditActivity
                        Intent intent = new Intent(ctx, EditActivity.class);
                        intent.putExtra(MainActivity.ACTIVITY, BookDetailActivity.BOOK_DETAIL_ACTIVITY);
                        intent.putExtra(MainActivity.QUOTE_EDIT, quote);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    }
                });

                Animation slideInAnim = AnimationUtils.loadAnimation(ctx, android.R.anim.slide_in_left);
                holder.itemView.startAnimation(slideInAnim);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        switch (_activity) {
            case MAIN_ACTIVITY_RECENTLY:
                if (quoteList != null) return quoteList.size();
                break;

            case MAIN_ACTIVITY_BOOKSHELF:
                if (bookList != null) return bookList.size() + emptyItems;
                break;

            case GALERY_ACTIVITY:
                return picDirs.size();

            case BOOK_DETAIL_ACTIVITY:
                return bookQuotesList.size();
        }
        return 0;
    }

    private void areYouSure(View v, final QuoteClass quote) {
        try {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup, null);

            final PopupWindow pw = new PopupWindow(layout, 300, 200, true);
            pw.setOutsideTouchable(true);
            pw.setFocusable(true);
            pw.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.popup_frame));
            pw.showAsDropDown(v, -250, 8);

            Button yesBtn = layout.findViewById(R.id.yes_btn);
            Button noBtn = layout.findViewById(R.id.no_btn);
            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quoteList.remove(quote);
                    SQLDatabase db = new SQLDatabase(ctx);
                    db.deleteQuote(quote);
                    db.close();

                    if (_activity == BOOK_DETAIL_ACTIVITY && quoteList.size() == 0) {
                        ctx.startActivity(new Intent(ctx, MainActivity.class).putExtra(MainActivity.ACTIVITY, BookDetailActivity.BOOK_DETAIL_ACTIVITY)
                                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        notifyDataSetChanged();
                    }
                    pw.dismiss();
                }
            });
            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shareBtnClicked(final View[] views, final ImageView btn, final QuoteClass quote) {
        btn.setImageResource(R.drawable.cancel);

        // TODO: animate visible
        for (int i = 0; i < views.length; i++) {
            AnimationSet set = new AnimationSet(true);
            Animation trAnimation = new TranslateAnimation(60*(i+1), 0, 0, 0);
            trAnimation.setDuration(800);

            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(800);

            set.addAnimation(trAnimation);
            set.addAnimation(anim);

            views[i].startAnimation(set);
            views[i].setVisibility(View.VISIBLE);

            final int chooseToShare = i;
            if (packageShareNames[chooseToShare].equals("OTHER")) {
                views[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, quote.getShareText());
                        sendIntent.setType("text/plain");
                        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(Intent.createChooser(sendIntent, "Share to..."));
                    }
                });

            } else {
                views[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.setPackage(packageShareNames[chooseToShare]);

                        intent.putExtra(Intent.EXTRA_TEXT, quote.getShareText());
                        ctx.startActivity(intent);
                    }
                });
            }
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBtnClicked(views, btn, quote);
            }
        });

    }

    private void cancelBtnClicked(final View[] views, final ImageView btn, final QuoteClass quote) {
        // TODO: animate invisible
        for (int i = 0; i < views.length; i++) {
            AnimationSet set = new AnimationSet(true);
            Animation trAnimation = new TranslateAnimation(0, 60*(i+1), 0, 0);
            trAnimation.setDuration(800);

            Animation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(800);

            set.addAnimation(trAnimation);
            set.addAnimation(anim);
            views[i].startAnimation(set);

            views[i].setVisibility(View.GONE);
        }

        btn.setImageResource(R.drawable.share_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareBtnClicked(views, btn, quote);
            }
        });
    }

    static String getImgPath() {
        return imgPath;
    }

    private void setImgPath(String imgPath) {
        RecyclerViewAdapter.imgPath = imgPath;
    }

    public File getParentFile() {
        return parentFile;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        // Galery Item ViewHolder
        ImageView img;
        ImageView stt;
        TextView name;

        // Quote Item ViewHolder
        TextView quote;
        TextView author;
        TextView title;
        ImageButton share;
        ImageButton delete;
        ImageButton edit;

        ImageButton facebook;
        ImageButton instagram;
        ImageButton twitter;
        ImageButton tumblr;
        ImageButton other;

        // Bookshelf Item ViewHolder
        ImageView cover;
        ImageView shelf;
        TextView coverTitle;

        // BookDetail Item ViewHolder
        TextView time;

        ViewHolder(View itemView) {
            super(itemView);
            switch (_activity) {
                case GALERY_ACTIVITY: {
                    img = (ImageView) itemView.findViewById(R.id.pic);
                    name = (TextView) itemView.findViewById(R.id.name);
                    stt = (ImageView) itemView.findViewById(R.id.stt_checked);
                    break;
                }

                case MAIN_ACTIVITY_RECENTLY: {
                    quote = (TextView) itemView.findViewById(R.id.display_quote);
                    author = (TextView) itemView.findViewById(R.id.display_author);
                    title = (TextView) itemView.findViewById(R.id.display_title);

                    share = (ImageButton) itemView.findViewById(R.id.share_btn);
                    delete = (ImageButton) itemView.findViewById(R.id.delete_btn);
                    edit = (ImageButton) itemView.findViewById(R.id.edit_btn);

                    facebook = (ImageButton) itemView.findViewById(R.id.facebook_btn);
                    instagram = (ImageButton) itemView.findViewById(R.id.instagram_btn);
                    twitter = (ImageButton) itemView.findViewById(R.id.twitter_btn);
                    tumblr = (ImageButton) itemView.findViewById(R.id.tumblr_btn);
                    other = (ImageButton) itemView.findViewById(R.id.other_btn);
                    break;
                }

                case MAIN_ACTIVITY_BOOKSHELF: {
                    cover = (ImageView) itemView.findViewById(R.id.cover);
                    shelf = (ImageView) itemView.findViewById(R.id.shelf);
                    coverTitle = (TextView) itemView.findViewById(R.id.cover_title);
                    break;
                }

                case BOOK_DETAIL_ACTIVITY: {
                    quote = (TextView) itemView.findViewById(R.id.display_quote);
                    author = (TextView) itemView.findViewById(R.id.display_author);
                    title = (TextView) itemView.findViewById(R.id.display_title);

                    share = (ImageButton) itemView.findViewById(R.id.share_btn);
                    delete = (ImageButton) itemView.findViewById(R.id.delete_btn);
                    edit = (ImageButton) itemView.findViewById(R.id.edit_btn);

                    facebook = (ImageButton) itemView.findViewById(R.id.facebook_btn);
                    instagram = (ImageButton) itemView.findViewById(R.id.instagram_btn);
                    twitter = (ImageButton) itemView.findViewById(R.id.twitter_btn);
                    tumblr = (ImageButton) itemView.findViewById(R.id.tumblr_btn);
                    other = (ImageButton) itemView.findViewById(R.id.other_btn);
                    time = (TextView) itemView.findViewById(R.id.time);
                    break;
                }
            }
        }

        private void clearAnimation()
        {
            itemView.clearAnimation();
        }
    }

    private class QuoteFilter extends Filter {
        private ArrayList<QuoteClass> backupQuoteData = new ArrayList<>();
        private ArrayList<BookClass> backupBookData = new ArrayList<>();

        private QuoteFilter() {
            switch (_activity) {
                case MAIN_ACTIVITY_RECENTLY:
                    backupQuoteData.addAll(quoteList);
                    return;
                case MAIN_ACTIVITY_BOOKSHELF:
                    backupBookData.addAll(bookList);
                    return;
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            switch (_activity) {
                case MAIN_ACTIVITY_RECENTLY:
                    if (constraint != null && constraint.length() > 0) {
                        ArrayList<QuoteClass> filterList = new ArrayList<>();
                        for (int i = 0; i < backupQuoteData.size(); i++) {
                            if ((backupQuoteData.get(i).get_quote().toUpperCase()).contains(constraint.toString().toUpperCase()) ||
                                    (backupQuoteData.get(i).get_title().toUpperCase()).contains(constraint.toString().toUpperCase()) ||
                                    (backupQuoteData.get(i).get_author().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                                filterList.add(backupQuoteData.get(i));
                            }

                        }
                        results.count = filterList.size();
                        results.values = filterList;
                    } else {
                        results.count = backupQuoteData.size();
                        results.values = backupQuoteData;
                    }
                    return results;

                case MAIN_ACTIVITY_BOOKSHELF:
                    if (constraint != null && constraint.length() > 0) {
                        ArrayList<BookClass> filterList = new ArrayList<>();
                        for (int i = 0; i < backupBookData.size(); i++) {
                            if ((backupBookData.get(i).get_title().toUpperCase()).contains(constraint.toString().toUpperCase()) ||
                                    (backupBookData.get(i).get_author().toUpperCase()).contains(constraint.toString().toUpperCase()) ||
                                    (backupBookData.get(i).get_content().toUpperCase()).contains(constraint.toString().toUpperCase()) ||
                                    (backupBookData.get(i).get_type().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                                filterList.add(backupBookData.get(i));
                            }

                        }
                        results.count = filterList.size();
                        results.values = filterList;
                    } else {
                        results.count = backupBookData.size();
                        results.values = backupBookData;
                    }
                    return results;
            }
            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            switch (_activity) {
                case MAIN_ACTIVITY_RECENTLY:
                    quoteList.clear();
                    quoteList.addAll((ArrayList) results.values);
                    notifyDataSetChanged();
                    break;

                case MAIN_ACTIVITY_BOOKSHELF:
                    bookList.clear();
                    bookList.addAll((ArrayList) results.values);

                    emptyItems = 0;
                    if (bookList.size() % numOfCols != 0) {
                        emptyItems = numOfCols - bookList.size() % numOfCols;
                    }
                    notifyDataSetChanged();
                    break;
            }

        }
    }
}
