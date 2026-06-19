package aggregator;

public class Master {
    
    public static void main(String[] args) {
        /*1) legge la porta da linea di comando
        *se non viene passata la porta, stampa le istruzioni di utilizzo 
        */
        if(args.length!=1){
            System.err.println("Uso: java Master <porta>");
            return;
        }

        int port;
        try{
            port=Integer.parseInt(args[0]);
        }catch(NumberFormatException e){
            System.err.println("Porta non valida: "+args[0]);
            return;
        }
    }
}
