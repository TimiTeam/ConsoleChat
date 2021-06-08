#mvn clean && mvn package
echo $'\n------------ run MessengerClient -----------\n'
java -jar target/MessengerClient-1.0-SNAPSHOT.jar ua.unit.tbujalo.App
if [ $? != 0 ]
then

	echo $'^^ oops, jar file mau not exist ^^'
	echo $'\n-----------** RUN THE COMMAND BELOW, AND RUN SCRIPT AGAIN **-----------\n'
	echo $'mvn clean && mvn package\n'
fi
