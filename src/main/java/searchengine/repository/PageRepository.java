package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
    List<Page> findBySiteIn(Iterable<Site> site);
    Page findBySiteAndPath(Site site, String path);
    List<Page> findBySiteAndPathIn(Site site, Iterable<String> paths);
}
