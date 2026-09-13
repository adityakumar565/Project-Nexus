package com.workflow.dag_engine.controller.temp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@RestController
public class TempController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @GetMapping("/temp/graphs")
    public List<Map<String, Object>> getGraphs() {
        return jdbcTemplate.queryForList("SELECT graph_id, user_id, graph_name, graph_data IS NULL as is_null, substring(graph_data::text from 1 for 200) as data FROM workflow_graphs.t_graph");
    }
}
