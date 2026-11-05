package in.viva.assembly;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

final class NewsEngine {
    static final String[] CATEGORIES={"International","National — India","Andhra Pradesh","Indian sports internationally"};
    static final String[] QUERIES={
        "(world OR international) (science OR environment OR education OR diplomacy OR technology) -India -cricket -football",
        "India (education OR science OR economy OR environment OR government) -Andhra -Telangana -cricket",
        "\"Andhra Pradesh\" (education OR development OR science OR environment OR government)",
        "(India OR Indian) (international OR world OR Olympics OR championship OR Asia) (cricket OR badminton OR chess OR hockey OR athletics OR tennis OR shooting) -IPL -Ranji"
    };
    static final class Item {
        String title="",source="",url="",published="";
        Item(){}
        Item(String title,String source,String url,String published){this.title=title;this.source=source;this.url=url;this.published=published;}
        Item copy(){return new Item(title,source,url,published);}
        String stamp(){return title+"\n"+source+"\n"+url+"\n"+published;}
    }
    static String feedUrl(int category,LocalDate selected,LocalDate today){
        LocalDate cutoff=selected.isAfter(today)?today:selected;
        String q=QUERIES[category]+" after:"+cutoff.minusDays(2)+" before:"+cutoff.plusDays(1);
        try{return "https://news.google.com/rss/search?q="+URLEncoder.encode(q,"UTF-8")+"&hl=en-IN&gl=IN&ceid=IN:en";}
        catch(Exception e){throw new IllegalArgumentException(e);}
    }
    static String clean(String text){return text.replaceAll("<[^>]*>","").replaceAll("\\s+"," ").trim();}
    static String field(Element e,String name){NodeList n=e.getElementsByTagName(name);return n.getLength()==0?"":clean(n.item(0).getTextContent());}
    static List<Item> parse(String xml) throws Exception {
        String upper=xml.toUpperCase(Locale.ROOT);
        if(upper.contains("<!DOCTYPE")||upper.contains("<!ENTITY"))throw new IOException("External XML declarations are not allowed");
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder=factory.newDocumentBuilder();
        builder.setEntityResolver((publicId,systemId)->{throw new org.xml.sax.SAXException("External entities disabled");});
        Document doc=builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        List<Item> result=new ArrayList<>();NodeList nodes=doc.getElementsByTagName("item");
        for(int i=0;i<Math.min(nodes.getLength(),100);i++){
            Element e=(Element)nodes.item(i);
            String title=field(e,"title"),source=field(e,"source"),url=field(e,"link"),published=field(e,"pubDate");
            if(!source.isEmpty() && title.endsWith(" - "+source))title=title.substring(0,title.length()-source.length()-3);
            try {
                Instant date=ZonedDateTime.parse(published,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                if(!title.isEmpty() && safeUrl(url))result.add(new Item(title,source.isEmpty()?"News publisher":source,url,date.toString()));
            } catch(DateTimeParseException ignored){}
        }
        return result;
    }
    static boolean safeUrl(String value){
        try {URI uri=new URI(value);return "https".equalsIgnoreCase(uri.getScheme())&&uri.getHost()!=null;}
        catch(Exception e){return false;}
    }
    static boolean recent(Item i,LocalDate selected,Instant now){
        try {
            LocalDate cutoff=selected.isAfter(now.atZone(DayContent.ZONE).toLocalDate())?now.atZone(DayContent.ZONE).toLocalDate():selected;
            Instant time=Instant.parse(i.published);
            return !time.isBefore(cutoff.minusDays(2).atStartOfDay(DayContent.ZONE).toInstant())
                &&time.isBefore(cutoff.plusDays(1).atStartOfDay(DayContent.ZONE).toInstant())&&!time.isAfter(now);
        } catch(Exception e){return false;}
    }
    static String canonical(String t){return t.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").trim();}
    static boolean suitable(String t){
        String s=" "+canonical(t)+" ";
        for(String term:new String[]{" rape "," murder "," sexual "," suicide "," betting "," gambling "," horoscope "," celebrity "})
            if(s.contains(term))return false;
        return true;
    }
    static List<Item> choose(List<Item> candidates,LocalDate selected,Instant now,Set<String> used){
        List<Item> out=new ArrayList<>();
        candidates.sort((a,b)->b.published.compareTo(a.published));
        for(Item i:candidates){
            String key=canonical(i.title);
            if(recent(i,selected,now)&&suitable(i.title)&&used.add(key)){out.add(i);if(out.size()==2)break;}
        }
        return out;
    }
}
