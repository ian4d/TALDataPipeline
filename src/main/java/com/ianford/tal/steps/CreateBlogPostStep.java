package com.ianford.tal.steps;

import com.github.slugify.Slugify;
import com.ianford.podcasts.model.ParsedEpisode;
import com.ianford.podcasts.model.jekyll.BlogEpisode;
import com.ianford.tal.model.PipelineConfig;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateBlogPostStep implements PipelineStep {
    @Override
    public void run(PipelineConfig pipelineConfig) throws IOException {
        // Load post template
        String postTemplate = null;
        try {
            postTemplate = loadPostTemplate();

            for (ParsedEpisode parsedEpisode : pipelineConfig.getParsedEpisodes()) {
                Map<Integer, BlogEpisode> episodeMap = parsedEpisode.getEpisodeMap();
                for (BlogEpisode episode : episodeMap.values()) {
                    String postContent = buildPostContents(postTemplate,
                            episode);

                    // Write file to _posts directory
                    String newPostFilename = writePostToLocalRepository(
                            pipelineConfig.getWorkingDirectory()
                                    .resolve(pipelineConfig.getLocalPostsDirectory()),
                            episode.getEpisodeTitle(),
                            postContent);
                }
            }


        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Loads the template being used for generating new Jekyll posts.
     *
     * @return String body of the template
     *
     * @throws IOException        Thrown if template can't be loaded
     * @throws URISyntaxException Thrown if URI of template is invalid
     */
    private String loadPostTemplate() throws IOException, URISyntaxException {
        return Files.readAllLines(Path.of(this.getClass()
                        .getClassLoader()
                        .getResource("jekyll/post_template.md")
                        .toURI()))
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));
    }


    /**
     * Writes a new Jekyll post file to the local git repo directory.
     *
     * @param postDirectory Local directory where posts are being written.
     * @param postContent   Text of the post to generate.
     * @return String Filename of the new post.
     *
     * @throws IOException Thrown if writing the post fails.
     */
    private String writePostToLocalRepository(Path postDirectory, String episodeTitle, String postContent) throws IOException {
        String postFilenameDatePrefix = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
        String postFilenameTitleInfix = Slugify.builder().build().slugify(episodeTitle);
        String newPostFilename = String.format("%s-%s.md",
                postFilenameDatePrefix,
                postFilenameTitleInfix);
        Path newPostPath = postDirectory.resolve(newPostFilename);
        Files.write(newPostPath,
                postContent.getBytes(StandardCharsets.UTF_8));
        return newPostPath.toString();
    }

    /**
     * Builds the body of a post to be committed to the blog.
     *
     * @param template Template to load for this post
     * @return String contents of the post
     */
    private String buildPostContents(String template, BlogEpisode episode) {
        Map<String, String> tokenMap = new HashMap<>();
        // TODO: Parameterize post layout using environment variables
        tokenMap.put("layout",
                "post");

        tokenMap.put("title",
                episode.getEpisodeTitle());

        // TODO: Set post body based on episode being parsed.
        tokenMap.put("content",
                "Here is a description of the episode");

        StringSubstitutor postSubstitutor = new StringSubstitutor(tokenMap);
        return postSubstitutor.replace(template);
    }
}
