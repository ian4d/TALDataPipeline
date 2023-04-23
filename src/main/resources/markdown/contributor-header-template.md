---
layout: blogEpisodeContributor
title: "%s"
blogEpisodeContributor: %s
---
{%% assign person = site.data.contributors[page.blogEpisodeContributor] %%}

So what can I tell you about {{ person.name }}?

Well, {{ person.name }} has appeared in the following episodes: {{ person.episodes }}

<ul>
{%% for blogEpisode in person.episodes %%}
    <li class="blogEpisode-container">
      <span class="blogEpisode-link">
        <a href="/episodes/{{ blogEpisode }}.html">{{ blogEpisode }}</a>
      </span>
    </li>
{%% endfor %%}
</ul>

Also, here are all the unique words that {{ person.name }} has spoken on the show: {{ person.allWords }}

<script language="javascript">
(function() {
    var data = '{{ person }}';
    while (data.indexOf("=>") > -1) {
      data = data.replace("=>",":");
    }
    data = JSON.parse(data);
    var allWords = data['allWords'];
    var name = data['name'];
    var normalizedName = data['normalized-name'];
    var episodes = data['episodes'];

    console.log(name);
})();
</script>

