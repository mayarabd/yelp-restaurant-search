package app.api.contollers;

import app.search.YelpSearch;
import app.search.SearchType;
import app.search.YelpBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.QueryParam;
import java.util.List;

@RestController
public class SearchController {
    @RequestMapping(value = "/api/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity search(@QueryParam("query") String query, @QueryParam("searchType") SearchType searchType) {
        if (query == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("query is required.");
        }

        List<YelpBusiness> responseList = new YelpSearch().query(query, searchType);
        return ResponseEntity.ok(responseList);
    }
}
