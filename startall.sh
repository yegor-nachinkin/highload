#!/usr/bin/bash
echo "PLease make sure that RabbitMQ server is running."
read -p "Press p to proceed or any other key to exit: " choice
if [[ "$choice" != "p" ]]
then
  exit 0
fi

cd balancer
mvn clean package
cd ../slowworker
./compile
cd ../bomber
sbt clean assembly

echo ""
echo "+++++++++++++++++++++++++++++++++++++++++++"
echo ""
echo "Everything compiled, launching proggies..."
echo ""

gnome-terminal -- sh  -c "bash -c \"java -jar ../balancer/target/Balancer.jar; exec bash\"" &
sleep 1
gnome-terminal -- sh  -c "bash -c \"../slowworker/run3; exec bash\"" &
sleep 1
gnome-terminal -- sh  -c "bash -c \"java -jar target/scala-2.13/Bomber.jar 3 100 10; exec bash\"" &
sleep 1
echo "Everything should be running and busy now."


