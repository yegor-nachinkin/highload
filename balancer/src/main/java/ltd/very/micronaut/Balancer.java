package ltd.very.micronaut;

import io.micronaut.runtime.Micronaut;
import io.micronaut.context.ApplicationContext;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

import java.util.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.*;
import java.util.function.*;

import java.io.File;

@Controller
public class Balancer {
	
	public static ApplicationContext ac;
	public static ConnectionFactory factory;
	public static Connection connection;
	public static Channel taskChannel;
	public static Channel responseChannel;
	public static String taskQueueName = "tasks";	
	public static long maxWaitMillis = 20000L;
	
	@Get("/reverse/{word}")
    @Produces(MediaType.TEXT_PLAIN) 
    public String reverse(final String word) {
		String id = java.util.UUID.randomUUID().toString();
		String msg = id + ":" + word;
		final byte[] mybytes = new byte[512];
		final boolean[] success = new boolean[1]; success[0] = false;
		try{
			 responseChannel.queueDeclare(id, false, false, false, null);
			 // it is vital for response channel to start listening on response queue
			 // BEFORE the task message is sent out!
		     responseChannel.basicConsume(id, true, (consumerTag, delivery) -> {
                     byte[] in = delivery.getBody();
                     for(int x = 0; x < in.length; x++){mybytes[x] = in[x];}
                     success[0] = true;
                     responseChannel.queueDelete(id);
                  }, consumerTag -> { });
             taskChannel.basicPublish("", taskQueueName, null, msg.getBytes(StandardCharsets.UTF_8));
	         System.out.println("Sent: " + msg);                  
        }catch(Exception eee){eee.printStackTrace();}
        String res = "";
        int p = 0;
        while(!success[0]){
			// System.out.println("Waiting for " + id);
			try{Thread.sleep(1L);}catch(Exception ee){res = "response: none"; ee.printStackTrace();}
			p++;
			if(p > maxWaitMillis){break;}
		}
        // remove trailing zero bytes !
        try{res = (new String(mybytes, "UTF-8").replaceAll("\0", ""));}catch(Exception ex){res = "response: bad"; ex.printStackTrace();} 
        System.out.println("Received: " + res + " <-- " + word + " (" + String.valueOf(p) + ")");
        return res + "\n";   
    }	
    
	@Get("/reverseself/{word}")
    @Produces(MediaType.TEXT_PLAIN) 
    public String reverseself(String word) {
      // some slow work here
            for(int i = 1; i < 6; i++){try{Thread.sleep(500);System.out.println("slow " + String.valueOf(i));}catch(Exception e){e.printStackTrace();}}
            StringBuilder sb = new StringBuilder();
            sb.append(word);
       // --- some slow work here	
       String s = "Work done: " + sb.reverse().toString() + "\n";
       System.out.println(s);	
       return s; 
    }
	
	@Get("/")
    @Produces(MediaType.TEXT_PLAIN) 
    public String index() {
        return "Balancer is alive.\n"; 
    }
	
	@Get("/stop")
    @Produces(MediaType.TEXT_PLAIN) 
    public String stop() {
        new Thread(new Runnable(){
			   @Override
			   public void run(){
				   System.out.println("Stopping...");
				   try{
					   taskChannel.queueDelete(taskQueueName);
					   taskChannel.close();
					   responseChannel.close();
					   connection.close();
					   Thread.sleep(1000L);
					  }catch(Exception e){}
				   Balancer.ac.stop();
				   System.exit(0);
			   }
			}).start();
        return "Stopping...\n"; 
    }
	
	@Get("/stopall")
    @Produces(MediaType.TEXT_PLAIN) 
    public String stopall() {
        new Thread(new Runnable(){
			   @Override
			   public void run(){
				   try{
                   String[] splt = (new File(Balancer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()).toString().split(System.getProperty("file.separator")); //s.split(System.getProperty("file.separator"));
                   String[] b = Arrays.copyOf(splt, splt.length - 2);
                   String killerCommand = String.join(System.getProperty("file.separator"), b) + System.getProperty("file.separator") + "killall.pl";
                   System.out.println("Killer command: " + killerCommand);
		              for(int x = 0; x < 20; x++){
		              taskChannel.basicPublish("", taskQueueName, null, "stop".getBytes(StandardCharsets.UTF_8));
	                  System.out.println("Sent stop signal " + String.valueOf(x));
				      }
				      CompletableFuture<Integer> perlResult = CompletableFuture.supplyAsync(new Supplier<Integer>(){
                          @Override
                          public Integer get() {
                            int res = -1;
                            try{
                                Process cmdProc = Runtime.getRuntime().exec(killerCommand);
								res = cmdProc.waitFor();
							}catch(Exception ee){ee.printStackTrace();}
                            return res;
                          }
                      });
				      System.out.println("Perl killer returned " + String.valueOf(perlResult.get()));
				   }catch(Exception e){e.printStackTrace();}
				   System.out.println("Stopping all...");
				   try{
					   taskChannel.queueDelete(taskQueueName);
					   taskChannel.close();
					   responseChannel.close();
					   connection.close();
					   Thread.sleep(1000L);
					  }catch(Exception e){}
				   Balancer.ac.stop();
				   System.exit(0);
			   }
			}).start();
        return "Stopping...\n"; 
    }	
	
    public static void main(String[] args) {
        ac = Micronaut.run(Balancer.class, args);
        try{
          factory = new ConnectionFactory();
          factory.setHost("localhost");
          connection = factory.newConnection();
          taskChannel = connection.createChannel();
          responseChannel = connection.createChannel();
          taskChannel.queueDeclare(taskQueueName, false, false, false, null);
          if((args.length > 0) && (args[0].equals("test"))){
            String testMessage = "test task";
            taskChannel.basicPublish("", taskQueueName, null, testMessage.getBytes(StandardCharsets.UTF_8));
	      }
        }catch(Exception e){e.printStackTrace();}
    }
}
