package app.search;

import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

/**
 * Created by mayara on 4/29/17.
 */

public class YelpBusiness {

    @Field("new_score")
    private double newScore;

    @Field("business_score_service")
    private double serviceScore;

    @Field("business_score_ambiance")
    private double ambianceScore;

    @Field("business_id")
    private String businessId;

    @Field("name")
    private String name;

    @Field("neighborhood")
    private String neighborhood;

    @Field("address")
    private String address;

    @Field("city")
    private String city;

    @Field("state")
    private String state;

    @Field("postal_code")
    private String postalCode;

    @Field("stars")
    private float stars;

    @Field("review_count")
    private int reviewCount;

    @Field("categories")
    private List<String> categories;

    @Field("hours")
    private List<String> hours;

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getHours() {
        return hours;
    }

    public void setHours(List<String> hours) {
        this.hours = hours;
    }

    public float getStars() {
        return stars;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReview_count(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public double getServiceScore() { return this.serviceScore; }

    public void setServiceScore (double score) { this.serviceScore = score; }

    public double getAmbianceScore() {
        return ambianceScore;
    }

    public void setAmbianceScore(double ambianceScore) {
        this.ambianceScore = ambianceScore;
    }

    public double getNewScore() {
        return newScore;
    }

    public void setNewScore(double newScore) {
        this.newScore = newScore;
    }
}
