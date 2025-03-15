package com.example.mapease;

public class Review {
    private String title;
    private String subtitle;
    private String location;
    private float rating;
    private String time;
    private String comment;
    private String views;
    private String react;
    private Integer likes; // Có thể null cho các review không có lượt thích

    public Review(String title, String subtitle, String location, float rating, String time,
                  String comment, String views, String react, Integer likes) {
        this.title = title;
        this.subtitle = subtitle;
        this.location = location;
        this.rating = rating;
        this.time = time;
        this.comment = comment;
        this.views = views;
        this.react = react;
        this.likes = likes;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getLocation() {
        return location;
    }

    public float getRating() {
        return rating;
    }

    public String getTime() {
        return time;
    }

    public String getComment() {
        return comment;
    }

    public String getViews() {
        return views;
    }

    public String getReact() {
        return react;
    }

    public Integer getLikes() {
        return likes;
    }
}
