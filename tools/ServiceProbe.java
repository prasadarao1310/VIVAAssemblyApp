package in.viva.assembly;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
public class ServiceProbe {
    public static void main(String[] args) throws Exception {
        int failures=0;Set<String> used=new HashSet<>();
        for(int i=0;i<4;i++){
            try{
                HttpURLConnection c=(HttpURLConnection)new URL(NewsEngine.feedUrl(i,DayContent.today(),DayContent.today())).openConnection();
                c.setConnectTimeout(10000);c.setReadTimeout(12000);c.setRequestProperty("User-Agent","VIVAAssembly/0.1 school-assembly-reader");
                try{
                    String xml;
                    try(java.io.InputStream in=c.getInputStream()){xml=new String(in.readNBytes(2_000_000),StandardCharsets.UTF_8);}
                    List<NewsEngine.Item> parsed=NewsEngine.parse(xml);
                    List<NewsEngine.Item> chosen=NewsEngine.choose(parsed,i,DayContent.today(),Instant.now(),used);
                    System.out.println(NewsEngine.CATEGORIES[i]+": "+parsed.size()+" parsed, "+chosen.size()+" eligible selected");
                    if(chosen.isEmpty())failures++;
                }finally{c.disconnect();}
            }catch(Exception e){failures++;System.out.println(NewsEngine.CATEGORIES[i]+": "+e.getClass().getSimpleName()+" "+e.getMessage());}
        }
        if(failures>0)System.out.println("Some live feeds unavailable or empty. Manual editing remains available.");
    }
}
