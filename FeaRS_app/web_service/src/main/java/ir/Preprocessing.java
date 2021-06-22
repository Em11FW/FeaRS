package ir;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class Preprocessing {
    private static Pattern pattern_nonAlphabetic = Pattern.compile("[^a-zA-Z _]");
    private static Pattern pattern_whiteSpace = Pattern.compile("\\s+");
    private static Pattern pattern_newLines = Pattern.compile("[\\t\\n\\r]+");
    private static Pattern pattern_oneChar = Pattern.compile("\\b[a-zA-Z]{1,1}\\b");
    private static Pattern pattern_camelCase = Pattern.compile(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"));
    public HashSet<String> androidClasses = null;
    public HashSet<String> androidAPIs = null;
    public HashSet<String> androidConstants = null;
    private HashSet<String> engStopword = null;
    private HashSet<String> javaStopword = null;
    private boolean considerShortDescription;
    private boolean considerLongDescription;
    private boolean applyStemming;

    public Preprocessing(boolean considerShortDescription, boolean considerLongDescription, boolean applyStemming, String englishStopwordPath, String javaStopwordPath, String androidAPIPath, String androidConstantsPath) throws IOException {
        this.considerShortDescription = considerShortDescription;
        this.considerLongDescription = considerLongDescription;
        this.applyStemming = applyStemming;
        this.engStopword = new HashSet();
        this.javaStopword = new HashSet();
        BufferedReader br;
        String line;
        if (englishStopwordPath != null) {
            br = new BufferedReader(new FileReader(englishStopwordPath));

            try {
                while((line = br.readLine()) != null) {
                    this.engStopword.add(line);
                }
            } catch (Throwable var18) {
                try {
                    br.close();
                } catch (Throwable var11) {
                    var18.addSuppressed(var11);
                }

                throw var18;
            }

            br.close();
        }

        if (javaStopwordPath != null) {
            br = new BufferedReader(new FileReader(javaStopwordPath));

            try {
                while((line = br.readLine()) != null) {
                    this.javaStopword.add(line);
                }
            } catch (Throwable var17) {
                try {
                    br.close();
                } catch (Throwable var13) {
                    var17.addSuppressed(var13);
                }

                throw var17;
            }

            br.close();
        }

        this.androidClasses = new HashSet();
        this.androidAPIs = new HashSet();
        br = new BufferedReader(new FileReader(androidAPIPath));

        try {
            while((line = br.readLine()) != null) {
                if (!line.startsWith("ID")) {
                    String[] tokens = line.split(",");
                    if (tokens.length >= 8 && tokens[2].contains("android")) {
                        this.androidClasses.add(tokens[4]);
                        this.androidAPIs.add(tokens[7]);
                    }
                }
            }
        } catch (Throwable var16) {
            try {
                br.close();
            } catch (Throwable var14) {
                var16.addSuppressed(var14);
            }

            throw var16;
        }

        br.close();
        this.androidConstants = new HashSet();
        br = new BufferedReader(new FileReader(androidConstantsPath));

        try {
            while((line = br.readLine()) != null) {
                this.androidConstants.add(line);
            }
        } catch (Throwable var15) {
            try {
                br.close();
            } catch (Throwable var12) {
                var15.addSuppressed(var12);
            }

            throw var15;
        }

        br.close();
    }



    public String CleanText(String text) {
        String cleanedString = pattern_nonAlphabetic.matcher(text).replaceAll(" ");
        cleanedString = pattern_newLines.matcher(cleanedString).replaceAll(" ");
        cleanedString = pattern_oneChar.matcher(cleanedString).replaceAll(" ");
        cleanedString = pattern_whiteSpace.matcher(cleanedString).replaceAll(" ");
        return cleanedString;
    }

    public String[] SplitForAndroidSim(String cleanedText) {
        return cleanedText.split(" ");
    }

    public Map<String, Double> SplitAndTokenize(String cleanedText) {
        Map<String, Double> toReturn = new HashMap();
        cleanedText = cleanedText.replaceAll("_", " ");
        cleanedText = this.splitCamelCase(cleanedText);
        cleanedText = pattern_oneChar.matcher(cleanedText).replaceAll(" ");
        String[] tokens = cleanedText.split(" ");
        String[] var4 = tokens;
        int var5 = tokens.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String token = var4[var6];
            if (!token.isEmpty() && !token.equals(" ")) {
                if (!javaStopword.contains(token.toLowerCase())) {
                    if (!toReturn.containsKey(token)) {
                        toReturn.put(token, 1.0D);
                    } else {
                        toReturn.put(token, (Double) toReturn.get(token) + 1.0D);
                    }
                }
                else {
                    if (!toReturn.containsKey(token)) {
                        toReturn.put(token, 1.5D);
                    } else {
                        toReturn.put(token, (Double) toReturn.get(token) + 1.5D);
                    }
                }
            }
        }

        return toReturn;
    }

    private String splitCamelCase(String text) {
        String t = pattern_camelCase.matcher(text).replaceAll(" ");
        return t;
    }
}
