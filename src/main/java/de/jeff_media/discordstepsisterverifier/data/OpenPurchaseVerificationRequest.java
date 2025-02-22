package de.jeff_media.discordstepsisterverifier.data;

import de.jeff_media.daddy.Stepsister;
import de.jeff_media.discordstepsisterverifier.utils.StepsisterUtils;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class OpenPurchaseVerificationRequest {
    private Integer spigotId;
    private String verificationCode;
    private LinkedHashMap<String,String> verificationEntries;

    public boolean matches() {
        return String.valueOf(spigotId).equals(StepsisterUtils.getEntries(verificationCode).get("UID"));
    }

}
