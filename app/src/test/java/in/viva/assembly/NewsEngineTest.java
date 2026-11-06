package in.viva.assembly;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
import java.util.*;
public class NewsEngineTest {
    final LocalDate assembly=LocalDate.of(2026,11,5);
    final Instant now=Instant.parse("2026-11-05T10:00:00Z");
    private NewsEngine.Item item(String title,String date){return new NewsEngine.Item(title,"The Hindu","https://www.thehindu.com/news/story",date);}
    @Test public void rssRetainsPublisherDomain() throws Exception{
        String xml="<rss><channel><item><title>Science &amp; learning - The Hindu</title><source url='https://www.thehindu.com'>The Hindu</source><link>https://news.google.com/rss/articles/test</link><pubDate>Wed, 4 Nov 2026 08:00:00 GMT</pubDate></item></channel></rss>";
        NewsEngine.Item i=NewsEngine.parse(xml).get(0);assertEquals("Science & learning",i.title);assertTrue(NewsEngine.allowed(i,0));
    }
    @Test public void rejectsMissingDateAndUnsafeLink() throws Exception{
        assertTrue(NewsEngine.parse("<rss><channel><item><title>A</title><link>javascript:alert(1)</link><pubDate>Wed, 4 Nov 2026 08:00:00 GMT</pubDate></item></channel></rss>").isEmpty());
    }
    @Test(expected=java.io.IOException.class) public void rejectsExternalEntities() throws Exception{
        NewsEngine.parse("<!DOCTYPE rss [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><rss>&x;</rss>");
    }
    @Test public void acceptsExactPreviousIstDay(){
        assertTrue(NewsEngine.recent(item("A","2026-11-03T18:30:00Z"),assembly,now));
        assertTrue(NewsEngine.recent(item("B","2026-11-04T18:29:59Z"),assembly,now));
    }
    @Test public void excludesAdjacentIstDays(){
        assertFalse(NewsEngine.recent(item("A","2026-11-03T18:29:59Z"),assembly,now));
        assertFalse(NewsEngine.recent(item("B","2026-11-04T18:30:00Z"),assembly,now));
        assertFalse(NewsEngine.recent(item("C","2026-11-02T12:00:00Z"),assembly,now));
    }
    @Test public void doesNotSubstituteForFutureAssembly(){
        assertFalse(NewsEngine.recent(item("A","2026-11-04T08:00:00Z"),LocalDate.of(2026,11,7),now));
        assertFalse(NewsEngine.recent(item("B","2026-11-06T08:00:00Z"),LocalDate.of(2026,11,7),now));
    }
    @Test public void yearAndLeapDayBoundaries(){
        assertTrue(NewsEngine.recent(item("A","2026-12-31T10:00:00Z"),LocalDate.of(2027,1,1),Instant.parse("2027-01-01T12:00:00Z")));
        assertTrue(NewsEngine.recent(item("A","2028-02-29T10:00:00Z"),LocalDate.of(2028,3,1),Instant.parse("2028-03-01T12:00:00Z")));
    }
    @Test public void noDateOrInvalidDateIsRejected(){assertFalse(NewsEngine.recent(item("A",""),assembly,now));assertFalse(NewsEngine.recent(item("A","yesterday"),assembly,now));}
    @Test public void publisherNamesAloneAreNotTrusted(){
        NewsEngine.Item i=new NewsEngine.Item("A","The Hindu","https://evil.test/story","2026-11-04T08:00:00Z","https://thehindu.com");
        assertFalse(NewsEngine.allowed(i,0));i.url="https://thehindu.com.evil.test/story";assertFalse(NewsEngine.allowed(i,0));
        i.url="https://notindianexpress.com/a";assertFalse(NewsEngine.allowed(i,0));
    }
    @Test public void eachCategoryUsesOnlyApprovedDomains(){
        for(String domain:new String[]{"indianexpress.com","www.thehindu.com"}){
            NewsEngine.Item i=item("A","2026-11-04T08:00:00Z");i.url="https://"+domain+"/a";
            assertTrue(NewsEngine.allowed(i,0));assertTrue(NewsEngine.allowed(i,1));assertTrue(NewsEngine.allowed(i,3));assertFalse(NewsEngine.allowed(i,2));
        }
        for(String domain:new String[]{"eenadu.net","www.andhrajyothy.com","web.sakshi.com"}){
            NewsEngine.Item i=item("A","2026-11-04T08:00:00Z");i.url="https://"+domain+"/a";
            assertTrue(NewsEngine.allowed(i,2));assertFalse(NewsEngine.allowed(i,0));
        }
    }
    @Test public void googleLinkRequiresApprovedMetadata(){
        NewsEngine.Item i=new NewsEngine.Item("A","The Hindu","https://news.google.com/rss/articles/abc","2026-11-04T08:00:00Z","");
        assertFalse(NewsEngine.allowed(i,0));i.publisherUrl="http://www.thehindu.com";assertTrue(NewsEngine.allowed(i,0));
    }
    @Test public void ignoresOldAndUnapprovedCandidates(){
        NewsEngine.Item bad=item("B","2026-11-04T08:00:00Z");bad.url="https://example.com/a";
        List<NewsEngine.Item> list=new ArrayList<>(Arrays.asList(item("A","2026-11-03T08:00:00Z"),bad,item("C","2026-11-04T07:00:00Z")));
        assertEquals(1,NewsEngine.choose(list,0,assembly,now,new HashSet<>()).size());
    }
    @Test public void removesDuplicates(){
        List<NewsEngine.Item> in=Arrays.asList(item("Science advances","2026-11-04T08:00:00Z"),item("SCIENCE advances!","2026-11-04T07:00:00Z"),item("New library","2026-11-04T06:00:00Z"));
        Set<String> used=new HashSet<>();assertEquals(2,NewsEngine.choose(new ArrayList<>(in),0,assembly,now,used).size());
        assertEquals(0,NewsEngine.choose(new ArrayList<>(in),1,assembly,now,used).size());
    }
    @Test public void filtersUnsafeTopics(){assertFalse(NewsEngine.suitable("Police investigate murder"));assertTrue(NewsEngine.suitable("Students open science exhibition"));}
    @Test public void stateFeedUsesTeluguEdition(){assertTrue(NewsEngine.feedUrl(2,assembly,assembly).contains("ceid=IN:te"));}
    @Test public void linksRequireSafeHttps(){assertFalse(NewsEngine.safeUrl("https://user@thehindu.com/a"));assertFalse(NewsEngine.safeUrl("file:///tmp/a"));}
}
