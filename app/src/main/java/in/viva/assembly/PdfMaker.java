package in.viva.assembly;

import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

final class PdfMaker {
    static final int PURPLE=0xff5f4c96,INK=0xff243138,MUTED=0xff59646a;
    static final class Line{
        final String text;final float size,step;final int color;final boolean bold;
        Line(String t,float size,float step,int color,boolean bold){text=t;this.size=size;this.step=step;this.color=color;this.bold=bold;}
    }
    private static void add(List<Line> list,String value,float size,int color,boolean bold,float width){
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setTextSize(size);p.setTypeface(bold?Typeface.create("sans-serif",Typeface.BOLD):Typeface.create("sans-serif",Typeface.NORMAL));
        for(String line:TextWrap.lines(value,width,p::measureText))list.add(new Line(line,size,size*1.32f,color,bold));
    }
    private static void gap(List<Line> list,float points){list.add(new Line("",1,points,INK,false));}
    private static void section(List<Line> list,String title,String value,float width){
        gap(list,10);add(list,title.toUpperCase(Locale.ENGLISH),10,PURPLE,true,width);gap(list,4);
        add(list,value.trim().isEmpty()?"Not yet added.":value,11,INK,false,width);
    }
    static String stamp(String iso){
        try{return Instant.parse(iso).atZone(DayContent.ZONE).format(DateTimeFormatter.ofPattern("dd MMM, HH:mm 'IST'",Locale.ENGLISH));}
        catch(Exception e){return iso.isEmpty()?"Not provided":iso;}
    }
    static byte[] create(AssemblyData d,String affirmation,Bitmap logo) throws IOException {
        List<Line> left=new ArrayList<>(),right=new ArrayList<>();
        float leftWidth=267,rightWidth=226;
        for(int category=0;category<4;category++){
            gap(left,category==0?0:10);
            add(left,NewsEngine.CATEGORIES[category].toUpperCase(Locale.ENGLISH),10,PURPLE,true,leftWidth);gap(left,5);
            for(int n=0;n<2;n++){
                NewsEngine.Item item=d.news[category*2+n];
                add(left,(n+1)+". "+(item.title.isEmpty()?"Headline not yet selected.":item.title),10.5f,INK,false,leftWidth);
                if(!item.title.isEmpty())add(left,(item.source.isEmpty()?"Source not provided":item.source)+" · "+stamp(item.published),7.5f,MUTED,false,leftWidth);
                gap(left,7);
            }
        }
        section(right,"Weather · Nambur",d.weather,rightWidth);
        if(!d.weather.isEmpty())add(right,"Source: "+(d.weatherSource.isEmpty()?"Teacher entry":d.weatherSource)+" · fetched "+stamp(d.weatherFetched),7.5f,MUTED,false,rightWidth);
        section(right,"International observance",d.international,rightWidth);
        section(right,"National observance",d.national,rightWidth);
        add(right,"Starter calendar is not exhaustive; teacher review required.",7.5f,MUTED,false,rightWidth);
        section(right,"Affirmation · "+d.date.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.ENGLISH)),affirmation,rightWidth);
        section(right,"Word of the day",d.word,rightWidth);
        section(right,"Thought of the day",d.thought,rightWidth);
        add(right,"Original thought; editable by the teacher.",7.5f,MUTED,false,rightWidth);
        int l=0,r=0,pageNo=0;
        PdfDocument pdf=new PdfDocument();
        try{
            while(l<left.size()||r<right.size()){
                PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,++pageNo).create());
                Canvas c=page.getCanvas();Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
                c.drawColor(Color.WHITE);
                p.setColor(PURPLE);c.drawRect(0,0,595,7,p);
                if(logo!=null){
                    float scale=Math.min(75f/logo.getWidth(),66f/logo.getHeight());
                    float w=logo.getWidth()*scale,h=logo.getHeight()*scale;
                    c.drawBitmap(logo,null,new RectF(30,20,30+w,20+h),p);
                }else{
                    p.setColor(PURPLE);p.setTextSize(20);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));c.drawText("VIVA",30,48,p);
                    p.setTextSize(8);c.drawText("THE SCHOOL",30,63,p);
                }
                p.setColor(INK);p.setTextSize(19);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
                c.drawText("MORNING ASSEMBLY",120,40,p);
                p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(10);
                c.drawText("VIVA The School · Nambur, Guntur",120,58,p);
                c.drawText(d.date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",Locale.ENGLISH)),120,75,p);
                p.setColor(MUTED);p.setTextSize(7.5f);
                c.drawText((d.reviewed?"TEACHER REVIEWED":"DRAFT — PLEASE REVIEW")+" · News refreshed: "+stamp(d.fetched),30,99,p);
                p.setColor(0xffdedbe8);c.drawLine(30,107,565,107,p);c.drawLine(308,119,308,795,p);
                l=drawColumn(c,left,l,30,125,790);r=drawColumn(c,right,r,327,125,790);
                p.setTextSize(7.5f);p.setColor(MUTED);
                c.drawText("VIVA Assembly · Sources and article links are available in the app.",30,817,p);
                c.drawText("A4 · "+pageNo,527,817,p);
                pdf.finishPage(page);
            }
            ByteArrayOutputStream out=new ByteArrayOutputStream();pdf.writeTo(out);return out.toByteArray();
        } finally { pdf.close(); }
    }
    private static int drawColumn(Canvas c,List<Line> lines,int index,float x,float y,float bottom){
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        while(index<lines.size()){
            Line line=lines.get(index);if(y+line.step>bottom)break;
            p.setColor(line.color);p.setTextSize(line.size);p.setTypeface(Typeface.create("sans-serif",line.bold?Typeface.BOLD:Typeface.NORMAL));
            c.drawText(line.text,x,y+line.size,p);y+=line.step;index++;
        }
        return index;
    }
}
