package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {
    Site findByUrl(String url);
    List<Site> findByUrlIn(List<String> urls);
    List<Site> findByStatusAndUrlIn(IndexingStatus status, List<String> urls);
}
