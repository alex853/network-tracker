package web;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

public class Web {
    public static final DateTimeFormatter df = DateTimeFormat.forPattern("yyyy/MM/dd");
    public static final DateTimeFormatter tf = DateTimeFormat.forPattern("HH:mm");
    public static final DateTimeFormatter urlDf = DateTimeFormat.forPattern("yyyyMMdd");

    public static final DateTimeFormatter hm = DateTimeFormat.forPattern("HH:mm");
}
