package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    List<Lemma> findBySiteIn(Iterable<Site> site);
    List<Lemma> findBySiteAndLemmaIn(Site site, Iterable<String> lemma);
    List<Lemma> findBySiteInAndLemmaIn(Iterable<Site> site, Iterable<String> lemma);

}
