#!/usr/bin/bash
echo "PLease make sure that RabbitMQ server is running."
read -p "Press p to proceed or any other key to exit: " choice
if [[ "$choice" != "p" ]]
then
  exit 0
fi

gnome-terminal -- sh  -c "bash -c \"java -jar ./balancer/target/Balancer.jar; exec bash\"" &
sleep 1
gnome-terminal -- sh  -c "bash -c \"./slowworker/run3; exec bash\"" &
sleep 1
gnome-terminal -- sh  -c "bash -c \"java -jar ./bomber/target/scala-2.13/Bomber.jar 3 100 10; exec bash\"" &
sleep 1
echo "Everything should be running and busy now."


