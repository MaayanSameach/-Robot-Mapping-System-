package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.ErrorObject;

public class CrashedBroadcast<T> implements Broadcast {
    private String senderID;
    private String errorMessage;

    public CrashedBroadcast(String senderID, String errorMessage) {
        this.senderID = senderID;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    public String getSenderID() {
        return senderID;
    }

    public String toString() {
        return "Crashed [sender:" + senderID + ", errorMessage:" + errorMessage + "]";
    }

}
