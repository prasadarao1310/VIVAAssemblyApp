package in.viva.assembly;

import java.time.*;
import java.util.*;

final class DayContent {
    static final ZoneId ZONE=ZoneId.of("Asia/Kolkata");
    static LocalDate today(){return LocalDate.now(ZONE);}
    static final String UN="https://www.un.org/en/observances/list-days-weeks";
    static final Map<String,String> INTERNATIONAL=new LinkedHashMap<>();
    static final Map<String,String[]> NATIONAL=new LinkedHashMap<>();
    static {
        INTERNATIONAL.put("01-24","International Day of Education");
        INTERNATIONAL.put("02-11","International Day of Women and Girls in Science");
        INTERNATIONAL.put("02-21","International Mother Language Day");
        INTERNATIONAL.put("03-08","International Women's Day");
        INTERNATIONAL.put("03-20","International Day of Happiness");
        INTERNATIONAL.put("03-21","International Day of Forests");
        INTERNATIONAL.put("03-22","World Water Day");
        INTERNATIONAL.put("04-02","World Autism Awareness Day");
        INTERNATIONAL.put("04-07","World Health Day");
        INTERNATIONAL.put("04-22","International Mother Earth Day");
        INTERNATIONAL.put("04-23","World Book and Copyright Day");
        INTERNATIONAL.put("05-15","International Day of Families");
        INTERNATIONAL.put("06-05","World Environment Day");
        INTERNATIONAL.put("06-08","World Oceans Day");
        INTERNATIONAL.put("06-21","International Day of Yoga");
        INTERNATIONAL.put("07-30","International Day of Friendship");
        INTERNATIONAL.put("08-12","International Youth Day");
        INTERNATIONAL.put("09-08","International Literacy Day");
        INTERNATIONAL.put("09-16","International Day for the Preservation of the Ozone Layer");
        INTERNATIONAL.put("09-21","International Day of Peace");
        INTERNATIONAL.put("10-02","International Day of Non-Violence");
        INTERNATIONAL.put("10-05","World Teachers' Day");
        INTERNATIONAL.put("10-16","World Food Day");
        INTERNATIONAL.put("10-24","United Nations Day");
        INTERNATIONAL.put("11-05","World Tsunami Awareness Day");
        INTERNATIONAL.put("11-06","International Day for Preventing the Exploitation of the Environment in War and Armed Conflict");
        INTERNATIONAL.put("11-10","World Science Day for Peace and Development");
        INTERNATIONAL.put("11-20","World Children's Day");
        INTERNATIONAL.put("12-03","International Day of Persons with Disabilities");
        INTERNATIONAL.put("12-10","Human Rights Day");
        NATIONAL.put("01-26",new String[]{"Republic Day","https://knowindia.india.gov.in/national-identity-elements/national-days.php"});
        NATIONAL.put("02-28",new String[]{"National Science Day","https://www.ursc.gov.in/nsd.jsp"});
        NATIONAL.put("08-15",new String[]{"Independence Day","https://knowindia.india.gov.in/national-identity-elements/national-days.php"});
        NATIONAL.put("10-02",new String[]{"Gandhi Jayanti","https://knowindia.india.gov.in/national-identity-elements/national-days.php"});
        NATIONAL.put("11-11",new String[]{"National Education Day","https://www.pib.gov.in/Pressreleaseshare.aspx?PRID=2072203"});
    }
    static String international(LocalDate d){return INTERNATIONAL.getOrDefault(d.toString().substring(5),"No entry in the starter calendar. Check an official calendar or add a verified observance.");}
    static String national(LocalDate d){String[] v=NATIONAL.get(d.toString().substring(5));return v==null?"No entry in the starter calendar. Check an official calendar or add a verified observance.":v[0];}
    static String nationalSource(LocalDate d){String[] v=NATIONAL.get(d.toString().substring(5));return v==null?"":v[1];}
    static final String[][] WORDS={
        {"Resilience","The ability to recover after difficulties.","Our resilience helped us try again after the experiment failed."},
        {"Curiosity","A strong desire to learn or understand.","Curiosity encourages us to ask thoughtful questions."},
        {"Integrity","Being honest and following good principles.","She showed integrity by admitting her mistake."},
        {"Empathy","Understanding another person's feelings.","Empathy helps us support a classmate who feels left out."},
        {"Perseverance","Continuing despite difficulty.","His perseverance helped him master the difficult passage."},
        {"Gratitude","A feeling of thankfulness.","We expressed gratitude to the staff who keep our school clean."},
        {"Initiative","The willingness to take the first useful step.","She took the initiative to organise the books."},
        {"Diligence","Careful and steady effort.","Diligence makes our work more accurate."},
        {"Cooperation","Working together towards a shared goal.","Cooperation helped our team finish the model."},
        {"Adaptability","The ability to adjust to new situations.","Adaptability helped us change our plan when it rained."},
        {"Compassion","Concern for others and a desire to help.","We showed compassion by helping an injured animal."},
        {"Clarity","The quality of being easy to understand.","Clarity makes a presentation easier to follow."},
        {"Resourceful","Able to find practical ways to solve problems.","The resourceful team repaired its model with spare parts."},
        {"Mindful","Paying careful attention to the present moment.","Be mindful of others when you speak."},
        {"Inclusive","Welcoming people and allowing them to participate.","An inclusive classroom gives everyone a voice."},
        {"Discernment","The ability to judge carefully.","Use discernment before sharing a news story."},
        {"Optimism","Hope that good outcomes are possible.","Optimism helped us approach the challenge calmly."},
        {"Accountability","Taking responsibility for one's actions.","Accountability means keeping the promises we make."},
        {"Innovate","To develop a new idea or method.","Students can innovate to solve everyday problems."},
        {"Sustainable","Able to continue without exhausting resources.","Using water carefully is a sustainable habit."},
        {"Perspective","A particular way of viewing something.","Listening to others can broaden our perspective."},
        {"Patience","The ability to wait or work calmly.","Growing a plant teaches us patience."},
        {"Respect","Treating others and their rights with care.","We show respect by listening without interrupting."},
        {"Dedication","Strong commitment to a task or purpose.","Her dedication improved the whole team's performance."},
        {"Prudent","Careful and sensible when making decisions.","It is prudent to check the weather before a trip."},
        {"Courage","The ability to act despite fear.","It takes courage to stand up for someone."},
        {"Observe","To notice and examine carefully.","Observe the changes in the plant each morning."},
        {"Reflect","To think carefully about an experience.","Reflect on what helped you learn today."},
        {"Aspire","To hope and work towards a goal.","We aspire to make our community cleaner."},
        {"Contribute","To give effort or resources towards a shared purpose.","Everyone can contribute to a successful assembly."},
        {"Humility","Recognising that we can still learn from others.","Humility helps us accept useful feedback."}
    };
    static final String[] THOUGHTS={
        "A small act of kindness can improve someone's whole day.",
        "Ask one good question today, and let it lead you to learning.",
        "Progress begins when you try again with a better plan.",
        "Listen to understand, not only to prepare your reply.",
        "Your effort today creates possibilities for tomorrow.",
        "Honesty builds trust one decision at a time.",
        "Make room for someone else's idea.",
        "A mistake can become a lesson when you reflect on it.",
        "Take care of shared spaces; they belong to all of us.",
        "You do not need to be first to make a valuable contribution.",
        "Be curious enough to ask and patient enough to practise.",
        "Celebrate improvement, including the improvement you cannot yet see.",
        "Choose words that help others feel respected.",
        "Do the useful thing you can do right now.",
        "Good teamwork gives every person a chance to contribute.",
        "Courage can be as quiet as admitting that you need help.",
        "Use what you learn to make life better for someone.",
        "Careful observation is the beginning of discovery.",
        "Respect grows when we make an effort to understand differences.",
        "Let your actions make your promises believable.",
        "A thoughtful pause can lead to a wiser decision.",
        "Learning becomes stronger when we share it.",
        "Be gentle with others and responsible for yourself.",
        "Look for a solution before giving up on a problem.",
        "Gratitude helps us notice the support around us.",
        "A new skill begins with a first attempt.",
        "Protect the resources that tomorrow's children will need.",
        "You can change your approach without giving up your goal.",
        "Every person you meet knows something you can learn.",
        "Make today a little kinder than yesterday.",
        "Finish the day knowing you made an honest effort."
    };
    static int index(LocalDate d,int length){return (int)Math.floorMod(d.toEpochDay(),(long)length);}
    static String word(LocalDate d){String[] w=WORDS[index(d,WORDS.length)];return w[0]+" — "+w[1]+"\nExample: "+w[2];}
    static String thought(LocalDate d){return THOUGHTS[index(d,THOUGHTS.length)];}
}
