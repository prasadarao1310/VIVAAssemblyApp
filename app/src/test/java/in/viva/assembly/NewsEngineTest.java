package in.viva.assembly;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
import java.util.*;
public class NewsEngineTest {
    private NewsEngine.Item item(String title,String date){return new NewsEngine.Item(title,"Publisher","https://example.com/story",date);}
    @Test public void rssParsesPublisherAndDate() throws Exception{
        String xml="<rss><channel><item><title>Science &amp; learning - Example</title><source>Example</source><link>https://example.com/a</link><pubDate>Thu, 5 Nov 2026 08:00:00 GMT</pubDate></item></channel></rss>";
        List<NewsEngine.Item> list=NewsEngine.parse(xml);assertEquals(1,list.size());assertEquals("Science & learning",list.get(0).title);
        assertEquals("2026-11-05T08:00:00Z",list.get(0).published);
    }
    @Test public void rejectsMissingDateAndUnsafeLink() throws Exception{
        String xml="<rss><channel><item><title>A</title><link>javascript:alert(1)</link><pubDate>Thu, 5 Nov 2026 08:00:00 GMT</pubDate></item><item><title>B</title><link>https://example.com</link></item></channel></rss>";
        assertTrue(NewsEngine.parse(xml).isEmpty());
    }
    @Test(expected=java.io.IOException.class) public void rejectsExternalEntities() throws Exception {
        NewsEngine.parse("<!DOCTYPE rss [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><rss>&x;</rss>");
    }
    @Test public void preventsFutureAndStaleNews(){
        LocalDate d=LocalDate.of(2026,11,5);Instant now=Instant.parse("2026-11-05T10:00:00Z");
        assertTrue(NewsEngine.recent(item("A","2026-11-05T08:00:00Z"),d,now));
        assertFalse(NewsEngine.recent(item("B","2026-11-06T08:00:00Z"),d,now));
        assertFalse(NewsEngine.recent(item("C","2026-10-29T08:00:00Z"),d,now));
        assertFalse(NewsEngine.recent(item("D",""),d,now));
    }
    @Test public void futureAssemblyUsesAvailableNews(){
        assertTrue(NewsEngine.recent(item("A","2026-11-05T08:00:00Z"),LocalDate.of(2026,11,6),Instant.parse("2026-11-05T10:00:00Z")));
    }
    @Test public void removesDuplicateHeadlinesAcrossCategories(){
        List<NewsEngine.Item> in=Arrays.asList(item("Science advances","2026-11-05T08:00:00Z"),item("SCIENCE advances!","2026-11-05T07:00:00Z"),item("New library","2026-11-05T06:00:00Z"));
        Set<String> used=new HashSet<>();assertEquals(2,NewsEngine.choose(new ArrayList<>(in),LocalDate.of(2026,11,5),Instant.parse("2026-11-05T10:00:00Z"),used).size());
        assertEquals(0,NewsEngine.choose(new ArrayList<>(in),LocalDate.of(2026,11,5),Instant.parse("2026-11-05T10:00:00Z"),used).size());
    }
    @Test public void checksSchoolFilter(){
        assertFalse(NewsEngine.suitable("Police investigate murder"));assertFalse(NewsEngine.suitable("New betting app"));
        assertTrue(NewsEngine.suitable("Students open a science exhibition"));
    }
    @Test public void feedWindowDoesNotAskForFutureNews(){
        String url=NewsEngine.feedUrl(0,LocalDate.of(2026,11,8),LocalDate.of(2026,11,5));
        assertTrue(url.contains("2026-11-03"));assertTrue(url.contains("2026-11-06"));
    }
    @Test public void linksRequireHttps(){
        assertTrue(NewsEngine.safeUrl("https://example.com/news"));assertFalse(NewsEngine.safeUrl("file:///tmp/a"));
        assertFalse(NewsEngine.safeUrl("javascript:alert(1)"));assertFalse(NewsEngine.safeUrl("https:evil"));
    }
}
