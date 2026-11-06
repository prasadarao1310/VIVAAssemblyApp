package in.viva.assembly;

import java.time.*;
import java.time.format.*;
import java.util.*;

final class ObservanceNotes {
    static final Map<String,String> TOPICS=new HashMap<>();
    static {
        TOPICS.put("01-24","fair access to education and the ways learning can improve individual lives and communities");
        TOPICS.put("02-11","the contributions of women and girls in science and the importance of equal learning opportunities");
        TOPICS.put("02-21","the value of mother languages, multilingual learning and respect for linguistic diversity");
        TOPICS.put("03-08","equal opportunity, respect and the contributions of women in every area of life");
        TOPICS.put("03-20","wellbeing, friendship, kindness and the conditions that help people live fulfilling lives");
        TOPICS.put("03-21","forests, biodiversity and the connections between trees, water, wildlife and human communities");
        TOPICS.put("03-22","clean water, careful water use and the importance of protecting shared water resources");
        TOPICS.put("04-02","autism awareness, inclusion and respectful support for different ways of learning and communicating");
        TOPICS.put("04-07","health education, access to care and the importance of seeking reliable health information");
        TOPICS.put("04-22","environmental responsibility and the links between ecosystems and everyday human activities");
        TOPICS.put("04-23","reading, creative expression, authorship and respect for the work of writers");
        TOPICS.put("05-15","families, care, shared responsibilities and the different kinds of support people need");
        TOPICS.put("06-05","environmental protection, waste reduction and practical choices that support healthier surroundings");
        TOPICS.put("06-08","ocean ecosystems and the connections between inland communities, rivers and the sea");
        TOPICS.put("06-21","the cultural significance of yoga and the value of learning about wellbeing from qualified sources");
        TOPICS.put("07-30","friendship, mutual understanding and the ways respectful relationships strengthen a community");
        TOPICS.put("08-12","young people's ideas, participation and opportunities to contribute to society");
        TOPICS.put("09-08","literacy and the role reading and writing play in learning, communication and participation");
        TOPICS.put("09-16","the ozone layer and how scientific understanding can guide environmental cooperation");
        TOPICS.put("09-21","peace, dialogue and constructive ways to address disagreements");
        TOPICS.put("10-02","nonviolence, responsible action and the importance of resolving disagreements without harm");
        TOPICS.put("10-05","teachers' contributions and the conditions that support good teaching and learning");
        TOPICS.put("10-16","food, agriculture, access to nourishment and the need to reduce avoidable food waste");
        TOPICS.put("10-24","international cooperation and the work of the United Nations");
        TOPICS.put("11-05","tsunami awareness, official early-warning information and the importance of community preparedness");
        TOPICS.put("11-06","the environmental consequences of armed conflict and the need to protect natural resources");
        TOPICS.put("11-10","the connection between science, peace, development and informed public discussion");
        TOPICS.put("11-20","children's rights, wellbeing and meaningful opportunities to learn and participate");
        TOPICS.put("12-03","accessibility, inclusion and the rights and participation of persons with disabilities");
        TOPICS.put("12-10","human rights, dignity and equal respect for every person");
    }
    static int words(String text){
        String trimmed=text.trim();
        return trimmed.isEmpty()?0:trimmed.split("\\s+").length;
    }
    static boolean sufficient(String international,String national){return words(international+" "+national)>=100;}
    static String missing(boolean national){
        return "No "+(national?"Indian national":"international")+" observance has been verified for this date in the app's limited calendar. This does not establish that none exists. Please add a significance note only after checking an official source. Do not substitute an observance from a different date.";
    }
    static String international(LocalDate day){
        String key=day.toString().substring(5),name=DayContent.INTERNATIONAL.get(key);
        if(name==null)return missing(false);
        return note(day,name,TOPICS.getOrDefault(key,"the meaning of this observance and its relevance to our community"),false);
    }
    static String national(LocalDate day){
        String[] entry=DayContent.NATIONAL.get(day.toString().substring(5));
        if(entry==null)return missing(true);
        String topic;
        switch(day.toString().substring(5)){
            case "01-26":topic="India's Constitution, democratic institutions and responsible citizenship";break;
            case "02-28":topic="scientific curiosity, careful observation and the importance of evidence";break;
            case "08-15":topic="India's independence and our shared responsibilities as citizens";break;
            case "08-29":topic="sport, teamwork, fair play and respect for the dedication of athletes";break;
            case "09-05":topic="teachers' contributions and the habits that make a classroom a supportive place to learn";break;
            case "10-02":topic="Mahatma Gandhi's life and discussion of honesty, nonviolence and service";break;
            case "11-11":topic="the value of education and Maulana Abul Kalam Azad's contribution to public life";break;
            case "11-14":topic="children's wellbeing, education and the need to listen to their ideas";break;
            case "11-26":topic="India's Constitution, its principles and the relationship between rights and responsibilities";break;
            default:topic="mathematics, Srinivasa Ramanujan's contribution and the value of exploring patterns";
        }
        return note(day,entry[0],topic,true);
    }
    private static String note(LocalDate date,String name,String topic,boolean national){
        return date.format(DateTimeFormatter.ofPattern("d MMMM",Locale.ENGLISH))+" is observed "+(national?"in India ":"")+"as "+name+". "
            +"Students can use this occasion to explore "+topic+". "
            +"An observance is most useful when we move beyond remembering its name and consider what we can learn from it. "
            +"In our assembly, let us connect this subject with something we have studied, an experience in our community, or a question we would like to investigate. "
            +"We can listen carefully to different perspectives, check information before sharing it, and discuss one practical action appropriate for our age. "
            +"During the day, each class can choose a relevant question, find an answer using a trustworthy source, and share a short reflection. "
            +"The aim is thoughtful participation and responsible learning, rather than a celebration without understanding.";
    }
}
