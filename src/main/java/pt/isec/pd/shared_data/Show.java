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
    private String duration;
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
            String duration,
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

    public int getId() { return id; }

    public String getDescription() { return description; }

    public String getType() { return type; }

    public String getDateHour() { return dateHour; }

    public String getDuration() { return duration; }

    public String getLocation() { return location; }

    public String getLocality() { return locality; }

    public String getCountry() { return country; }

    public String getAgeClassification() { return ageClassification; }

    public boolean isVisible() { return visible; }
}
