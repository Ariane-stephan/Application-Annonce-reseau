import java.io.IOException;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Utilisateur  {

    private String pseudo;
    private InetAddress adresse;
    private int port;
    private ArrayList<Annonce> listeAnnonces;
    private String keyPubllic;


    public Utilisateur(String pseudo, InetAddress adresse, int port, String keyPublic) {
        this.pseudo = pseudo;
        this.adresse = adresse;
        this.port = port;
        listeAnnonces = new ArrayList<Annonce>();
        this.keyPubllic = keyPublic;
    }
    public Annonce getAnnonce(int id){
        for(Annonce a : listeAnnonces){
            if(a.getId() == id)
                return a;
        }
        return null;
    }
    @Override
    public String toString() {
     System.out.println("ici " +   InetAddress.getLoopbackAddress().getHostAddress());
        System.out.println(adresse.toString().substring(1));
        return  "#"+pseudo + "#" + adresse.toString().substring(1) + "#" + port+"#"+keyPubllic+"\n";
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getKeyPubllic() {
        return keyPubllic;
    }

    public ArrayList<Annonce> getListeAnnonces() {
        return listeAnnonces;
    }

    public String supprimerAnnonce(int i) {
        if (getAnnonce(i) == null)
            return "F#L annonce n a pas été trouvé";
        else
            listeAnnonces.remove(getAnnonce(i));
        System.out.println("Fins fonction");

        return "T#L annonce a bien été supprimé";
    }

    public String afficherListeAnnonce() {
        String msg = "";
        if (listeAnnonces.size() > 0) {
            for (int i = 0; i < listeAnnonces.size(); i++)
                msg += "Annonce numero : " + listeAnnonces.get(i).getId() + " de : " + listeAnnonces.get(i).getClient() + " du domaine : " + listeAnnonces.get(i).getDomaine() + " au prix : " + listeAnnonces.get(i).getPrix() + " euros. " + "Descriptif : " + listeAnnonces.get(i).getDescriptif()+"#";

            return msg;

        } else
            return "Pas d'annonce";
    }

    public String chatAmi(Utilisateur ami, String message) {
        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName(ami.adresse + "");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        byte[] sendData = new byte[1024];
        sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876); // port de ami ou le tiens
        try {
            assert clientSocket != null;
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Message envoyé a l'ami " + ami.getPseudo();

    }



}