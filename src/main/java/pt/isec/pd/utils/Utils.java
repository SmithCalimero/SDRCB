package pt.isec.pd.utils;

import javafx.util.Pair;
import pt.isec.pd.shared_data.Seat;
import pt.isec.pd.shared_data.Show;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
    public static byte[] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oss = new ObjectOutputStream(baos);
        oss.writeObject(object);
        return baos.toByteArray();
    }

    public static <T> T deserializeObject(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Pair<Show,Map<String,List<Seat>>> readFile(String path) {
        Show show = new Show();
        Map<String,List<Seat>> seatsMap = new HashMap<>();
        try (Scanner input = new Scanner(new FileReader(path))) {
            while(input.hasNext()){
                input.useDelimiter(";|:|\n");
                input.useLocale(Locale.US);
                String key = processString(input.next());
                switch (key) {
                    case "Designação" -> show.setDescription(processString(input.nextLine()));
                    case "Tipo" -> show.setType(processString(input.nextLine()));
                    case "Data" -> {
                        String day = processString(input.next());
                        String month = processString(input.next());
                        String year = processString(input.next());

                        input.next();

                        String hours = processString(input.next());
                        String minutes = processString(input.next());

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = format.parse(year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":00");
                        show.setDateHour(year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":00");
                    }
                    case "Duração" -> show.setDuration(Integer.parseInt(processString(input.next())));
                    case "Local" -> show.setLocation(processString(input.nextLine()));
                    case "Localidade" -> show.setLocality(processString(input.next()));
                    case "País" -> show.setCountry(processString(input.next()));
                    case "Classificação etária" -> show.setAgeClassification(processString(input.next()));
                    case "Fila" -> {
                        input.nextLine();
                        while(input.hasNext()) {
                            setsReader(input,seatsMap);
                        }
                    }
                }
            }
        } catch (FileNotFoundException | ParseException e) {
            return null;
        }
        return new Pair<>(show,seatsMap);
    }

    private static String processString(String value) {
        return value.replace(";","").replace("“","").replace("\"","").replace("”","").replace(":","").trim();
    }

    private static void setsReader(Scanner input, Map<String,List<Seat>> seatsMap) {
        String row = input.nextLine();
        String[] seats = row.split(";");
        row = processString(seats[0]);
        seatsMap.put(processString(seats[0]),new ArrayList<>());
        for (int i = 1;i < seats.length; i++) {
            String[] details = seats[i].split(":");
            seatsMap.get(processString(seats[0])).add(new Seat(row,processString(details[0]),Double.parseDouble(processString(details[1]))));
        }
    }


}
