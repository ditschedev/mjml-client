package dev.ditsche.mjml;

import kong.unirest.Unirest;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.SmtpApi;
import sibModel.*;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author Tobias Dittmann
 */
public class SendinblueMailProvider extends AbstractMailProvider {

    private final SmtpApi sendApi;

    public SendinblueMailProvider(MJMLConfig config, String apiKey) {
        super(config);
        ApiClient apiClient = Configuration.getDefaultApiClient();
        ApiKeyAuth key = (ApiKeyAuth) apiClient.getAuthentication("api-key");
        key.setApiKey(apiKey);

        sendApi = new SmtpApi();
    }

    @Override
    public boolean send(Mail mail) throws IOException {
        SendSmtpEmail email = new SendSmtpEmail();
        email.addToItem(new SendSmtpEmailTo().email(mail.getRecipient().getEmail()).name(mail.getRecipient().getName()));
        email.subject(mail.getSubject());
        email.setSender(new SendSmtpEmailSender().email(config.getFrom()));
        email.setHtmlContent(mjmlToHtml(mail.getMjml()));
        email.setCc(mail.getCC().stream().map(ma -> new SendSmtpEmailCc().email(ma.getEmail()).name(ma.getName())).collect(Collectors.toList()));
        email.setBcc(mail.getBCC().stream().map(ma -> new SendSmtpEmailBcc().email(ma.getEmail()).name(ma.getName())).collect(Collectors.toList()));
        email.setReplyTo(new SendSmtpEmailReplyTo().email(mail.getReplyTo().getEmail()).name(mail.getReplyTo().getName()));

        try {
            sendApi.sendTransacEmail(email);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
            return false;
        }
    }

}
