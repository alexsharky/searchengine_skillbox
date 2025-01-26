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
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.util.List;

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
    void startIndexingShouldReturnErrorResponseWhenIndexingAlreadyRunning() {

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        expectedResponse.setError("Индексация уже выполняется");

        when(indexingService.startIndexing()).thenReturn(expectedResponse);


        ResponseEntity<IndexingResponse> actualResponse = apiController.startIndexing();


        assertAll(
                () -> assertEquals(400, actualResponse.getStatusCodeValue(), "HTTP status should be 400"),
                () -> assertFalse(actualResponse.getBody().isResult(), "Result should be false"),
                () -> assertEquals("Индексация уже выполняется", actualResponse.getBody().getError(), "Error message mismatch")
        );
        verify(indexingService, times(1)).startIndexing();
    }


    @Test
    void stopIndexingShouldReturnErrorResponseWhenNoActiveIndexing() {

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        expectedResponse.setError("Индексация не выполняется");

        when(indexingService.stopIndexing()).thenReturn(expectedResponse);


        ResponseEntity<IndexingResponse> actualResponse = apiController.stopIndexing();


        assertAll(
                () -> assertEquals(400, actualResponse.getStatusCodeValue(), "HTTP status should be 400"),
                () -> assertFalse(actualResponse.getBody().isResult(), "Result should be false"),
                () -> assertEquals("Индексация не выполняется", actualResponse.getBody().getError(), "Error message mismatch")
        );
        verify(indexingService, times(1)).stopIndexing();
    }


    @Test
    void indexPageShouldReturnErrorForInvalidUrl() {

        IndexPageRequest invalidRequest = new IndexPageRequest();
        invalidRequest.setUrl("invalid-url");

        IndexingResponse expectedResponse = new IndexingResponse();
        expectedResponse.setResult(false);
        expectedResponse.setError("Некорректный URL");

        when(indexingService.indexPage(invalidRequest.getUrl())).thenReturn(expectedResponse);


        ResponseEntity<IndexingResponse> actualResponse = apiController.indexPage(invalidRequest);


        assertAll(
                () -> assertEquals(400, actualResponse.getStatusCodeValue(), "HTTP status should be 400"),
                () -> assertFalse(actualResponse.getBody().isResult(), "Result should be false"),
                () -> assertEquals("Некорректный URL", actualResponse.getBody().getError(), "Error message mismatch")
        );
        verify(indexingService, times(1)).indexPage(invalidRequest.getUrl());
    }

    @Test
    void searchShouldReturnValidResultsForAllSites() {
        // Arrange
        String searchQuery = "test query";
        int resultOffset = 0;
        int resultLimit = 10;

        SearchResponse expectedResponse = new SearchResponse();
        expectedResponse.setResult(true);
        expectedResponse.setCount(25);
        expectedResponse.setData(List.of(/* mock search results */));

        when(searchService.searchAllSites(searchQuery, resultLimit, resultOffset)).thenReturn(expectedResponse);


        ResponseEntity<SearchResponse> actualResponse = apiController.search(searchQuery, null, resultOffset, resultLimit);


        assertAll(
                () -> assertEquals(200, actualResponse.getStatusCodeValue(), "HTTP status should be 200"),
                () -> assertTrue(actualResponse.getBody().isResult(), "Result should be true"),
                () -> assertEquals(25, actualResponse.getBody().getCount(), "Total results count mismatch"),
                () -> assertNotNull(actualResponse.getBody().getData(), "Search results should not be null")
        );
        verify(searchService, times(1)).searchAllSites(searchQuery, resultLimit, resultOffset);
    }


    @Test
    void searchShouldReturnErrorForEmptyQuery() {

        String emptyQuery = "";

        SearchResponse expectedResponse = new SearchResponse();
        expectedResponse.setResult(false);
        expectedResponse.setError("Поисковый запрос не может быть пустым");

        when(searchService.searchAllSites(emptyQuery, 20, 0)).thenReturn(expectedResponse);


        ResponseEntity<SearchResponse> actualResponse = apiController.search(emptyQuery, null, 0, 20);


        assertAll(
                () -> assertEquals(400, actualResponse.getStatusCodeValue(), "HTTP status should be 400"),
                () -> assertFalse(actualResponse.getBody().isResult(), "Result should be false"),
                () -> assertEquals("Поисковый запрос не может быть пустым", actualResponse.getBody().getError(), "Error message mismatch")
        );
        verify(searchService, times(1)).searchAllSites(emptyQuery, 20, 0);
    }
}