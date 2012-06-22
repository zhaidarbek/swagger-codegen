#!/bin/bash
if [ $# -ne 4 ]
then
    echo "Error in $0 - Invalid Argument Count"
    echo "Syntax: $0 location_of_service api_key package_name library_root"
    exit
fi

echo "" > classpath.txt
for file in `ls target/lib`;
        do echo -n 'target/lib/' >> classpath.txt;
        echo -n $file >> classpath.txt;
        echo -n ':' >> classpath.txt;
done
for file in `ls target/*.jar`;
	do echo -n '' >> classpath.txt;
	echo -n $file >> classpath.txt;
	echo -n ':' >> classpath.txt;
done

export CLASSPATH=$(cat classpath.txt)
export JAVA_OPTS="${JAVA_OPTS} -Dproperty=Xmx2g"
java $WORDNIK_OPTS $JAVA_CONFIG_OPTIONS $JAVA_OPTS -cp $CLASSPATH com.wordnik.swagger.codegen.config.java.JavaLibCodeGen "$@"

if [[ $1 == *Billing* || $1 == *billing* ]]
then
    cp conf/java/sample/billing_pom.xml $4/pom.xml
else
    cp conf/java/sample/platform_pom.xml $4/pom.xml
fi

