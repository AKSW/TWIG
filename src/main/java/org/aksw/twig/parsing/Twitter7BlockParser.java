package org.aksw.twig.parsing;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class Twitter7BlockParser implements Callable<Model> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String TWITTER_AUTHORITY = "twitter.com";

    private String lineT;

    private LocalDateTime messageDateTime;

    public LocalDateTime getMessageDateTime() {
        return messageDateTime;
    }

    private String lineU;

    private String twitterUserName;

    public String getTwitterUserName() {
        return twitterUserName;
    }

    private String lineW;

    private String messageContent;

    public String getMessageContent() {
        return messageContent;
    }

    public Twitter7BlockParser(Triple<String, String, String> twitter7Triple) {
        this.lineT = twitter7Triple.getLeft();
        this.lineU = twitter7Triple.getMiddle();
        this.lineW = twitter7Triple.getRight();
    }

    public Model call() throws Twitter7BlockParseException {

        // Parse date and time
        try {
            this.messageDateTime = LocalDateTime.from(DATE_TIME_FORMATTER.parse(this.lineT.trim()));
        } catch (DateTimeException e) {
            throw new Twitter7BlockParseException(Twitter7BlockParseException.Error.DATETIME_MALFORMED);
        }

        // Parse user name
        try {
            URL twitterUrl = new URL(this.lineU);
            if (!twitterUrl.getAuthority().equalsIgnoreCase(TWITTER_AUTHORITY)) {
                throw new Twitter7BlockParseException(Twitter7BlockParseException.Error.NO_TWITTER_LINK);
            }

            this.twitterUserName = Arrays.stream(twitterUrl.getPath().split("/"))
                    .filter(pathPart -> !pathPart.isEmpty())
                    .findFirst()
                    .orElse(null);

            if (this.twitterUserName == null) {
                throw new Twitter7BlockParseException(Twitter7BlockParseException.Error.NO_TWITTER_ACCOUNT);
            }
        } catch (MalformedURLException e) {
            throw new Twitter7BlockParseException(Twitter7BlockParseException.Error.URL_MALFORMED);
        }

        // Parse message content
        this.messageContent = this.lineW.trim();

        Model model = ModelFactory.createDefaultModel();

        return model;
    }
}
