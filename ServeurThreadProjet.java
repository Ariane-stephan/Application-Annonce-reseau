import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Clock;
import java.util.ArrayList;


public class ServeurThreadProjet implements Runnable {


    public Socket socket;

    private Utilisateur utilisateur;
    private int port;
    static ArrayList<Utilisateur> listeUtilisateurs = new ArrayList<Utilisateur>();


    public ServeurThreadProjet(Socket s, int port) {
        this.socket = s;
        this.port = port;

    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }




    public void run() {

        try {


            /********* initialisation des variables reseaux ******/

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            String sentence;
            String reponse;
            sentence = br.readLine();
            System.out.println("Voici requete du client : "+sentence);

            sentence = sentence.substring(1);
            String[] messageTableau = sentence.split("\\s+");
            boolean test = false;


            /******** attend que le client ai envoyé un pseudo valide *********/

            while (!test) {

                /********* si le pseudo est valide **********/

                if (connexion(messageTableau)) {

                    utilisateur = new Utilisateur(messageTableau[1], socket.getInetAddress(), socket.getPort(), messageTableau[2]);
                    reponse = traitementRequete(messageTableau[0], messageTableau, socket.getInetAddress(), this.port);
                    listeUtilisateurs.add(utilisateur);
                    System.out.println("Voici reponse du serveur : "+reponse);

                    pw.print(reponse);
                    pw.flush();
                    test = true;


                } else {
                    reponse = "F" + "\n";
                    System.out.println("Voici reponse du serveur : "+reponse);

                    pw.print(reponse);
                    pw.flush();
                    sentence = br.readLine();

                    sentence = sentence.substring(1);
                    messageTableau = sentence.split("\\s+", 4);
                }
            }


            /********** une fois le client connecte avec un bon pseudo le serveur ecoute et repond en continue *************/

            while (true) {

                sentence = br.readLine();

                System.out.println("Voici requete du client "+sentence);

                sentence = sentence.substring(1);

                messageTableau = sentence.split("\\s+", 4);

                reponse = traitementRequete(messageTableau[0], messageTableau, socket.getInetAddress(), socket.getPort());

                System.out.println("Voici reponse du serveur : "+reponse);
                pw.print(reponse);
                pw.flush();
            }


        } catch (Exception e) {

        }
    }


    /******** verifie si un utilisateur existe ***********/

    public boolean utilisateurExist(String pseudo) {
        for (Utilisateur utilisateur : ServeurThreadProjet.listeUtilisateurs) {
            if (utilisateur.getPseudo().equals(pseudo))
                return true;
        }
        return false;
    }


    /******** recupere un utilisateur avec son pseudo ***********/

    public Utilisateur getUtilisateur(String pseudo) {
        for (Utilisateur utilisateur : listeUtilisateurs) {
            if (utilisateur.getPseudo().equals(pseudo))
                return utilisateur;
        }
        return null;
    }


    /******* recupere toutes les annonces **********/

    public String getAllAnnonces() {
        String msg = "";
        for (Utilisateur utilisateur : listeUtilisateurs) {
            if (!(utilisateur.afficherListeAnnonce().equals("Pas d'annonce"))) {
                msg += utilisateur.afficherListeAnnonce();
                System.out.println("Dans le serveur "+ msg);

            }
        }
        if (msg.equals(""))
            return "Aucune annonce enregistrée pour l’instant.";
        return msg;
    }

    /******* recupere toutes les annonces de l'utilisateur**********/


    public String getMyAnnonces(){
        if(utilisateur.afficherListeAnnonce().equals("Pas d'annonce"))
            return "Vous n avez posté aucune annonce";
        else return utilisateur.afficherListeAnnonce();
    }


    public boolean connexion(String[] messageTableau) {
        if ( messageTableau.length ==3 && messageTableau[0].equals("connect") && !utilisateurExist(messageTableau[1])) {
            System.out.println(messageTableau.length + "  " +messageTableau[1]);
            return true;
        }
        return false;
    }


    public String traitementRequete(String requete, String[] messageTableau, InetAddress adressClient, int portClient) {
        int nombre;
        switch (requete) {

            case "annonce":
                try{
                    nombre = Integer.parseInt(messageTableau[2]);
                }catch (Exception e){
                    return "F#Mauvaise annonce\n";
                }
                utilisateur.getListeAnnonces().add(new Annonce(utilisateur.getPseudo(), messageTableau[1],nombre, messageTableau[3]));
                return "T#Annonce mise en ligne\n";

            case "getAnnonce":
                if(messageTableau.length != 1)
                    return "F#Mauvais argument \n";

                return "T#"+getAllAnnonces() + "\n";

            case "getMyAnnonce":
                return "T#"+getMyAnnonces()+"\n";

            case "remove":
                if(messageTableau.length != 2)
                    return "F#Mauvais arguments\n";
                else{
                    return utilisateur.supprimerAnnonce(Integer.parseInt(messageTableau[1]))+" \n";
                }
            case "connect":

                return "T\n";

            case "info":
                System.out.println("Je passe info "+messageTableau[1]);
                if (!utilisateurExist(messageTableau[1]) )
                    return "F#Le pseudo de l'utilisateur n'existe pas dans la base de données\n";

                else return "T"+getUtilisateur(messageTableau[1]).toString();

            case "msg":
                if (!utilisateurExist(messageTableau[1]))
                    return "Utilisateur inexistant\n";
                else{
                    for (Utilisateur utilisateurTmp : listeUtilisateurs) {
                        if (messageTableau[1].equals(utilisateurTmp.getPseudo()))
                            return (utilisateur.chatAmi(utilisateurTmp, messageTableau[2]));

                    }
                }


            default:

                return "Erreur de syntaxe\n";

        }

    }

}
