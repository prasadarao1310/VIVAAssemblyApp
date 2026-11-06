package in.viva.assembly;

import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.text.*;
import android.text.style.StyleSpan;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

final class PdfMaker {
    static final int PURPLE=0xff5f4c96,INK=0xff243138,MUTED=0xff59646a;
    static String stamp(String iso){
        try{return Instant.parse(iso).atZone(DayContent.ZONE).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm 'IST'",Locale.ENGLISH));}
        catch(Exception e){return iso.isEmpty()?"Not provided":iso;}
    }
    static byte[] create(AssemblyData d,String affirmation,Bitmap logo) throws IOException {
        if(!ObservanceNotes.sufficient(d.international,d.national))throw new IOException("Importance of the day must contain at least 100 words.");
        for(int i=0;i<8;i++)if(!NewsEngine.eligible(d.news[i],i/2,d.date,Instant.now()))
            throw new IOException("Headline "+(i+1)+" needs an approved publisher and publication date "+d.date.minusDays(1)+".");
        Set<String> titles=new HashSet<>();
        for(NewsEngine.Item item:d.news)if(!titles.add(NewsEngine.canonical(item.title)))throw new IOException("Duplicate headline: select a different story.");
        PdfDocument pdf=new PdfDocument();Writer w=new Writer(pdf,d,logo);
        try{
            w.newPage();
            w.heading("Morning News Headlines");
            w.paragraph("News published on "+d.date.minusDays(1).format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy",Locale.ENGLISH))+" only (India time).",9,MUTED,false);
            String[] headings={"International News","National News","State News (Andhra Pradesh)","Sports News — India Internationally"};
            for(int c=0;c<4;c++){
                w.heading(headings[c]);
                for(int n=0;n<2;n++){
                    NewsEngine.Item item=d.news[c*2+n];
                    w.paragraph("• "+item.title,11.2f,INK,false);
                    w.paragraph("Source: "+item.source+" | Published: "+stamp(item.published),8,MUTED,false);
                    w.gap(4);
                }
            }
            w.heading("Weather Report for Namburu, Andhra Pradesh");
            w.paragraph(d.weather.isEmpty()?"Weather report not yet added.":d.weather,11,INK,false);
            if(!d.weather.isEmpty())w.paragraph("Source: "+(d.weatherSource.isEmpty()?"Teacher entry":d.weatherSource)+" | Retrieved: "+stamp(d.weatherFetched),8,MUTED,false);
            // Match the supplied example: the assembly reflections start on a separate A4 page.
            w.newPage();
            w.heading("Importance of the Day — "+d.date.format(DateTimeFormatter.ofPattern("d MMMM",Locale.ENGLISH)));
            w.paragraph("International significance",10.5f,PURPLE,true);
            w.paragraph(d.international,11,INK,false);
            w.paragraph("Calendar reference: "+DayContent.UN,7.5f,MUTED,false);
            w.gap(5);
            w.paragraph("National significance",10.5f,PURPLE,true);
            w.paragraph(d.national,11,INK,false);
            String source=DayContent.nationalSource(d.date);
            if(!source.isEmpty())w.paragraph("Calendar reference: "+source,7.5f,MUTED,false);
            w.heading("Thought for the Day");
            w.paragraph(d.thought,11.5f,INK,false);
            w.heading("Affirmation Theme of the Month — "+d.date.format(DateTimeFormatter.ofPattern("MMMM",Locale.ENGLISH)));
            w.paragraph(affirmation.trim().isEmpty()?"Monthly affirmation not yet added.":affirmation,11.5f,INK,false);
            w.heading("Word for the Day");
            w.paragraph(d.word,11.5f,INK,false);
            w.finish();
            ByteArrayOutputStream out=new ByteArrayOutputStream();pdf.writeTo(out);return out.toByteArray();
        }finally{w.finish();pdf.close();}
    }
    private static final class Writer{
        final PdfDocument pdf;final AssemblyData data;final Bitmap logo;
        PdfDocument.Page page;Canvas canvas;int number=0;float y;
        static final int WIDTH=511;static final float BOTTOM=790;
        Writer(PdfDocument pdf,AssemblyData data,Bitmap logo){this.pdf=pdf;this.data=data;this.logo=logo;}
        void finish(){if(page!=null){pdf.finishPage(page);page=null;}}
        void newPage(){
            finish();page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,++number).create());canvas=page.getCanvas();canvas.drawColor(Color.WHITE);
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(PURPLE);canvas.drawRect(0,0,595,5,p);
            if(logo!=null){
                float scale=Math.min(70f/logo.getWidth(),62f/logo.getHeight());
                canvas.drawBitmap(logo,null,new RectF(42,17,42+logo.getWidth()*scale,17+logo.getHeight()*scale),p);
            }else{p.setTextSize(19);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));canvas.drawText("VIVA",42,44,p);}
            p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(17);p.setColor(INK);
            canvas.drawText("VIVA The School",132,36,p);
            p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(10);
            canvas.drawText("Morning Assembly | Nambur, Guntur",132,53,p);
            canvas.drawText(data.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy",Locale.ENGLISH)),132,69,p);
            p.setTextSize(7.5f);p.setColor(MUTED);
            canvas.drawText(data.reviewed?"TEACHER REVIEWED":"DRAFT — TEACHER REVIEW REQUIRED",42,94,p);
            canvas.drawLine(42,101,553,101,p);
            canvas.drawText("News date: "+data.date.minusDays(1)+" | Sources and article links available in the app.",42,817,p);
            canvas.drawText("A4 | "+number,516,817,p);y=110;
        }
        void gap(float value){y+=value;}
        void heading(String text){
            if(y+72>BOTTOM)newPage();gap(9);paragraph(text,13.5f,PURPLE,true);gap(3);
        }
        void paragraph(String text,float size,int color,boolean bold){
            if(text.trim().isEmpty())return;
            TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG);paint.setColor(color);paint.setTextSize(size);
            paint.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
            SpannableString content=new SpannableString(text);
            int colon=text.indexOf(':');
            if(!bold&&size>=11&&colon>0&&colon<90)content.setSpan(new StyleSpan(Typeface.BOLD),0,colon+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StaticLayout layout=StaticLayout.Builder.obtain(content,0,content.length(),paint,WIDTH)
                .setIncludePad(false).setLineSpacing(1.5f,1.1f).build();
            int line=0;
            while(line<layout.getLineCount()){
                if(y+size*1.6f>BOTTOM)newPage();
                int end=line;float available=BOTTOM-y;int top=layout.getLineTop(line);
                while(end<layout.getLineCount()&&layout.getLineBottom(end)-top<=available)end++;
                if(end==line){newPage();continue;}
                float height=layout.getLineBottom(end-1)-top;
                canvas.save();canvas.translate(42,y);canvas.clipRect(0,0,WIDTH,height);canvas.translate(0,-top);layout.draw(canvas);canvas.restore();
                y+=height+3;line=end;
                if(line<layout.getLineCount())newPage();
            }
        }
    }
}
