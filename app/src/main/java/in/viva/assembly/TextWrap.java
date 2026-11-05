package in.viva.assembly;
import java.util.*;
final class TextWrap {
    interface Measure {float width(String s);}
    static List<String> lines(String text,float width,Measure measure){
        if(width<=0)throw new IllegalArgumentException("Positive width required");
        List<String> result=new ArrayList<>();
        for(String paragraph:text.replace("\r","").split("\n",-1)){
            String current="";
            for(String word:paragraph.trim().split("\\s+")){
                if(word.isEmpty())continue;
                String combined=current.isEmpty()?word:current+" "+word;
                if(measure.width(combined)<=width){current=combined;continue;}
                if(!current.isEmpty()){result.add(current);current="";}
                for(int offset=0;offset<word.length();){
                    int next=word.offsetByCodePoints(offset,1);
                    String part=word.substring(offset,next);
                    if(!current.isEmpty()&&measure.width(current+part)>width){result.add(current);current="";}
                    current+=part;offset=next;
                }
            }
            result.add(current);
        }
        return result;
    }
}
