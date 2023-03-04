package com.ianford.podcasts.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * Loads downloaded HTML as JSoup entities for further parsing
 */
@SuppressWarnings("unused")
public class JSoupDocumentLoader implements Function<String, Document> {

    private final Logger logger = LogManager.getLogger();

    @Override
    public Document apply(String filePath) {
        try {
            return Jsoup.parse(new File(filePath), Charset.defaultCharset()
                    .name());
        } catch (IOException e) {
            logger.error("Error parsing file", e);
        }
        return null;
    }

}
