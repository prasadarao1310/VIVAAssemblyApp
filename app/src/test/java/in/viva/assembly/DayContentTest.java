package in.viva.assembly;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
public class DayContentTest {
    @Test public void matchesKnownDates(){
        assertEquals("World Tsunami Awareness Day",DayContent.international(LocalDate.of(2026,11,5)));
        assertEquals("National Education Day",DayContent.national(LocalDate.of(2026,11,11)));
    }
    @Test public void unknownIsNotFabricated(){assertTrue(DayContent.national(LocalDate.of(2026,11,5)).startsWith("No entry"));}
    @Test public void changesDailyAndIsStable(){
        LocalDate day=LocalDate.of(2026,11,5);assertEquals(DayContent.word(day),DayContent.word(day));
        assertNotEquals(DayContent.word(day),DayContent.word(day.plusDays(1)));assertNotEquals(DayContent.thought(day),DayContent.thought(day.plusDays(1)));
    }
    @Test public void handlesPreEpochAndLeapDay(){
        assertFalse(DayContent.word(LocalDate.of(1960,1,1)).isEmpty());assertFalse(DayContent.thought(LocalDate.of(2028,2,29)).isEmpty());
    }
}
