package in.viva.assembly;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

final class OnlineContent {
    static final String WEATHER_URL="https://api.open-meteo.com/v1/forecast?latitude=16.36&longitude=80.53&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=Asia%2FKolkata&forecast_days=16";
    static String get(String url) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setConnectTimeout(10000);c.setReadTimeout(12000);c.setRequestProperty("User-Agent","VIVAAssembly/0.1 school-assembly-reader");
        try{
            if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());
            try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] b=new byte[8192];int n,total=0;
                while((n=in.read(b))!=-1){total+=n;if(total>2_000_000)throw new IOException("Response too large");out.write(b,0,n);}
                return new String(out.toByteArray(),StandardCharsets.UTF_8);
            }
        }finally{c.disconnect();}
    }
    static final class Result {
        final List<List<NewsEngine.Item>> candidates=new ArrayList<>();
        final List<String> messages=new ArrayList<>();
        String weather="",weatherFetched="";
    }
    static Result fetch(LocalDate selected){
        ExecutorService pool=Executors.newFixedThreadPool(5);
        Result result=new Result();List<Future<List<NewsEngine.Item>>> jobs=new ArrayList<>();
        for(int i=0;i<4;i++){final int category=i;jobs.add(pool.submit(()->NewsEngine.parse(get(NewsEngine.feedUrl(category,selected,DayContent.today())))));}
        Future<String> weather=pool.submit(()->weather(selected));
        try{
            for(int i=0;i<4;i++){
                try{result.candidates.add(jobs.get(i).get(28,TimeUnit.SECONDS));}
                catch(Exception e){result.candidates.add(new ArrayList<>());result.messages.add(NewsEngine.CATEGORIES[i]+": feed unavailable; existing text kept.");}
            }
            try{result.weather=weather.get(28,TimeUnit.SECONDS);result.weatherFetched=Instant.now().toString();}
            catch(Exception e){result.messages.add("Weather unavailable for this date; existing text kept.");}
        }finally{pool.shutdownNow();}
        return result;
    }
    static String weather(LocalDate selected) throws Exception {
        LocalDate now=DayContent.today();
        if(selected.isBefore(now)||selected.isAfter(now.plusDays(15)))throw new IOException("Outside forecast range");
        JSONObject daily=new JSONObject(get(WEATHER_URL)).getJSONObject("daily");
        JSONArray dates=daily.getJSONArray("time");
        for(int i=0;i<dates.length();i++)if(selected.toString().equals(dates.getString(i))){
            double min=daily.getJSONArray("temperature_2m_min").getDouble(i),max=daily.getJSONArray("temperature_2m_max").getDouble(i);
            int code=daily.getJSONArray("weather_code").getInt(i);
            JSONArray rain=daily.getJSONArray("precipitation_probability_max");
            String chance=rain.isNull(i)?"Unavailable":rain.getInt(i)+"%";
            return String.format(Locale.ENGLISH,"Nambur, Guntur — %s. Expected temperature: %.0f–%.0f °C. Maximum rain probability: %s. Forecast for %s; approximate village location.",condition(code),min,max,chance,selected);
        }
        throw new IOException("Date missing");
    }
    static String condition(int code){
        if(code==0)return "Clear sky";if(code<=3)return "Partly cloudy to overcast";
        if(code==45||code==48)return "Fog";if(code>=51&&code<=57)return "Drizzle";
        if(code>=61&&code<=67)return "Rain";if(code>=71&&code<=77)return "Snow";
        if(code>=80&&code<=82)return "Rain showers";if(code==85||code==86)return "Snow showers";
        if(code>=95&&code<=99)return "Thunderstorms";return "Conditions unavailable";
    }
}
