package pt.isec.pd.shared_data;

import java.io.Serial;
import java.io.Serializable;

public class Show implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String description;
    private String type;
    private String dateHour;
    private int duration;
    private String location;
    private String locality;
    private String country;
    private String ageClassification;
    private boolean visible;

    public Show(
            int id,
            String description,
            String type,
            String dateHour,
            int duration,
            String location,
            String locality,
            String country,
            String ageClassification,
            boolean visible) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.dateHour = dateHour;
        this.duration = duration;
        this.location = location;
        this.locality = locality;
        this.country = country;
        this.ageClassification = ageClassification;
        this.visible = visible;
    }

    public Show(
            String description,
            String type,
            String dateHour,
            int duration,
            String location,
            String locality,
            String country,
            String ageClassification,
            boolean visible) {
        this.description = description;
        this.type = type;
        this.dateHour = dateHour;
        this.duration = duration;
        this.location = location;
        this.locality = locality;
        this.country = country;
        this.ageClassification = ageClassification;
        this.visible = visible;
    }

    public Show() {}

    public int getId() { return id; }

    public String getDescription() { return description; }

    public String getType() { return type; }

    public String getDateHour() { return dateHour; }

    public int getDuration() { return duration; }

    public String getLocation() { return location; }

    public String getLocality() { return locality; }

    public String getCountry() { return country; }

    public String getAgeClassification() { return ageClassification; }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDateHour(String dateHour) {
        this.dateHour = dateHour;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setAgeClassification(String ageClassification) {
        this.ageClassification = ageClassification;
    }

    public boolean isVisible() { return visible; }

    @Override
    public String toString() {
        return "descrição: " + description  + "\n" + "duração: " + duration  + "\n" + "data: " + dateHour + "\n" + visible;}
}
