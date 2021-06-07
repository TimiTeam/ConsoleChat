mvn clean && mvn package
if [ $? = 0]
then
echo $'\n\n\n--------------** EXECUTE LINE BELOW **-------------\n\n'
echo "java -jar target/MessagerClient-1.0-SNAPSHOT.jar ua.unit.tbujalo.App"
fi
