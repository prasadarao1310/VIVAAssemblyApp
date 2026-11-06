package in.viva.assembly;

import android.content.*;
import org.json.*;
import java.time.*;

final class AssemblyData {
    final LocalDate date;
    NewsEngine.Item[] news=new NewsEngine.Item[8];
    String weather="",weatherSource="",weatherFetched="",international,national,word,thought;
    String fetched="",status="No online refresh yet.";
    boolean reviewed=false;
    AssemblyData(LocalDate date){
        this.date=date;for(int i=0;i<8;i++)news[i]=new NewsEngine.Item();
        international=DayContent.international(date);national=DayContent.national(date);
        word=DayContent.word(date);thought=DayContent.thought(date);
    }
    static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("assembly",Context.MODE_PRIVATE);}
    static AssemblyData load(Context c,LocalDate date){
        AssemblyData d=new AssemblyData(date);
        try {
            JSONObject j=new JSONObject(prefs(c).getString("day:"+date,"{}"));
            JSONArray a=j.optJSONArray("news");
            if(a!=null)for(int i=0;i<Math.min(a.length(),8);i++){
                JSONObject item=a.getJSONObject(i);d.news[i]=new NewsEngine.Item(item.optString("title"),item.optString("source"),item.optString("url"),item.optString("published"),item.optString("publisherUrl"));
            }
            d.weather=j.optString("weather");d.weatherSource=j.optString("weatherSource");d.weatherFetched=j.optString("weatherFetched");
            d.international=j.optString("international",d.international);d.national=j.optString("national",d.national);
            d.word=j.optString("word",d.word);d.thought=j.optString("thought",d.thought);
            d.fetched=j.optString("fetched");d.status=j.optString("status",d.status);d.reviewed=j.optBoolean("reviewed",false)&&j.optString("affirmationAtReview").equals(d.affirmation(c));
            if(j.optInt("schema",0)<2){
                if(d.word.equals(DayContent.legacyWord(date)))d.word=DayContent.word(date);
                if(d.international.equals(DayContent.INTERNATIONAL.getOrDefault(date.toString().substring(5),""))||d.international.startsWith("No entry"))d.international=DayContent.international(date);
                String[] national=DayContent.NATIONAL.get(date.toString().substring(5));
                if((national!=null&&d.national.equals(national[0]))||d.national.startsWith("No entry"))d.national=DayContent.national(date);
                d.reviewed=false;
                d.status="Previous-day/source rules changed. Refresh and review this saved draft.";
            }
        }catch(JSONException ignored){}
        return d;
    }
    void save(Context c){
        try{
            JSONObject j=new JSONObject();JSONArray a=new JSONArray();
            for(NewsEngine.Item n:news)a.put(new JSONObject().put("title",n.title).put("source",n.source).put("url",n.url).put("published",n.published).put("publisherUrl",n.publisherUrl));
            j.put("schema",2).put("news",a).put("weather",weather).put("weatherSource",weatherSource).put("weatherFetched",weatherFetched)
             .put("international",international).put("national",national).put("word",word).put("thought",thought)
             .put("fetched",fetched).put("status",status).put("reviewed",reviewed).put("affirmationAtReview",reviewed?affirmation(c):"");
            prefs(c).edit().putString("day:"+date,j.toString()).apply();
        }catch(JSONException e){throw new IllegalStateException(e);}
    }
    String affirmation(Context c){return prefs(c).getString("affirmation:"+date.toString().substring(0,7),"");}
    void affirmation(Context c,String value){prefs(c).edit().putString("affirmation:"+date.toString().substring(0,7),value).apply();}
    AssemblyData copy(){
        AssemblyData d=new AssemblyData(date);
        for(int i=0;i<8;i++)d.news[i]=news[i].copy();
        d.weather=weather;d.weatherSource=weatherSource;d.weatherFetched=weatherFetched;d.international=international;d.national=national;
        d.word=word;d.thought=thought;d.fetched=fetched;d.status=status;d.reviewed=reviewed;return d;
    }
}
