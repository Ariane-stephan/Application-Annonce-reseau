import java.net.ServerSocket;
import java.net.Socket;

public class ServeurProjet {

    static int portChoisi;

    public static void main(String[]args){

        if(args.length == 0)
            portChoisi = 2000;
        else{
            portChoisi = Integer.parseInt(args[0]);
        }
        try{

            ServerSocket server=new ServerSocket(portChoisi);

            int port = 7000;

            while(true){

                Socket socket=server.accept();
                port++;
                ServeurThreadProjet serveur = new ServeurThreadProjet(socket, port);
                Thread t=new Thread(serveur);
                t.start();
            }

        }catch(Exception e){

        }

    }
}