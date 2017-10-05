package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import database.Connection;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CurriculumService {

    private final Connection conn;

    @Inject
    public CurriculumService(Connection conn) {
        this.conn = conn;
    }

    public ObjectNode getCurriculumSemesters(String curriculumId) throws SQLException, IOException {
        String query =
                "SELECT coalesce(array_to_json(array_agg(row_to_json(co))), '[]'::json) as semesters\n" +
                        "FROM study.curriculum_overview co\n" +
                        "WHERE study_programme = ?;";
        final ResultSet resultSet = conn.executeQuery(query, curriculumId);
        resultSet.next();
        final String JSONString = resultSet.getString("semesters");

        ObjectNode resultObject = buildJsonResult(JSONString);
        return resultObject;
    }

    private ObjectNode buildJsonResult(String JSONString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode resultObject = mapper.createObjectNode();
        ArrayNode resultArray = mapper.createArrayNode();
        resultObject.set("semesters", resultArray);

        ArrayNode jsonArray = (ArrayNode) mapper.readTree(JSONString);
        if (jsonArray.size() == 0) return resultObject;

        ObjectNode currentModule = (ObjectNode) jsonArray.get(0);
        int semester = currentModule.get("semester").asInt();

        cleanModuleNode(currentModule);

        ObjectNode currentSemester = mapper.createObjectNode();
        currentSemester.put("semester", semester);

        ArrayNode currentSemesterModules = mapper.createArrayNode();
        currentSemester.set("modules", currentSemesterModules);

        currentSemesterModules.add(currentModule);

        for (int i = 1; i < jsonArray.size(); i++) {
            currentModule = (ObjectNode) jsonArray.get(i);

            if (currentModule.get("semester").asInt() != semester) {
                resultArray.add(currentSemester);
                currentSemester = mapper.createObjectNode();
                semester = currentModule.get("semester").asInt();
                currentSemester.put("semester", semester);

                currentSemesterModules = mapper.createArrayNode();
                currentSemester.set("modules", currentSemesterModules);

            }

            cleanModuleNode(currentModule);
            currentSemesterModules.add(currentModule);
        }
        resultArray.add(currentSemester);
        return resultObject;
    }

    private void cleanModuleNode(ObjectNode currentModule) {
        currentModule.remove("semester");
        currentModule.remove("name");
        currentModule.remove("study_programme");
    }
}
