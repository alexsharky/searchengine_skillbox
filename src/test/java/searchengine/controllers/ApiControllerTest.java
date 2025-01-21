package searchengine.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import searchengine.dto.indexing.IndexPageRequest;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private IndexingService indexingService;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private ApiController apiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testStatistics() {
        // Arrange
        StatisticsResponse expectedResponse = new StatisticsResponse();
        when(statisticsService.getStatistics()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<StatisticsResponse> response = apiController.statistics();

        // Assert
        assertEquals(expectedResponse, response.getBody());
        verify(statisticsService, times(1)).getStatistics();
    }

    @Test
    void testStartIndexingSuccess() {
        // Arrange
        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(true);
        when(indexingService.startIndexing()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<IndexingResponse> response = apiController.startIndexing();

        // Assert
        assertEquals(expectedResponse, response.getBody());
        verify(indexingService, times(1)).startIndexing();
    }

    @Test
    void testStartIndexingFailure() {
        // Arrange
        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        when(indexingService.startIndexing()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<IndexingResponse> response = apiController.startIndexing();

        // Assert
        assertEquals(expectedResponse, response.getBody());
        assertEquals(400, response.getStatusCodeValue());
        verify(indexingService, times(1)).startIndexing();
    }

    @Test
    void testStopIndexingSuccess() {
        // Arrange
        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(true);
        when(indexingService.stopIndexing()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<IndexingResponse> response = apiController.stopIndexing();

        // Assert
        assertEquals(expectedResponse, response.getBody());
        verify(indexingService, times(1)).stopIndexing();
    }

    @Test
    void testStopIndexingFailure() {
        // Arrange
        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        when(indexingService.stopIndexing()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<IndexingResponse> response = apiController.stopIndexing();

        // Assert
        assertEquals(expectedResponse, response.getBody());
        assertEquals(400, response.getStatusCodeValue());
        verify(indexingService, times(1)).stopIndexing();
    }

    @Test
    void testIndexPageSuccess() {
        // Arrange
        IndexPageRequest request = new IndexPageRequest();
        request.setUrl("http://example.com");
        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(true);
        when(indexingService.indexPage(request.getUrl())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<IndexingResponse> response = apiController.indexPage(request);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        verify(indexingService, times(1)).indexPage(request.getUrl());
    }

    @Test
    void testIndexPageFailure() {
        // Arrange
        IndexPageRequest request = new IndexPageRequest();
        request.setUrl("http://example.com");
        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        when(indexingService.indexPage(request.getUrl())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<IndexingResponse> response = apiController.indexPage(request);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        assertEquals(400, response.getStatusCodeValue());
        verify(indexingService, times(1)).indexPage(request.getUrl());
    }

    @Test
    void testSearchAllSites() {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;
        SearchResponse expectedResponse = new SearchResponse();
        expectedResponse.setResult(true);
        when(searchService.searchAllSites(query, limit, offset)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<SearchResponse> response = apiController.search(query, null, offset, limit);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        verify(searchService, times(1)).searchAllSites(query, limit, offset);
    }

    @Test
    void testSearchSite() {
        // Arrange
        String query = "test query";
        String site = "http://example.com";
        int offset = 0;
        int limit = 20;
        SearchResponse expectedResponse = new SearchResponse();
        expectedResponse.setResult(true);
        when(searchService.searchSite(site, query, limit, offset)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<SearchResponse> response = apiController.search(query, site, offset, limit);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        verify(searchService, times(1)).searchSite(site, query, limit, offset);
    }

    @Test
    void testSearchFailure() {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;
        SearchResponse expectedResponse = new SearchResponse();
        expectedResponse.setResult(false);
        when(searchService.searchAllSites(query, limit, offset)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<SearchResponse> response = apiController.search(query, null, offset, limit);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        assertEquals(400, response.getStatusCodeValue());
        verify(searchService, times(1)).searchAllSites(query, limit, offset);
    }
}