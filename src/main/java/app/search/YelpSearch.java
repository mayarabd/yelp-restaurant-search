package app.search;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import javax.json.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YelpSearch {
    private static String REVIEW_URL = "http://localhost:8983/solr/reviews";
    private static String BUSINESS_URL = "http://localhost:8983/solr/business";

    private List<YelpBusiness> businessObjList;

    private SolrDocumentList query(String userQuery, String urlString, SearchType searchType) {

        SolrDocumentList results = new SolrDocumentList();
        QueryResponse response = null;
        int numItemsPerPage = 10;

        try {
            SolrClient solr = new HttpSolrClient.Builder(urlString).build();
            SolrQuery solrQuery = new SolrQuery();

            int pageNum = 1;

            if (urlString.equals(BUSINESS_URL)) {
                numItemsPerPage = 30;
            }

            solrQuery.setStart((pageNum - 1) * numItemsPerPage);
            solrQuery.setRows(numItemsPerPage);

            //gets matching restaurant results with most reviews and highest star rating
            if (urlString.equals(BUSINESS_URL)) {
                solrQuery.set("q", "categories:" + userQuery);
                solrQuery.addSort(new SolrQuery.SortClause("review_count", SolrQuery.ORDER.desc));
                solrQuery.addSort(new SolrQuery.SortClause("stars", SolrQuery.ORDER.desc));

                //gets reviews for restaurant and searchType
            } else if (urlString.equals(REVIEW_URL)) {
                solrQuery.setFields("business_id", "text", "review_id");
                solrQuery.set("q", "business_id:" + userQuery + " text:"+ searchType);
            }

            response = solr.query(solrQuery);

            if (response != null) {
                results.addAll(response.getResults());
            }

            //prints result for reference to console
            for (int i = 0; i < results.size(); i++) {
                System.out.println(results.get(i));
            }

        } catch (org.apache.solr.client.solrj.SolrServerException slre) {
            System.out.println(slre);
        } catch (Exception e) {
            System.out.println(e);
        }

        return results;
    }

    /**
     * Saves the results from re-ranking businesses.
     * Format: [QryID] 0 [DocID] [Rank] [Score] STANDARD
     *
     * @param rankedResults the ranked documents
     * @param query         the query evaluated
     *
     */
    private void saveQueryResultToFile(List<YelpBusiness> rankedResults, String query, SearchType searchType) {
        if (searchType != null) {
            try (FileWriter writer = new FileWriter(query + searchType + ".txt", true)) {
                if (rankedResults != null && rankedResults.size() > 0) {
                    int rank = 1;
                    for (YelpBusiness business : rankedResults) {
                        String name = business.getName().replaceAll("\\s", "");
                        System.out.println(name);
                        writer.write(query + " " + "0" + " " + name + " " + rank + " " + business.getNewScore()
                                + " " + "STANDARD" + "\n");
                        rank++;

                    }
                }
            } catch (IOException ioe) {
                System.out.println(Arrays.toString(ioe.getStackTrace()));
            }
        }
    }


    /**
     * saves Business result object
     *
     * @param results a solr response list
     */
    private void createBusinessObj(SolrDocumentList results) {
        DocumentObjectBinder dob = new DocumentObjectBinder();
        this.businessObjList = dob.getBeans(YelpBusiness.class, results);
    }

    /**
     * Formats Reviews into proper Json form for sentiment analysis
     * @param reviewsResults a list of Solr reviews
     * @return a list of formatted Json Objects
     */
    private JsonObject createJSONObj(SolrDocumentList reviewsResults) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (SolrDocument review : reviewsResults) {
            String reviewId = (String) review.get("review_id");
            String reviewText = (String) review.get("text");

            arrayBuilder.add(Json.createObjectBuilder()
                    .add("language", "en")
                    .add("id", reviewId)
                    .add("text", reviewText));
        }

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("documents", arrayBuilder)
                .build();

        return jsonObject;
    }

    /**
     * Performs sentiment analysis on reviews
     * @param reviewsResults a list of Solr reviews
     * @return a list of Json reviews with sentiment score
     */
    private JsonObject sentimentAnalysis(SolrDocumentList reviewsResults) {
        //formats the reviews
        JsonObject jsonReviews = createJSONObj(reviewsResults);

        HttpClient httpclient = HttpClients.createDefault();
        JsonObject jsonSentimentResult = null;

        try {
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/text/analytics/v2.0/sentiment");
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", "108643889e6848688c94790875713412");

            StringEntity reqEntity = new StringEntity(jsonReviews.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            //saves response in Json Object
            if (entity != null) {
                JsonReader jsonReader = Json.createReader(new StringReader(EntityUtils.toString(entity)));
                jsonSentimentResult = jsonReader.readObject();
                jsonReader.close();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return jsonSentimentResult;
    }

    /**
     * Calculates average of all review scores
     * @param results a list of reviews with sentiment score
     * @return a sentiment score for the
     */
    private double averageSentiment(JsonObject results) {
        JsonArray arrayResult = results.getJsonArray("documents");
        double total = 0;

        for (int i = 0; i < arrayResult.size(); i++) {
            JsonObject obj = arrayResult.getJsonObject(i);
            double score = obj.getJsonNumber("score").doubleValue();
            total += score;
            System.out.println("Score: " + obj.get("score") + "id: " +  obj.get("id"));
        }

        return (total / (double) arrayResult.size());
    }

    private void rankBusinesses(SearchType searchType) {
        businessObjList.forEach(business -> {
            if (searchType == SearchType.SERVICE) {
                business.setNewScore(business.getServiceScore());
            } else if (searchType == SearchType.AMBIANCE) {
                business.setNewScore(business.getAmbianceScore());
            } else {
                business.setNewScore(business.getStars());
            }
        });

        //sort
        businessObjList.sort((bus1, bus2) -> new Double(bus2.getNewScore()).compareTo(bus1.getNewScore()));
    }

    /**
     *
     * @param userQuery a String to be searched
     * @param searchType ambiance or service string
     * @return a list of ranked restaurants
     */
    public List<YelpBusiness> query(String userQuery, SearchType searchType) {
        SolrDocumentList restaurantResults = this.query(userQuery, BUSINESS_URL, searchType);
        createBusinessObj(restaurantResults);

        List<YelpBusiness> tempList = new ArrayList<>();

        for (YelpBusiness restaurant : this.businessObjList) {
            //escape special solr character at beginning of businessId
            StringBuilder stbBusId = new StringBuilder(restaurant.getBusinessId());
            stbBusId.insert(0, '\\');
            String businessId = stbBusId.toString();

            //get all reviews from this restaurant
            SolrDocumentList reviewsResults = this.query(businessId, REVIEW_URL, searchType);
            //if restaurant does not have reviews, skip it
            if (reviewsResults.size() > 0 && tempList.size() < 10) {

                tempList.add(restaurant);
                //get sentiment score for each review
                JsonObject sentimentResult = this.sentimentAnalysis(reviewsResults);
                //gets overall sentiment score for the business
                double businessSentimentScore = averageSentiment(sentimentResult);
                //saves score
                if (searchType == SearchType.SERVICE) {
                    restaurant.setServiceScore(businessSentimentScore);
                } else if (searchType == SearchType.AMBIANCE) {
                    restaurant.setAmbianceScore(businessSentimentScore);
                }
            } else if (tempList.size() == 10) {
                break;
            }

        }
        this.businessObjList = tempList;

        //rank business based on new scores
        rankBusinesses(searchType);

        //save to results to file
        saveQueryResultToFile(businessObjList, userQuery, searchType);

        return this.businessObjList;
    }
}
