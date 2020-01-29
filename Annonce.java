public class Annonce {

    private int id;
    private static int compteurId = 0;
    private String client;
    private String domaine;
    private int prix;
    private String descriptif;


    public Annonce(String client, String domaine, int prix, String descriptif){

        this.id = compteurId;
        compteurId++;
        this.client = client;
        this.domaine = domaine;
        this.prix = prix;
        this.descriptif = descriptif;

    }



    public int getId() {
        return id;
    }

    public String getClient() {
        return client;
    }

    public String getDomaine() {
        return domaine;
    }

    public int getPrix() {
        return prix;
    }

    public String getDescriptif() {
        return descriptif;
    }

}