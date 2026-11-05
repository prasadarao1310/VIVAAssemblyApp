package in.viva.assembly;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class TextWrapTest {
    @Test public void wrapsWithoutDroppingWords(){
        List<String> lines=TextWrap.lines("One two three four",8,s->s.length());
        assertEquals("One two three four",String.join(" ",lines));for(String s:lines)assertTrue(s.length()<=8);
    }
    @Test public void splitsLongTokens(){
        List<String> lines=TextWrap.lines("abcdefghijklmnop",4,s->s.length());
        assertEquals("abcdefghijklmnop",String.join("",lines));assertEquals(4,lines.size());
    }
    @Test public void preservesParagraphs(){assertEquals(Arrays.asList("A","","B"),TextWrap.lines("A\n\nB",20,s->s.length()));}
    @Test public void preservesUnicodeCodePoints(){
        String s="😀😀😀";List<String> lines=TextWrap.lines(s,1,t->t.codePointCount(0,t.length()));
        assertEquals(3,lines.size());assertEquals(s,String.join("",lines));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsZeroWidth(){TextWrap.lines("A",0,s->s.length());}
}
