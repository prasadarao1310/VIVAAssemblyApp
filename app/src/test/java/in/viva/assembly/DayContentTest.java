package in.viva.assembly;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
public class DayContentTest{
    @Test public void knownObservancesHaveLongNotes(){
        for(String key:DayContent.INTERNATIONAL.keySet()){
            String note=DayContent.international(LocalDate.parse("2026-"+key));
            assertTrue(key,ObservanceNotes.words(note)>=100);
        }
        for(String key:DayContent.NATIONAL.keySet())assertTrue(key,ObservanceNotes.words(DayContent.national(LocalDate.parse("2026-"+key)))>=100);
    }
    @Test public void missingDateDoesNotInventObservance(){
        String note=DayContent.national(LocalDate.of(2026,11,5));assertTrue(note.startsWith("No Indian"));assertFalse(note.contains("Ayurveda"));
        assertFalse(ObservanceNotes.sufficient(ObservanceNotes.missing(false),ObservanceNotes.missing(true)));
    }
    @Test public void minimumWordCountBoundary(){
        String ninetyNine=String.join(" ",java.util.Collections.nCopies(99,"word"));
        assertFalse(ObservanceNotes.sufficient(ninetyNine,""));assertTrue(ObservanceNotes.sufficient(ninetyNine,"one"));
    }
    @Test public void dayAndWordAreStable(){
        LocalDate d=LocalDate.of(2026,11,5);assertEquals(DayContent.word(d),DayContent.word(d));assertNotEquals(DayContent.word(d),DayContent.word(d.plusDays(1)));
        assertTrue(DayContent.word(d).contains("Pronunciation:"));assertTrue(DayContent.word(d).contains("Meaning:"));
    }
    @Test public void leapDayAndPreEpochDatesWork(){assertFalse(DayContent.word(LocalDate.of(1960,1,1)).isEmpty());assertFalse(DayContent.thought(LocalDate.of(2028,2,29)).isEmpty());}
}
