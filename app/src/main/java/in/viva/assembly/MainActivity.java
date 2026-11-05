package in.viva.assembly;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class MainActivity extends Activity {
    private static final int BG=0xfff7f6f2,WHITE=0xffffffff,PURPLE=0xff5f4c96,INK=0xff243138,MUTED=0xff697479;
    private final ExecutorService worker=Executors.newFixedThreadPool(2);
    private LinearLayout root,body,tabs;
    private Button dateButton,refreshButton;
    private TextView status;
    private AssemblyData data;
    private int tab;
    private boolean fetching=false;
    private List<List<NewsEngine.Item>> candidates=new ArrayList<>();
    private byte[] pendingPdf;
    private String pendingName="",importMonth="";
    private Bitmap logo;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LocalDate selected=DayContent.today();
        if(state!=null)try{selected=LocalDate.parse(state.getString("date",""));tab=state.getInt("tab",0);}catch(Exception ignored){}
        data=AssemblyData.load(this,selected);loadLogo();
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        root.setPadding(dp(16),dp(12),dp(16),dp(8));
        root.setOnApplyWindowInsetsListener((v,insets)->{
            v.setPadding(dp(16),dp(12)+insets.getSystemWindowInsetTop(),dp(16),dp(8)+insets.getSystemWindowInsetBottom());return insets;
        });
        setContentView(root);
        TextView brand=text("VIVA  /  ASSEMBLY COMPANION",13,PURPLE);brand.setLetterSpacing(.1f);root.addView(brand);
        root.addView(text("A thoughtful start to the day",23,INK));
        dateButton=button("",this::pickDate);root.addView(dateButton);
        ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        tabs=new LinearLayout(this);root.addView(tabs);
        String[] names={"News","Assembly","A4 export"};
        for(int i=0;i<3;i++){final int n=i;tabs.addView(button(names[i],()->{tab=n;render();scroll.scrollTo(0,0);}),new LinearLayout.LayoutParams(0,dp(52),1));}
        render();
    }
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("date",data.date.toString());out.putInt("tab",tab);}
    @Override protected void onDestroy(){worker.shutdown();super.onDestroy();}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(4),0,dp(4));return t;}
    private Button button(String label,Runnable action){Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setTextColor(PURPLE);b.setOnClickListener(v->action.run());return b;}
    private LinearLayout card(String heading){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(12),dp(14),dp(12));
        GradientDrawable bg=new GradientDrawable();bg.setColor(WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),0xffe5e1ec);box.setBackground(bg);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);body.addView(box,lp);
        if(!heading.isEmpty())box.addView(text(heading,18,PURPLE));return box;
    }
    private EditText edit(LinearLayout box,String value,String hint,int limit,Consumer<String> save){
        EditText e=new EditText(this);e.setTextSize(16);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setText(value);e.setHint(hint);
        e.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(limit)});e.setMinLines(2);e.setGravity(Gravity.TOP);
        box.addView(e,new LinearLayout.LayoutParams(-1,-2));
        e.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){save.accept(s.toString());data.reviewed=false;data.save(MainActivity.this);}
            public void afterTextChanged(Editable value){}
        });return e;
    }
    private void render(){
        body.removeAllViews();dateButton.setText(data.date.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy",Locale.ENGLISH))+"  ▾");
        for(int i=0;i<3;i++)((Button)tabs.getChildAt(i)).setTextColor(i==tab?PURPLE:MUTED);
        if(tab==0)news();else if(tab==1)assembly();else exportScreen();
    }
    private void pickDate(){
        new DatePickerDialog(this,(picker,y,m,d)->{
            data=AssemblyData.load(this,LocalDate.of(y,m+1,d));candidates=new ArrayList<>();render();
        },data.date.getYear(),data.date.getMonthValue()-1,data.date.getDayOfMonth()).show();
    }
    private void news(){
        LinearLayout top=card("Eight headlines. One clear assembly.");
        top.addView(text("Fetch suggestions, read the sources and edit the wording for your students. Topic filters are not a substitute for teacher review.",14,MUTED));
        refreshButton=button(fetching?"Fetching news & weather…":"Refresh news & weather",this::confirmRefresh);refreshButton.setEnabled(!fetching);top.addView(refreshButton);
        status=text(data.status,13,MUTED);top.addView(status);
        if(!data.fetched.isEmpty())top.addView(text("Last successful news refresh: "+PdfMaker.stamp(data.fetched),12,MUTED));
        if(data.date.isAfter(DayContent.today()))top.addView(text("Future assembly: news can only reflect reports available now. Refresh again nearer the assembly.",13,PURPLE));
        for(int c=0;c<4;c++){
            LinearLayout box=card(NewsEngine.CATEGORIES[c]);
            for(int n=0;n<2;n++){
                final int index=c*2+n,category=c;NewsEngine.Item item=data.news[index];
                box.addView(text("HEADLINE "+(n+1),11,MUTED));
                edit(box,item.title,"No headline selected — fetch or type a verified item.",600,value->data.news[index].title=value);
                box.addView(text(sourceLabel(item),12,MUTED));
                LinearLayout row=new LinearLayout(this);box.addView(row);
                row.addView(button("Source",()->sourceDialog(index)),new LinearLayout.LayoutParams(0,-2,1));
                row.addView(button("Choose",()->chooseCandidate(category,index)),new LinearLayout.LayoutParams(0,-2,1));
            }
        }
    }
    private String sourceLabel(NewsEngine.Item item){
        if(item.title.isEmpty())return "Source not yet selected";
        return (item.source.isEmpty()?"Source not provided":item.source)+" · "+PdfMaker.stamp(item.published);
    }
    private void confirmRefresh(){
        if(fetching)return;
        boolean has=false;for(NewsEngine.Item i:data.news)if(!i.title.isEmpty())has=true;
        if(has)new AlertDialog.Builder(this).setTitle("Refresh this assembly?")
            .setMessage("Available new suggestions will replace unchanged headlines. If a feed fails, existing text is kept. Edits you make while fetching are preserved.")
            .setPositiveButton("Refresh",(d,w)->refreshOnline()).setNegativeButton("Cancel",null).show();
        else refreshOnline();
    }
    private void refreshOnline(){
        fetching=true;AssemblyData before=data.copy();LocalDate selected=before.date;render();
        worker.execute(()->{
            OnlineContent.Result result=OnlineContent.fetch(selected);
            runOnUiThread(()->{
                fetching=false;if(isDestroyed())return;
                if(!data.date.equals(selected)){render();toast("Refresh finished for another date; select it and refresh again.");return;}
                candidates=result.candidates;
                int added=0;Set<String> used=new HashSet<>();
                for(int i=0;i<8;i++)if(!data.news[i].stamp().equals(before.news[i].stamp()))used.add(NewsEngine.canonical(data.news[i].title));
                for(int c=0;c<4;c++){
                    List<NewsEngine.Item> chosen=NewsEngine.choose(new ArrayList<>(result.candidates.get(c)),selected,Instant.now(),used);
                    int next=0;
                    for(int n=0;n<2;n++){
                        int i=c*2+n;
                        if(data.news[i].stamp().equals(before.news[i].stamp())&&next<chosen.size()){data.news[i]=chosen.get(next++).copy();added++;}
                    }
                    if(chosen.size()<2)result.messages.add(NewsEngine.CATEGORIES[c]+": fewer than two fresh suggestions; check remaining slots.");
                }
                if(!result.weather.isEmpty()&&data.weather.equals(before.weather)){
                    data.weather=result.weather;data.weatherSource="Open-Meteo";data.weatherFetched=result.weatherFetched;
                }
                if(added>0)data.fetched=Instant.now().toString();
                data.status=added+" news slots updated. "+String.join(" ",result.messages);
                data.reviewed=false;data.save(this);render();
            });
        });
    }
    private void chooseCandidate(int category,int slot){
        if(candidates.size()<4){toast("Refresh news first, or type a verified headline.");return;}
        List<NewsEngine.Item> valid=new ArrayList<>();
        for(NewsEngine.Item item:candidates.get(category))
            if(NewsEngine.recent(item,data.date,Instant.now())&&NewsEngine.suitable(item.title))valid.add(item);
        if(valid.isEmpty()){toast("No recent suggestions found for this category.");return;}
        String[] labels=new String[Math.min(valid.size(),15)];
        for(int i=0;i<labels.length;i++)labels[i]=valid.get(i).title+"\n"+valid.get(i).source;
        new AlertDialog.Builder(this).setTitle("Choose a headline").setItems(labels,(dialog,which)->{
            data.news[slot]=valid.get(which).copy();data.reviewed=false;data.save(this);render();
        }).setNegativeButton("Cancel",null).show();
    }
    private void sourceDialog(int slot){
        NewsEngine.Item item=data.news[slot];
        LinearLayout box=new LinearLayout(this);box.setPadding(dp(20),0,dp(20),0);box.setOrientation(LinearLayout.VERTICAL);
        EditText source=new EditText(this);source.setHint("Publisher / source");source.setText(item.source);box.addView(source);
        EditText url=new EditText(this);url.setHint("https://article-link");url.setText(item.url);url.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);box.addView(url);
        EditText published=new EditText(this);published.setHint("Publication date: YYYY-MM-DD");published.setText(item.published.isEmpty()?"":Instant.parse(item.published).atZone(DayContent.ZONE).toLocalDate().toString());box.addView(published);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Source details").setView(box)
            .setPositiveButton("Save",null).setNeutralButton("Open article",(d,w)->openLink(item.url)).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button->{
            String link=url.getText().toString().trim();
            if(!link.isEmpty()&&!NewsEngine.safeUrl(link)){toast("Use a valid https:// source link.");return;}
            String date=published.getText().toString().trim(),iso="";
            if(!date.isEmpty())try{
                if(!item.published.isEmpty()&&Instant.parse(item.published).atZone(DayContent.ZONE).toLocalDate().toString().equals(date))iso=item.published;
                else iso=LocalDate.parse(date).atStartOfDay(DayContent.ZONE).toInstant().toString();
            }catch(Exception e){toast("Use YYYY-MM-DD for publication date.");return;}
            item.source=source.getText().toString().trim();item.url=link;item.published=iso;data.reviewed=false;data.save(this);dialog.dismiss();render();
        }));dialog.show();
    }
    private void assembly(){
        LinearLayout weather=card("Weather · Nambur, Guntur");
        edit(weather,data.weather,"Refresh online or enter a verified weather report.",1000,value->{data.weather=value;data.weatherSource="Teacher edited";data.weatherFetched="";});
        weather.addView(text("Approximate location: 16.36° N, 80.53° E. Forecasts, not live station observations.",12,MUTED));
        if(!data.weatherFetched.isEmpty())weather.addView(text("Fetched: "+PdfMaker.stamp(data.weatherFetched)+" · "+data.weatherSource,12,MUTED));
        weather.addView(button("Refresh news & weather",this::confirmRefresh));
        LinearLayout international=card("Importance of the day · International");
        edit(international,data.international,"Add a verified international observance.",1200,value->data.international=value);
        international.addView(button("UN observance calendar",()->openLink(DayContent.UN)));
        LinearLayout national=card("Importance of the day · India");
        edit(national,data.national,"Add a verified Indian observance.",1200,value->data.national=value);
        String source=DayContent.nationalSource(data.date);
        if(!source.isEmpty())national.addView(button("Calendar source",()->openLink(source)));
        national.addView(text("The starter calendar is limited. 'No entry' does not mean there is no observance. Add school-specific or verified dates here.",13,MUTED));
        LinearLayout affirmation=card("Affirmation · "+data.date.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.ENGLISH)));
        edit(affirmation,data.affirmation(this),"Type this month's affirmation here.",2000,value->data.affirmation(this,value));
        affirmation.addView(text("Saved for this calendar month and reused on its daily sheets.",13,MUTED));
        affirmation.addView(button("Upload affirmation (.txt)",()->{
            importMonth=data.date.toString().substring(0,7);
            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("text/plain").addCategory(Intent.CATEGORY_OPENABLE),22);
        }));
        LinearLayout word=card("Word of the day");
        edit(word,data.word,"Word, meaning and example sentence.",1200,value->data.word=value);
        LinearLayout thought=card("Thought of the day");
        edit(thought,data.thought,"A short thought for students.",1200,value->data.thought=value);
        thought.addView(text("Daily defaults are original lines, not attributed quotations. Both sections are editable.",13,MUTED));
    }
    private void exportScreen(){
        LinearLayout brand=card("Your school logo");
        if(logo!=null){ImageView image=new ImageView(this);image.setImageBitmap(logo);image.setScaleType(ImageView.ScaleType.FIT_CENTER);brand.addView(image,new LinearLayout.LayoutParams(-1,dp(100)));}
        else brand.addView(text("Select the VIVA logo image you shared. The app will remember it and preserve its proportions on every PDF.",14,MUTED));
        brand.addView(button(logo==null?"Select school logo":"Replace school logo",()->startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE),21)));
        LinearLayout review=card("Review before assembly");
        String issues=issues();review.addView(text(issues.isEmpty()?"All eight headlines and supporting sections are filled. Read the sources and confirm their relevance.":issues,14,MUTED));
        CheckBox checked=new CheckBox(this);checked.setText("I have checked the facts, dates and suitability.");checked.setTextColor(INK);checked.setChecked(data.reviewed);
        checked.setOnCheckedChangeListener((v,yes)->{data.reviewed=yes;data.save(this);});review.addView(checked);
        review.addView(text("Edits reset the review flag. Unreviewed exports are marked DRAFT.",12,MUTED));
        LinearLayout export=card("A4 assembly sheet");
        export.addView(text("School header, eight headlines and all assembly sections. Long edits continue onto another A4 page without clipping.",14,MUTED));
        export.addView(button("Preview & save PDF",this::preview));
        LinearLayout privacy=card("About this app");
        privacy.addView(text("VIVA Assembly 0.1.0 · English · Dates use Asia/Kolkata. Drafts, logo and affirmations stay on this device. No login, AI API key, ads or analytics SDK.",13,MUTED));
        privacy.addView(text("Refresh connects to Google News RSS and Open-Meteo. It retrieves headlines, not independently verified or AI-written news. Feeds can fail or return off-topic items; confirm that sports stories concern India internationally.",13,MUTED));
        privacy.addView(text("Weather data: Open-Meteo, CC BY 4.0. Free endpoint is intended for non-commercial use. Word and thought banks rotate every 31 days. Monthly affirmation import supports UTF-8 .txt files.",13,MUTED));
        privacy.addView(button("Open-Meteo attribution",()->openLink("https://open-meteo.com/")));
    }
    private String issues(){
        List<String> missing=new ArrayList<>();int filled=0,sourced=0,old=0;
        for(NewsEngine.Item n:data.news)if(!n.title.trim().isEmpty()){filled++;if(NewsEngine.safeUrl(n.url))sourced++;if(!NewsEngine.recent(n,data.date,Instant.now()))old++;}
        if(filled<8)missing.add((8-filled)+" headline slots are empty.");
        if(sourced<filled)missing.add((filled-sourced)+" headlines need a source link.");
        if(old>0)missing.add(old+" headlines have missing/out-of-window publication dates.");
        if(data.weather.trim().isEmpty())missing.add("Weather is missing.");
        if(data.affirmation(this).trim().isEmpty())missing.add("Monthly affirmation is missing.");
        if(logo==null)missing.add("School logo has not been selected.");
        if(data.international.contains("No entry")||data.national.contains("No entry"))missing.add("Check observances in the official calendars.");
        return String.join("\n",missing);
    }
    private void preview(){
        AssemblyData snapshot=data.copy();String affirmation=data.affirmation(this);Bitmap logoCopy=logo;
        toast("Preparing A4 preview…");
        worker.execute(()->{
            try{
                byte[] bytes=PdfMaker.create(snapshot,affirmation,logoCopy);
                runOnUiThread(()->{if(!isDestroyed())showPreview(bytes,snapshot.date.toString());});
            }catch(Exception e){runOnUiThread(()->toast("Could not create PDF: "+e.getMessage()));}
        });
    }
    private void showPreview(byte[] bytes,String date){
        File file=new File(getCacheDir(),"preview.pdf");
        List<Bitmap> previews=new ArrayList<>();
        try{
            try(FileOutputStream out=new FileOutputStream(file)){out.write(bytes);}
            try(ParcelFileDescriptor descriptor=ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);PdfRenderer renderer=new PdfRenderer(descriptor)){
                for(int i=0;i<renderer.getPageCount();i++)try(PdfRenderer.Page page=renderer.openPage(i)){
                    Bitmap bitmap=Bitmap.createBitmap(595,842,Bitmap.Config.ARGB_8888);bitmap.eraseColor(Color.WHITE);
                    page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);previews.add(bitmap);
                }
            }
            LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
            content.addView(text(previews.size()+" A4 page"+(previews.size()==1?"":"s")+" · "+date,15,INK));
            for(Bitmap bitmap:previews){
                ImageView image=new ImageView(this);image.setImageBitmap(bitmap);image.setAdjustViewBounds(true);image.setScaleType(ImageView.ScaleType.FIT_CENTER);content.addView(image,new LinearLayout.LayoutParams(-1,-2));
            }
            ScrollView scroll=new ScrollView(this);scroll.addView(content);
            new AlertDialog.Builder(this).setTitle("Assembly sheet preview").setView(scroll)
                .setPositiveButton("Save PDF",(d,w)->{
                    pendingPdf=bytes;pendingName="VIVA-Assembly-"+date+".pdf";
                    try(FileOutputStream out=new FileOutputStream(new File(getCacheDir(),"pending-export.pdf"))){out.write(bytes);}
                    catch(IOException e){toast("Could not prepare export");return;}
                    AssemblyData.prefs(this).edit().putString("pendingName",pendingName).apply();
                    startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/pdf").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,pendingName),23);
                }).setNegativeButton("Back to editor",null).show();
        }catch(Exception e){toast("Could not preview PDF: "+e.getMessage());}
    }
    private void loadLogo(){File file=new File(getFilesDir(),"school-logo.png");logo=file.exists()?BitmapFactory.decodeFile(file.getAbsolutePath()):null;}
    private void importLogo(Uri uri){
        worker.execute(()->{
            try{
                byte[] bytes=readLimited(uri,12_000_000);
                BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,bounds);
                if(bounds.outWidth<=0||bounds.outHeight<=0)throw new IOException("Choose a PNG or JPEG image.");
                BitmapFactory.Options opts=new BitmapFactory.Options();opts.inSampleSize=1;
                while(bounds.outWidth/opts.inSampleSize>1200||bounds.outHeight/opts.inSampleSize>1200)opts.inSampleSize*=2;
                Bitmap image=BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
                if(image==null)throw new IOException("Image could not be read");
                File temp=new File(getFilesDir(),"logo-import.png");
                try(FileOutputStream out=new FileOutputStream(temp)){if(!image.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("Image could not be saved");}
                File dest=new File(getFilesDir(),"school-logo.png");
                if(!temp.renameTo(dest))throw new IOException("Logo could not be replaced");
                runOnUiThread(()->{logo=image;data.reviewed=false;data.save(this);if(!isDestroyed())render();toast("School logo saved");});
            }catch(Exception e){runOnUiThread(()->toast("Logo import failed: "+e.getMessage()));}
        });
    }
    private byte[] readLimited(Uri uri,int max) throws IOException{
        try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            if(in==null)throw new IOException("File unavailable");byte[] buffer=new byte[8192];int n,total=0;
            while((n=in.read(buffer))!=-1){total+=n;if(total>max)throw new IOException("File too large");out.write(buffer,0,n);}return out.toByteArray();
        }
    }
    @Override protected void onActivityResult(int request,int result,Intent intent){
        super.onActivityResult(request,result,intent);
        if(result!=RESULT_OK||intent==null||intent.getData()==null)return;
        Uri uri=intent.getData();
        if(request==21)importLogo(uri);
        if(request==22){
            final String month=importMonth.isEmpty()?data.date.toString().substring(0,7):importMonth;
            worker.execute(()->{
                try{
                    String value=new String(readLimited(uri,32000),StandardCharsets.UTF_8).replace("\ufeff","").trim();
                    if(value.length()>2000)throw new IOException("Keep the affirmation below 2,000 characters.");
                    AssemblyData.prefs(this).edit().putString("affirmation:"+month,value).apply();
                    runOnUiThread(()->{data.reviewed=false;data.save(this);if(!isDestroyed())render();toast("Affirmation saved for "+month);});
                }catch(Exception e){runOnUiThread(()->toast("Import failed: "+e.getMessage()));}
            });
        }
        if(request==23)worker.execute(()->{
            try{
                byte[] bytes=pendingPdf;
                if(bytes==null)try(FileInputStream in=new FileInputStream(new File(getCacheDir(),"pending-export.pdf"));ByteArrayOutputStream buffer=new ByteArrayOutputStream()){
                    byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)buffer.write(b,0,n);bytes=buffer.toByteArray();
                }
                try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("Destination unavailable");out.write(bytes);}
                pendingPdf=null;runOnUiThread(()->toast("A4 PDF saved. Open it from your Files app."));
            }catch(Exception e){runOnUiThread(()->toast("PDF export failed: "+e.getMessage()));}
        });
    }
    private void openLink(String url){
        if(!NewsEngine.safeUrl(url)){toast("Add a valid source link first.");return;}
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(ActivityNotFoundException e){toast("No browser available");}
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
