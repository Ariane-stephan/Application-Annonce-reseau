import javax.crypto.Cipher;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class ClientProjet {


    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[34m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    static PublicKey myPublicKey;
    static HashMap<String, Ami> listeAmi = new HashMap<>();
    static byte[] publickeyAmi;
    static String monPseudo;
    static String adresseIPChoisi;
    static int portChoisi;

    public static void main(String []args) {

        if(args.length == 0) {
            adresseIPChoisi = "localhost";
            portChoisi = 2000;
        }
        else {
            adresseIPChoisi = args[0];
            portChoisi = Integer.parseInt(args[1]);
        }
        try {

            /****************creation des attributs reseaux****************/

            InetAddress adrServeur = InetAddress.getByName(adresseIPChoisi);
            Socket socket = new Socket(adrServeur, portChoisi);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            /****************creation des variables****************/

            String requete;
            String reponseServeur;
            String nomRequete;
            String requeteParse;

            /****************creation des cle et encodage****************/


            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keypair = keyGen.genKeyPair();
            byte[] privateKey = keypair.getPrivate().getEncoded();
            myPublicKey = keypair.getPublic();
            byte[] encryptedBytes = keypair.getPublic().getEncoded();
            String publicKeyEncode = Base64.getEncoder().encodeToString(encryptedBytes);



            /*********** attend que le client soit connecte avec un pseudo validé par le serveur *********/

            while (!connexion(br, pw, socket, privateKey, publicKeyEncode)) {
                continue;
            }



            /****** une fois connecté le client echange en continue avec le serveur **********/

            while (true) {
                /******* recupere la requete que le client va envoyer ***********/

                requete = requeteApresConnexion();


                /******** recupere le nom de la requete *********/
                nomRequete = requete.substring(1);
                nomRequete = nomRequete.split("\\s")[0];

                requeteParse = requete.substring(1);

                String[] messageTableau = requeteParse.split("\\s+", 5);

                if (messageTableau[0].equals("msg")) {

                    envoieMessage(messageTableau);

                } else {

                    pw.print(requete);
                    pw.flush();
                    reponseServeur = br.readLine();


                    messageTableau = reponseServeur.split("#");


                    /******** si le serveur a repondu positivement *********/
                    if (messageTableau[0].equals("T")) {

                        /****** si on a demandé les infos d'un client **********/

                        if (nomRequete.equals("info")) {
                            Ami ami = enregistrementAmi(messageTableau);
                            System.out.println(ANSI_GREEN + "Votre ami " + ami.getNom() + " a été enregistré\n"+ ANSI_RESET);

                        }
                        /******** si on a demandé à afficher des annonces *********/

                        else if (nomRequete.equals("getAnnonce") || nomRequete.equals("getMyAnnonce")) {
                            messageTableau = reponseServeur.split("#");
                            for (int i = 0; i < messageTableau.length; i++) {
                                System.out.println(ANSI_GREEN + messageTableau[i] + ANSI_RESET);
                                System.out.println();

                            }

                        }
                        else
                            System.out.println(ANSI_GREEN + "Message recu du serveur : " + messageTableau[1] + ANSI_RESET);

                    } else
                        System.out.println(ANSI_RED + "Message recu du serveur : " + messageTableau[1]+ ANSI_RESET);
                }


            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }



    /********* enregistre l'ami dans la liste ***********/

    public static Ami enregistrementAmi(String messageTableau[]){

        publickeyAmi = Base64.getDecoder().decode(messageTableau[4]);
        Ami ami = new Ami(messageTableau[1], "/" + messageTableau[2], Integer.parseInt(messageTableau[3]), publickeyAmi);
        listeAmi.put(messageTableau[1], ami);
        return ami;
    }

    /********* envoie le message en UDP ***********/

    public static void envoieMessage(String messageTableau[]) {
        try {
            DatagramSocket dso = new DatagramSocket();

            byte[] data;
            String msg = monPseudo;
            msg += " " + messageTableau[4];
            msg = encrypt(publickeyAmi, msg.getBytes());
            data = msg.getBytes();

            String adresse = messageTableau[3].substring(1);
            InetSocketAddress ia = new
                    InetSocketAddress(InetAddress.getByName(adresse), Integer.parseInt(messageTableau[2]));
            DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
            dso.send(paquet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***** verifie si le serveur a accepté le pseudo du client pour qu'il se connecte ****/

    public static boolean connexion(BufferedReader br, PrintWriter pw, Socket socket, byte[] privateKey, String res) {
        String requeteEnvoie = requeteAvantConnexion();
        try {

            /****** envoie la requete avec la cle public ******/

            pw.print(requeteEnvoie + " " + res + "\n");
            pw.flush();

            /****** lit la réponse du client *******/


            String mess = br.readLine();

            /******* parse la réponse ******/

            String[] messageTableau = mess.split("\\s+");

            /******* si le serveur a accepté ****/

            if (messageTableau[0].equals("T")) {

                System.out.println(ANSI_GREEN + "Connexion ok" + ANSI_RESET);

                /******on créé le thread qui gere la reception des messages UDP *******/

                ClientUDP clientUdp = new ClientUDP(socket, privateKey);
                clientUdp.start();
                return true;


                /*********** si le serveur a refusé **********/

            } else {
                System.out.println(ANSI_RED + "Pseudo déjà utilisé "+ ANSI_RESET);
                return false;
            }

        } catch (Exception e) {
            return false;
        }


    }


    /***** prepare la requete /connect ***/

    public static String requeteAvantConnexion() {

        Scanner inFromUser = new Scanner(System.in);
        String messageTableau[];
        boolean pseudoFormeValide = false;
        System.out.println("Entrez un pseudo ");
        String pseudo = "";
        while (!pseudoFormeValide) {
            pseudo = inFromUser.nextLine();

            messageTableau = pseudo.split("\\s+");
            if (messageTableau.length != 1) {
                System.out.println("Vous devez choisir un seul pseudo");
            } else
                pseudoFormeValide = true;
        }
        monPseudo = pseudo;
        return "/connect " + pseudo;

    }


    /***** prepare les requetes une fois connecté ***/


    public static String requeteApresConnexion() throws Exception {

        Scanner inFromUser = new Scanner(System.in);
        System.out.println("Que voulez vous faire? Tapez :\n2 pour une annonce\n3 pour obtenir toutes les annonces disponibles\n4 pour obtenir toutes vos annonces\n5 pour supprimer une de vos annonces\n6 pour obtenir les infos des clients\n7 pour envoyer un message\n****************************");
        System.out.println();


        while (true) {
            int clavier = -1;
            try {
                /**** verifie si on entre un nombre *********/

                clavier = Integer.parseInt(inFromUser.nextLine());
            } catch (Exception e) {
            }
            switch (clavier) {
                case 2:
                    return reqAnnonce();

                case 3:
                    return "/getAnnonce\n";
                case 4:
                    return "/getMyAnnonce\n";
                case 5:
                    return reqRemove();

                case 6:
                    System.out.println("Quel utilisateur ?");
                    return "/info " + inFromUser.nextLine() + "\n";
                case 7:
                    String req = reqMsg();
                    if(req != null)
                        return req;
                    System.out.println();

                    System.out.println("Que voulez vous faire? Tapez :\n2 pour une annonce\n3 pour obtenir toutes les annonces disponibles\n4 pour obtenir toutes vos annonces\n5 pour supprimer une de vos annonces\n6 pour obtenir les infos des clients\n7 pour envoyer un message\n****************************");
                    System.out.println();

                    break;

                default:
                    System.out.println("Tapper un num entre 2 et 7" + clavier);
            }
        }
    }


    /************* prepare la requete d'une annonce ************/

    private static String reqAnnonce() {

        int nombre;
        String reponse;
        String reponseClavier;
        String[] reponseClavierTableau = new String[1];
        reponseClavierTableau[0] = "";
        String requete = "/annonce ";
        Scanner inFromUser = new Scanner(System.in);
        boolean test = false;


        requete = "/annonce ";

        System.out.println("Entrez un domaine ");

        /********demande un domaine sans espace ***********/

        while (!test) {

            reponseClavier = inFromUser.nextLine();
            reponseClavierTableau = reponseClavier.split("\\s+");

            if (reponseClavierTableau.length != 1) {
                System.out.println("Entrez un domaine sans espace ");

            } else
                test = true;
        }

        test = false;

        requete += reponseClavierTableau[0] + " ";

        /******** demande un prix sans espace ***********/

        while (!test) {
            System.out.println("Entrez un prix ");
            reponseClavier = inFromUser.nextLine();
            reponseClavierTableau = reponseClavier.split("\\s+");

            if (reponseClavierTableau.length != 1) {
                System.out.println("Entrez un seul prix");
            } else {
                try {
                    nombre = Integer.parseInt(reponseClavier);
                    test = true;
                } catch (Exception e) {
                    System.out.println("Entrez un nombre ");
                }

            }
        }


        requete += reponseClavierTableau[0] + " ";
        System.out.println("Entrez une description ");

        /******* recupere la description et envoie la requete complete *********/

        return requete += inFromUser.nextLine() + "\n";


    }


    /*********** prepare la requete du remove ***********/

    private static String reqRemove() {
        int nombre = -1;
        System.out.println("Quelle annonce voulez vous supprimer?");

        Scanner inFromUser = new Scanner(System.in);
        String requete;
        String reponseClavier;
        String[] reponseClavierTableau;
        boolean test = false;

        requete = "/remove ";
        while(!test) {
            reponseClavier = inFromUser.nextLine();
            try {
                nombre = Integer.parseInt(reponseClavier);
                test = true;
            } catch (Exception e) {
                System.out.println("Entrez un nombre ");
            }
        }
        requete += nombre + "\n";

        return requete;
    }


    /************ prepare la requete du message ***********/

    private static String reqMsg() throws Exception {


        String requete = "/msg ";
        int nombre;
        System.out.println("A qui voulez vous envoyer le message ?");
        boolean test = false;
        Scanner inFromUser = new Scanner(System.in);
        String reponseClavier;


        /****** verifie si l'ami existe **********/

        while (!test) {
            reponseClavier = inFromUser.nextLine();

            if (listeAmi.containsKey(reponseClavier)) {
                requete += listeAmi.get(reponseClavier).getNom() + " " + listeAmi.get(reponseClavier).getPort() + " " + listeAmi.get(reponseClavier).getAdresse() + " ";
                publickeyAmi = listeAmi.get(reponseClavier).getKeyPublic();
                test = true;
            } else {
                System.out.println("Personne introuvable.");
                return null;
            }
        }
        System.out.println("Quel est le message ?");
        reponseClavier = inFromUser.nextLine();
        requete += reponseClavier;


        return requete;
    }


    /******** methode pour encrypter le message ***********/

    public static String encrypt(byte[] publicKey, byte[] inputData) throws Exception {

        PublicKey key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(inputData);
        encryptedBytes = Base64.getEncoder().encode(encryptedBytes);
        return new String(encryptedBytes);
    }

}