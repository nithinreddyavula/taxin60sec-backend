package com.taxin60sec.backend.whatsapp;

import com.taxin60sec.backend.whatsapp.dto.Change;
import com.taxin60sec.backend.whatsapp.dto.Entry;
import com.taxin60sec.backend.whatsapp.dto.Message;
import com.taxin60sec.backend.whatsapp.dto.MetaWebhookRequest;
import com.taxin60sec.backend.whatsapp.dto.Value;
import org.springframework.stereotype.Component;

@Component
public class WhatsappWebhookMapper {

    public WhatsappMessage toWhatsappMessage(MetaWebhookRequest request) {

        if (request == null
                || request.getEntry() == null
                || request.getEntry().isEmpty()) {
            return null;
        }

        Entry entry = request.getEntry().get(0);

        if (entry.getChanges() == null
                || entry.getChanges().isEmpty()) {
            return null;
        }

        Change change = entry.getChanges().get(0);

        Value value = change.getValue();

        if (value == null
                || value.getMessages() == null
                || value.getMessages().isEmpty()) {
            return null;
        }

        Message metaMessage = value.getMessages().get(0);

        WhatsappMessage message = new WhatsappMessage();

        message.setPhoneNumber(metaMessage.getFrom());

        switch (metaMessage.getType()) {

            case "text" -> {

                if (metaMessage.getText() != null) {
                    message.setMessage(
                            metaMessage.getText().getBody()
                    );
                }

            }

            case "document" -> {

                if (metaMessage.getDocument() != null) {

                    message.setMediaId(
                            metaMessage.getDocument().getId()
                    );

                    message.setFileName(
                            metaMessage.getDocument().getFilename()
                    );

                    message.setMimeType(
                            metaMessage.getDocument().getMime_type()
                    );
                }

            }

            case "image" -> {

                if (metaMessage.getImage() != null) {

                    message.setMediaId(
                            metaMessage.getImage().getId()
                    );

                    message.setMimeType(
                            metaMessage.getImage().getMime_type()
                    );

                    message.setFileName("image");
                }

            }

            default -> {
                return null;
            }
        }

        return message;
    }

}