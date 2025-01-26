package searchengine.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SitesList sitesSettings;
    private final ApplicationContext applicationContext;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Override
    public SearchResponse searchSite(@NonNull String siteUrl, @NonNull String query, int limit, int offset) {
        if (siteUrl.endsWith("/")) {
            siteUrl = siteUrl.substring(0, siteUrl.length() - 1);
        }
        var url = siteUrl.toLowerCase();

        boolean notInSettings = sitesSettings.getSites().stream()
                .map(searchengine.config.Site::getUrl)
                .noneMatch(s -> s.equals(url));
        if (notInSettings) {
            return SearchResponse.builder().result(false).error("Сайт не указан в настройках индексации").build();
        }

        Site site = siteRepository.findByUrl(siteUrl);
        if (site == null || site.getStatus() == IndexingStatus.INDEXING) {
            return SearchResponse.builder().result(false).error("Индексация сайта ещё не завершена").build();
        }

        List<Site> sites = List.of(site);
        return search(sites, query, limit, offset);
    }

    @Override
    public SearchResponse searchAllSites(@NonNull String query, int limit, int offset) {
        List<String> sitesInSettings = sitesSettings.getSites().stream()
                .map(searchengine.config.Site::getUrl)
                .toList();

        List<Site> sitesInDB = siteRepository.findByUrlIn(sitesInSettings);

        if (sitesInSettings.size() != sitesInDB.size()) {
            return SearchResponse.builder().result(false).error("Индексация части сайтов из настроек не запускалась")
                    .build();
        }

        boolean indexingInProcess = sitesInDB.stream()
                .anyMatch(site -> site.getStatus() == IndexingStatus.INDEXING);
        if (indexingInProcess) {
            return SearchResponse.builder().result(false).error("Индексация части сайтов ещё не завершена").build();
        }

        return search(sitesInDB, query, limit, offset);
    }

    private SearchResponse search(List<Site> sites, String query, int limit, int offset) {
        if (query == null || query.isBlank()) {
            return SearchResponse.builder().result(false).error("Задан пустой поисковый запрос").build();
        }
        if (limit <= 0) {
            return SearchResponse.builder().result(false).error("Параметр limit должен быть больше нуля").build();
        }
        if (offset < 0) {
            return SearchResponse.builder().result(false).error("Параметр offset не может быть отрицательным").build();
        }

        Map<Site, List<Lemma>> lemmas = findLemmas(sites, query);
        if (lemmas.isEmpty()) {
            return SearchResponse.builder().result(true).count(0).data(Collections.emptyList()).build();
        }

        List<String> siteUrls = sites.stream().map(Site::getUrl).distinct().toList();
        log.info("Начат поиск \"{}\" в списке сайтов: {}", query, siteUrls);
        long start = System.currentTimeMillis();

        Map<Page, Float> absoluteRelevance = computeAbsoluteRelevance(lemmas);
        Map<Page, Float> relativeRelevance = computeRelativeRelevance(absoluteRelevance);

        LemmasFinder lemmasFinder = applicationContext.getBean(LemmasFinder.class);
        Set<String> lemmasNames = lemmasFinder.findLemmas(query).keySet();
        List<SearchData> data = getSearchData(relativeRelevance, limit, offset, lemmasNames);

        int foundCount = relativeRelevance.size();

        log.info("Поиск \"{}\" выполнен за {} мс. Найдено результатов: {}. Список сайтов: {}.",
                query, System.currentTimeMillis() - start, foundCount, siteUrls);

        return SearchResponse.builder().result(true).count(foundCount).data(data).build();
    }

    private Map<Site, List<Lemma>> findLemmas(List<Site> sites, String query) {
        if (sites.isEmpty() || query.isBlank()) {
            return Collections.emptyMap();
        }

        LemmasFinder lemmasFinder = applicationContext.getBean(LemmasFinder.class);
        Set<String> lemmasNames = lemmasFinder.findLemmas(query).keySet();
        List<Lemma> lemmas = lemmaRepository.findBySiteInAndLemmaIn(sites, lemmasNames);

        return lemmas.stream()
                .collect(Collectors.groupingBy(Lemma::getSite))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() == lemmasNames.size())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Page, Float> computeAbsoluteRelevance(Map<Site, List<Lemma>> lemmas) {
        if (lemmas.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Page, Float> relevance = new HashMap<>();

        for (List<Lemma> siteLemmas : lemmas.values()) {
            if (siteLemmas.isEmpty()) {
                continue;
            }
            Map<Page, Float> tmpRelevance = computeAbsoluteRelevance(siteLemmas);
            relevance.putAll(tmpRelevance);
        }

        return relevance;
    }

    private Map<Page, Float> computeAbsoluteRelevance(List<Lemma> lemmas) {
        if (lemmas.isEmpty()) {
            return Collections.emptyMap();
        }

        lemmas.sort(Comparator.comparingInt(Lemma::getFrequency));

        Map<Page, Float> relevance = indexRepository.findByLemma(lemmas.get(0))
                .stream()
                .collect(Collectors.toMap(Index::getPage, Index::getRank));

        for (int i = 1; i < lemmas.size(); i++) {
            Lemma currentLemma = lemmas.get(i);
            Map<Page, Float> tmpRelevance = indexRepository.findByLemmaAndPageIn(currentLemma, relevance.keySet())
                    .stream()
                    .collect(Collectors.toMap(Index::getPage, Index::getRank));

            if (tmpRelevance.isEmpty()) {
                return tmpRelevance;
            }

            for (Map.Entry<Page, Float> entry : tmpRelevance.entrySet()) {
                float newValue = entry.getValue() + relevance.get(entry.getKey());
                entry.setValue(newValue);
            }
            relevance = tmpRelevance;
        }

        return relevance;
    }

    private Map<Page, Float> computeRelativeRelevance(@NonNull Map<Page, Float> absoluteRelevance) {
        if (absoluteRelevance.isEmpty()) {
            return Collections.emptyMap();
        }

        float maxRelevance = absoluteRelevance.values().stream()
                .max(Float::compareTo)
                .get();

        return absoluteRelevance.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / maxRelevance));
    }

    private List<SearchData> getSearchData(
            @NonNull Map<Page, Float> relevance,
            int limit,
            int offset,
            @NonNull Set<String> lemmas
    ) {
        if (relevance.isEmpty() || offset > (relevance.size() - 1) || lemmas.isEmpty() || limit <= 0 || offset < 0) {
            return Collections.emptyList();
        }

        List<Map.Entry<Page, Float>> sortedRelevance = relevance.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .toList();

        List<SearchData> data = new ArrayList<>(limit);
        LemmasFinder lemmasFinder = applicationContext.getBean(LemmasFinder.class);

        int maxIndex = Math.min(offset + limit, relevance.size());
        for (int i = offset; i < maxIndex; i++) {
            Map.Entry<Page, Float> entry = sortedRelevance.get(i);
            Page page = entry.getKey();
            Site site = page.getSite();

            String title = "";
            String snippet = "";
            if (page.canBeParsed()) {
                var document = Jsoup.parse(page.getContent());
                title = document.title();
                snippet = lemmasFinder.getSnippet(document.wholeText(), lemmas);
            }

            SearchData searchData = new SearchData();
            searchData.setSite(site.getUrl());
            searchData.setSiteName(site.getName());
            searchData.setUri(page.getPath());
            searchData.setTitle(title);
            searchData.setSnippet(snippet);
            searchData.setRelevance(entry.getValue());

            data.add(searchData);
        }

        return data;
    }
}