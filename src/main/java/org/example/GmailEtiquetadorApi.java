package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GmailEtiquetadorApi extends JFrame {

    private static final String NOMBRE_APP = "Gmail API Java Quickstart";
    private static final String RUTA_CREDENCIALES = "src/main/resources/credentials.json";
    private static final String NOMBRE_ETIQUETA_DONE = "Done.";
    private static final String NOMBRE_ETIQUETA_WORK_IN_PROGRESS = "Work.in.Progress";
    private static final String NOMBRE_ETIQUETA_TO_BE_DONE = "To.be.Done";

    public GmailEtiquetadorApi() {

        setTitle("App Etiquetas Gmail");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JButton labelButton = new JButton("Etiquetar Correos");
        labelButton.addActionListener(e -> {
            try {
                Gmail service = getGmailService();
                etiquetarEmails(service);
                JOptionPane.showMessageDialog(this, "Etiquetas aplicadas con éxito");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al etiquetar correos: " + ex.getMessage());
            }
        });

        add(labelButton, BorderLayout.CENTER);
    }

    private Gmail getGmailService() throws IOException, GeneralSecurityException {

        File credentialsFile = new File(RUTA_CREDENCIALES);
        Credential credential = AutorizadorGoogle.autorizar(credentialsFile);

        return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(NOMBRE_APP)
                .build();
    }

    private void etiquetarEmails(Gmail service) throws IOException {

        ListMessagesResponse response = service.users().messages().list("me").setQ("in:inbox").setMaxResults(7L).execute();

        if (response.getMessages() == null || response.getMessages().isEmpty()) {
            System.out.println("No hay suficientes correos para etiquetar");
            return;
        }

        List<Message> messages = response.getMessages();
        if (messages.size() < 7) {
            System.out.println("No hay suficientes correos en la bandeja de entrada para etiquetar");
            return;
        }

        Map<String, String> etiquetasMap = getLabelMap(service);
        String idEtiquetaDone = etiquetasMap.get(NOMBRE_ETIQUETA_DONE);
        String idEtiquetaWorkInProgress = etiquetasMap.get(NOMBRE_ETIQUETA_WORK_IN_PROGRESS);
        String idEtiquetaToBeDone = etiquetasMap.get(NOMBRE_ETIQUETA_TO_BE_DONE);

        if (idEtiquetaDone == null || idEtiquetaWorkInProgress == null || idEtiquetaToBeDone == null) {
            System.err.println("No se encontraron todas las etiquetas requeridas");
            return;
        }

        etiquetarMensajes(service, messages.get(0).getId(), idEtiquetaDone);
        etiquetarMensajes(service, messages.get(1).getId(), idEtiquetaDone);
        etiquetarMensajes(service, messages.get(2).getId(), idEtiquetaDone);

        etiquetarMensajes(service, messages.get(3).getId(), idEtiquetaWorkInProgress);

        etiquetarMensajes(service, messages.get(4).getId(), idEtiquetaToBeDone);
        etiquetarMensajes(service, messages.get(5).getId(), idEtiquetaToBeDone);
        etiquetarMensajes(service, messages.get(6).getId(), idEtiquetaToBeDone);
    }

    private void etiquetarMensajes(Gmail service, String messageId, String labelId) throws IOException {

        ModifyMessageRequest mods = new ModifyMessageRequest().setAddLabelIds(Collections.singletonList(labelId));
        service.users().messages().modify("me", messageId, mods).execute();
    }

    private Map<String, String> getLabelMap(Gmail service) throws IOException {

        Map<String, String> labelMap = new HashMap<>();
        ListLabelsResponse listResponse = service.users().labels().list("me").execute();

        if (listResponse.getLabels() != null) {
            for (Label label : listResponse.getLabels()) {
                labelMap.put(label.getName(), label.getId());
            }
        }
        return labelMap;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            GmailEtiquetadorApi app = new GmailEtiquetadorApi();
            app.setVisible(true);
        });
    }
}
