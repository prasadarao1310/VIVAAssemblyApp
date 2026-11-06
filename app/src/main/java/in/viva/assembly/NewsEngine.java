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
    static final String[][] DOMAINS={
        {"indianexpress.com","thehindu.com"},{"indianexpress.com","thehindu.com"},
        {"eenadu.net","andhrajyothy.com","sakshi.com"},{"indianexpress.com","thehindu.com"}
    };
    static final String[] QUERIES={
        "(site:indianexpress.com/article/world OR site:thehindu.com/news/international)",
        "(site:indianexpress.com/article/india OR site:thehindu.com/news/national) -sports",
        "(site:eenadu.net OR site:andhrajyothy.com OR site:sakshi.com) (ఆంధ్రప్రదేశ్ OR అమరావతి OR ఆంధ్ర OR \"Andhra Pradesh\")",
        "(site:indianexpress.com/article/sports OR site:thehindu.com/sport) (India OR Indian) (international OR world OR Olympics OR championship OR Asia OR series) -IPL -Ranji"
    };
    static final class Item {
        String title="",source="",url="",published="",publisherUrl="";
        Item(){}
        Item(String title,String source,String url,String published){this(title,source,url,published,"");}
        Item(String title,String source,String url,String published,String publisherUrl){
            this.title=title;this.source=source;this.url=url;this.published=published;this.publisherUrl=publisherUrl;
        }
        Item copy(){return new Item(title,source,url,published,publisherUrl);}
        String stamp(){return title+"\n"+source+"\n"+url+"\n"+published+"\n"+publisherUrl;}
    }
    static String feedUrl(int category,LocalDate assembly,LocalDate today){
        // The query is a discovery hint. Exact IST boundaries are enforced below.
        LocalDate target=assembly.minusDays(1);
        String q=QUERIES[category]+" after:"+target.minusDays(1)+" before:"+assembly.plusDays(1);
        try{
            String language=category==2?"te":"en";
            return "https://news.google.com/rss/search?q="+URLEncoder.encode(q,"UTF-8")+"&hl="+language+"-IN&gl=IN&ceid=IN:"+language;
        }catch(Exception e){throw new IllegalArgumentException(e);}
    }
    static String clean(String text){return text.replaceAll("<[^>]*>","").replaceAll("\\s+"," ").trim();}
    static String field(Element e,String name){NodeList n=e.getElementsByTagName(name);return n.getLength()==0?"":clean(n.item(0).getTextContent());}
    static List<Item> parse(String xml) throws Exception {
        String upper=xml.toUpperCase(Locale.ROOT);
        if(upper.contains("<!DOCTYPE")||upper.contains("<!ENTITY"))throw new IOException("External XML declarations are not allowed");
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();factory.setExpandEntityReferences(false);
        DocumentBuilder builder=factory.newDocumentBuilder();
        builder.setEntityResolver((publicId,systemId)->{throw new org.xml.sax.SAXException("External entities disabled");});
        Document doc=builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        List<Item> result=new ArrayList<>();NodeList nodes=doc.getElementsByTagName("item");
        for(int i=0;i<Math.min(nodes.getLength(),100);i++){
            Element e=(Element)nodes.item(i);
            String title=field(e,"title"),source=field(e,"source"),url=field(e,"link"),published=field(e,"pubDate"),publisherUrl="";
            NodeList sources=e.getElementsByTagName("source");
            if(sources.getLength()>0)publisherUrl=((Element)sources.item(0)).getAttribute("url");
            if(!source.isEmpty()&&title.endsWith(" - "+source))title=title.substring(0,title.length()-source.length()-3);
            try{
                Instant date=ZonedDateTime.parse(published,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                if(!title.isEmpty()&&safeUrl(url))result.add(new Item(title,source,url,date.toString(),publisherUrl));
            }catch(DateTimeParseException ignored){}
        }
        return result;
    }
    static boolean safeUrl(String value){
        try{URI uri=new URI(value);return "https".equalsIgnoreCase(uri.getScheme())&&uri.getHost()!=null&&uri.getUserInfo()==null;}
        catch(Exception e){return false;}
    }
    static String host(String url){
        try{return safeUrl(url)?new URI(url).getHost().toLowerCase(Locale.ROOT):"";}catch(Exception e){return "";}
    }
    static String publisherHost(String url){
        try{URI u=new URI(url);String scheme=u.getScheme();return ("https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme))&&u.getUserInfo()==null&&u.getHost()!=null?u.getHost().toLowerCase(Locale.ROOT):"";}
        catch(Exception e){return "";}
    }
    static boolean domainAllowed(String host,int category){
        if(category<0||category>=DOMAINS.length)return false;
        for(String d:DOMAINS[category])if(host.equals(d)||host.endsWith("."+d))return true;
        return false;
    }
    static boolean allowed(Item item,int category){
        String articleHost=host(item.url);
        if(domainAllowed(articleHost,category))return true;
        // Only Google News feed links may use the accompanying publisher-domain metadata.
        return articleHost.equals("news.google.com")&&domainAllowed(publisherHost(item.publisherUrl),category);
    }
    static boolean recent(Item item,LocalDate assembly,Instant now){
        try{
            Instant time=Instant.parse(item.published);
            LocalDate required=assembly.minusDays(1);
            return time.atZone(DayContent.ZONE).toLocalDate().equals(required)&&!time.isAfter(now);
        }catch(Exception e){return false;}
    }
    static boolean eligible(Item item,int category,LocalDate assembly,Instant now){
        return !item.title.trim().isEmpty()&&allowed(item,category)&&recent(item,assembly,now)&&suitable(item.title);
    }
    static String canonical(String t){return t.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").trim();}
    static boolean suitable(String t){
        String s=" "+canonical(t)+" ";
        for(String term:new String[]{" rape "," murder "," sexual "," suicide "," betting "," gambling "," horoscope "," celebrity "," అత్యాచారం "," ఆత్మహత్య "})
            if(s.contains(term))return false;
        return true;
    }
    static List<Item> choose(List<Item> candidates,int category,LocalDate assembly,Instant now,Set<String> used){
        List<Item> out=new ArrayList<>();candidates.sort((a,b)->b.published.compareTo(a.published));
        for(Item item:candidates){
            String key=canonical(item.title);
            if(eligible(item,category,assembly,now)&&used.add(key)){out.add(item);if(out.size()==2)break;}
        }
        return out;
    }
}
