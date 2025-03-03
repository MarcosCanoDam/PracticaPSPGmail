package org.example;

import javax.mail.*;
import java.util.Properties;


public class GmailEtiquetadorJavaMail {

    private static final String EMAIL = "marcoscanopsp@gmail.com";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.host", "imap.gmail.com");
            properties.put("mail.imaps.port", "993");
            properties.put("mail.imaps.ssl.enable", "true");

            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore("imaps");
            store.connect("imap.gmail.com", EMAIL, PASSWORD);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages(Math.max(1, inbox.getMessageCount() - 6), inbox.getMessageCount());

            if (messages.length < 7) {
                System.out.println("No hay suficientes correos para etiquetar");
                return;
            }

            etiquetarCorreos(store, messages[0], "To.be.Done");
            etiquetarCorreos(store, messages[1], "To.be.Done");
            etiquetarCorreos(store, messages[2], "To.be.Done");
            etiquetarCorreos(store, messages[3], "Work.in.Progress");
            etiquetarCorreos(store, messages[4], "Done");
            etiquetarCorreos(store, messages[5], "Done");
            etiquetarCorreos(store, messages[6], "Done");

            inbox.close(false);
            store.close();

            System.out.println("Correos etiquetados correctamente");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void etiquetarCorreos(Store store, Message message, String label) throws MessagingException {

        Folder carpetaDestino = store.getFolder("[Gmail]/" + label);

        if (!carpetaDestino.exists()) {
            System.out.println("La etiqueta '" + label + "' no existe. Creándola...");
            carpetaDestino.create(Folder.HOLDS_MESSAGES);
        }
        carpetaDestino.open(Folder.READ_WRITE);

        Message[] msgs = new Message[]{message};
        carpetaDestino.appendMessages(msgs);

        carpetaDestino.close(false);
        System.out.println("Correo movido a '" + label + "'.");
    }
}
