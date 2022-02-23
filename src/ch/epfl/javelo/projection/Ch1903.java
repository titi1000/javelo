package ch.epfl.javelo.projection;

/**
 * Offre des méthodes statiques permettant de convertir entre les coordonnées WGS 84 et les coordonnées suisses.
 *
 * @author Quentin Chappuis (339517)
 */
public final class Ch1903 {
    private Ch1903(){};

    /**
     * Retourne la coordonnée E (est) du point de longitude lon et latitude lat dans le système WGS84.
     * @param lon longitude
     * @param lat latitude
     * @return la coordonnée E
     */
    public static double e(double lon, double lat) {
        lon = Math.toDegrees(lon);
        lat = Math.toDegrees(lat);
        double delta1 = 1e-4 * (3600 * lon - 26782.5);
        double phi1 =  1e-4 * (3600 * lat - 26782.5);
        return 2600072.37 + 211455.93 * delta1 - 10938.51 * delta1 * phi1 - 0.36 * delta1 * Math.pow(phi1, 2) - 44.54 * Math.pow(delta1, 3);
    };

    /**
     *Retourne la coordonnée N (nord) du point de longitude lon et latitude lat dans le système WGS84.
     * @param lon longitude
     * @param lat latitude
     * @return la coordonnée N
     */
    public static double n(double lon, double lat) {
        lon = Math.toDegrees(lon);
        lat = Math.toDegrees(lat);
        double delta1 = 1e-4 * (3600 * lon - 26782.5);
        double phi1 =  1e-4 * (3600 * lat - 26782.5);
        return 1200147.07 + 308807.95 * phi1 - 3745.25 * Math.pow(delta1, 2) + 76.63 * Math.pow(phi1, 2) - 194.56 * Math.pow(delta1, 2) * phi1 + 119.79 * Math.pow(phi1, 3);
    };

    /**
     * Retourne la longitude dans le système WGS84 du point dont les coordonnées sont e et n dans le système suisse.
     * @param e coordonnée Est
     * @param n coordonnée Nord
     * @return la longitude
     */
    public static double lon(double e, double n) {
        double x = 1e-6 * (e - 2600000);
        double y = 1e-6 * (n - 1200000);
        double delta0 = 2.6779094 + 4.728982 * x + 0.791484 * x * y + 0.1306 * x * Math.pow(y, 2) - 0.0436 * Math.pow(x, 3);
        return Math.toRadians(delta0 * 100 / 36);
    }

    /**
     * Retourne la latitude dans le système WGS84 du point dont les coordonnées sont e et n dans le système suisse.
     * @param e coordonnée Est
     * @param n coordonnée Nord
     * @return la latitude
     */
    public static double lat(double e, double n) {
        double x = 1e-6 * (e - 2600000);
        double y = 1e-6 * (n - 1200000);
        double phi0 = 16.9023892 + 3.238272 * y - 0.270978 * Math.pow(x, 2) - 0.002528 * Math.pow(y, 2) - 0.0447 * Math.pow(x, 2) * y - 0.0140 * Math.pow(y, 3);
        return Math.toRadians(phi0 * 100 / 36);
    }
}
