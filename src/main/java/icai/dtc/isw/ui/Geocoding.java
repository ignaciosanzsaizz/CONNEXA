package icai.dtc.isw.ui;

/**
 * Utilidad sencilla para convertir direcciones en coordenadas usando
 * el servicio público de Nominatim. Las posiciones devueltas se usan
 * para centrar el mapa dentro de la UI de CONNEXA.
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Geocoding {

    private static final Map<String, LatLon> CACHE = new ConcurrentHashMap<>();

    public static class LatLon {
        public final double lat, lon;
        public LatLon(double lat, double lon) { this.lat = lat; this.lon = lon; }
    }
    public static LatLon geocode(String address) throws Exception {
        if (address == null || address.isBlank()) return null;
        String q = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?q=" + q + "&format=json&limit=1";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestProperty("User-Agent", "Connexa/1.0 (contacto: soporte@connexa.local)");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line; while ((line = br.readLine()) != null) sb.append(line);
            JSONArray arr = new JSONArray(sb.toString());
            if (arr.length() == 0) return null;
            JSONObject obj = arr.getJSONObject(0);
            return new LatLon(Double.parseDouble(obj.getString("lat")), Double.parseDouble(obj.getString("lon")));
        }
    }

    public static LatLon geocodeCached(String address) throws Exception {
        if (address == null) return null;
        String key = address.trim().toLowerCase();
        if (key.isEmpty()) return null;
        LatLon cached = CACHE.get(key);
        if (cached != null) return cached;
        LatLon resolved = geocode(address);
        if (resolved != null) {
            CACHE.put(key, resolved);
        }
        return resolved;
    }

    public static double distanceKm(LatLon from, LatLon to) {
        if (from == null || to == null) return Double.NaN;
        final double R = 6371.0; // km
        double latDistance = Math.toRadians(to.lat - from.lat);
        double lonDistance = Math.toRadians(to.lon - from.lon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(from.lat)) * Math.cos(Math.toRadians(to.lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static double distanceKm(String originAddress, String destinationAddress) throws Exception {
        LatLon origin = geocodeCached(originAddress);
        LatLon destination = geocodeCached(destinationAddress);
        return distanceKm(origin, destination);
    }
}
