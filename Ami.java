import java.security.PublicKey;

public class Ami {

    private int port;
    private String adresse;
    private String nom;
    private byte[] keyPublic;

    public Ami(String nom, String adresse, int port, byte[] keyPublic){
        this.adresse = adresse;
        this.port = port;
        this.nom = nom;
        this.keyPublic = keyPublic;
    }

    public byte[] getKeyPublic() {
        return keyPublic;
    }

    public int getPort() {
        return port;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getNom() {
        return nom;
    }

}