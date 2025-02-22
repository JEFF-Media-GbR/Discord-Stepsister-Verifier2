package de.jeff_media.discordstepsisterverifier.utils;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

import static de.jeff_media.daddy.Stepsister.PREFIX;
import static de.jeff_media.daddy.Stepsister.SUFFIX;

public class StepsisterUtils {

public static class CouldNotDownloadException extends IOException {
    public CouldNotDownloadException(Throwable reason) {
        super(reason);
    }

}

    public static String extractVerificationCode(File file) {
        try (InputStream inputStream = new FileInputStream(file); InputStreamReader inputStreamReader = new InputStreamReader(inputStream); BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            ArrayList<String> lines = new ArrayList<>();

            String inOneLine = FileUtils.inOneLine(file);
            if (inOneLine != null) {
                if (inOneLine.contains(PREFIX) && inOneLine.contains(SUFFIX)) {
                    return inOneLine.split(PREFIX)[1].split(SUFFIX)[0];
                }
            }

            boolean collect = false;
            for (String line : bufferedReader.lines().toList()) {
                if (line.contains(PREFIX) && line.contains(SUFFIX)) {
                    return line.split(PREFIX)[1].split(SUFFIX)[0];
                }
                if (line.startsWith("Verification Code: ") || line.startsWith("<pre>Verification Code: ")) {
                    line = line.replace("<pre>", "").replace("Verification Code: ", "");
                    collect = true;
                }
                if (line.endsWith("</pre>")) {
                    line = line.replace("</pre>", "");
                    lines.add(line);
                    collect = false;
                }
                if (collect) {
                    lines.add(line);
                }
            }
            return String.join("", lines);
        } catch (IOException e) {
            return null;
        }

    }

    public static boolean isPossibleVerificationFile(Message.Attachment attachment) {
        if (attachment.getFileName().toLowerCase(Locale.ROOT).contains("discord-verification") && attachment.getFileName().toLowerCase(Locale.ROOT).contains(".html"))
            return true;
        if (attachment.getFileName().toLowerCase(Locale.ROOT).contains("message.txt")) return true;
        return false;
    }




    public static @Nonnull VerificationResult getVerificationResult(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String rawMessage = message.getContentRaw().replace("\n","");
        String verificationCode = null;
        if(rawMessage.contains(PREFIX) && rawMessage.contains(SUFFIX)) {
            String[] split = rawMessage.split(PREFIX);
            if (split.length > 1) {
                String code = split[1].split(SUFFIX)[0];
                verificationCode = code.replace("\n", "");
            }
        }
        if(verificationCode == null) {
            for (Message.Attachment attachment : message.getAttachments()) {
                if (attachment.isImage()) {
                    continue;
                }
                if (attachment.isVideo()) {
                    continue;
                }
                if (!isPossibleVerificationFile(attachment)) {
                    continue;
                }

                try {
                    CompletableFuture<File> fileCompletableFuture = attachment.downloadToFile(File.createTempFile("stepsister", null));
                    File file = null;
                    try {
                        file = fileCompletableFuture.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (file == null) {
                        return VerificationResult.COULD_NOT_DOWNLOAD;
                    }

                    verificationCode = StepsisterUtils.extractVerificationCode(file);
                    file.delete();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    return VerificationResult.COULD_NOT_DOWNLOAD;
                }
            }
        }
        if (verificationCode != null && verificationCode.length() != 0) {
            LinkedHashMap map = StepsisterUtils.getEntries(verificationCode);

            if (map == null || map.isEmpty()) {
                return VerificationResult.NO_VERIFICATION_CODE;
            }
            return new VerificationResult() {
                @Override
                public LinkedHashMap<String, String> getVerificationEntries() {
                    return map;
                }
            };
        } else {
            return VerificationResult.NO_VERIFICATION_CODE;
        }
    }

    public static LinkedHashMap<String, String> getEntries(String verificationCode) {
        String informationSet = getInformationSet(verificationCode);
        //System.out.println("ee");
        LinkedHashMap<String, String> map = new Gson().fromJson(StringUtils.reverse(informationSet), LinkedHashMap.class);
        //System.out.println("gsoned map");
        return map;
    }

    private static String getInformationSet(String base64) {
        String shuffledVerificationCode = null;
        try {
            shuffledVerificationCode = new String(decompress(Base64.getDecoder().decode(base64)).getBytes());
        } catch (Throwable ioException) {
            ioException.printStackTrace();
            return null;
        }
        String keyLeft = shuffledVerificationCode.substring(0, 6);
        String keyRight = shuffledVerificationCode.substring(shuffledVerificationCode.length() - 5);
        int key = Integer.valueOf(keyLeft + keyRight);
        String verificationCode = deShuffle(shuffledVerificationCode.substring(6, shuffledVerificationCode.length() - 5), key);
        String additionalInformation = verificationCode;
        return additionalInformation;
    }

    public static String decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        return sb.toString();
    }

    private static String deShuffle(String shuffled, int key) {
        int size = shuffled.length();
        char[] chars = shuffled.toCharArray();
        int[] exchanges = getShuffleExchanges(size, key);
        for (int i = 1; i < size; i++) {
            int n = exchanges[size - i - 1];
            char tmp = chars[i];
            chars[i] = chars[n];
            chars[n] = tmp;
        }
        return new String(chars);
    }

    private static int[] getShuffleExchanges(int size, int key) {
        int[] exchanges = new int[size - 1];
        Random rand = new Random(key);
        for (int i = size - 1; i > 0; i--) {
            int n = rand.nextInt(i + 1);
            exchanges[size - 1 - i] = n;
        }
        return exchanges;
    }

    public static class VerificationResult {
        public static VerificationResult NO_VERIFICATION_CODE = new VerificationResult();
        public static VerificationResult COULD_NOT_DOWNLOAD = new VerificationResult();
        public static VerificationResult INVALID_VERIFICATION_CODE = new VerificationResult();

        public LinkedHashMap<String, String> getVerificationEntries() {
            return null;
        }
    }

}
