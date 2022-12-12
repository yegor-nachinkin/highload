import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class SlowWorker {

    public static String inQueueName = "inqueue";
    public static String outQueueName = "outqueue";
    public static long counter = 0;
    
    public static long waitTime = 10L;
    public static int repeatWait = 6;
    

    public static void main(String[] argv) throws Exception {
		if(argv.length > 0){inQueueName = argv[0];}
		if(argv.length > 1){outQueueName = argv[1];}
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        // create two channels: one for listening, one for publishing
        Channel channelIn = connection.createChannel();
        final Channel channelOut = connection.createChannel();

        channelIn.queueDeclare(inQueueName, false, false, false, null);
        System.out.println("Waiting for messages from queue: " + inQueueName);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageIn = new String(delivery.getBody(), "UTF-8");
            counter++;
            System.out.println("Received '" + messageIn + "' " + String.valueOf(counter));
            if(messageIn.equals("stop")){
				try{
					channelIn.close();
					channelOut.close();
					connection.close();
				}catch(Exception e){e.printStackTrace();}
				System.exit(0);
			}
            // message has the format unique id:content
            // this unique id is also the name of the queue created by balancer to get response from worker,
            // e.g. c70cd6d0-12fe-48cd-9d84-a39337a8ccca:mymessage
            // e.g. 52fb65f4-2513-4f8f-ab8f-4451d32a5189:amessage
            String[] parts = messageIn.split(":");
            outQueueName = parts[0];
            // some slow work here
            if((waitTime > 0) && (repeatWait > 1)){
              for(int i = 1; i < repeatWait; i++){try{Thread.sleep(waitTime);System.out.println("slow " + String.valueOf(i));}catch(Exception e){e.printStackTrace();}}
		    }
            StringBuilder sb = new StringBuilder();
            sb.append(parts[1]);
            String messageOut = sb.reverse().toString();
            // --- some slow work here
            // send work done message
            try{
                // no need to declare this queue, it has been declared by balancer
                channelOut.basicPublish("", outQueueName, null, messageOut.getBytes(StandardCharsets.UTF_8));
                System.out.println("Sent '" + messageOut + "'");
            }catch(Exception e){e.printStackTrace();}
            // --- send work done message
        };
        
        channelIn.basicConsume(inQueueName, true, deliverCallback, consumerTag -> { });
    }
}
