package searchengine.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DefaultController.class) // Аннотация для тестирования контроллеров
public class DefaultControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc для выполнения HTTP-запросов

    @Test
    void testIndex() throws Exception {
        // Выполняем GET-запрос к корневому пути ("/")
        mockMvc.perform(get("/"))
                .andExpect(status().isOk()) // Проверяем, что статус ответа 200 (OK)
                .andExpect(view().name("index")); // Проверяем, что возвращается представление "index"
    }
}