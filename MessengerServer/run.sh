mvn clean && mvn package
if [ $? = 0 ]
then
echo $'\n\n\n--------------** RUNNTING COMMAND BELOW **-------------\n\n'
echo $'java -jar target/MessengerServer-1.0-SNAPSHOT.jar ua.unit.tbujalo.App\n\n'
java -jar target/MessengerServer-1.0-SNAPSHOT.jar ua.unit.tbujalo.App
fi
