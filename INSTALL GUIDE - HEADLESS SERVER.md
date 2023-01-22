# Install cHIMS in a Headless production server.

## Table of Contents.

- Install the OS and the necessary packages.

- Preparing the database.

- Downloading and configuring Payara Server 5.x.

- Configure JDBC MySQL connector.

- Configuring Payara Server.

- Installing CHIMS.

- Troubleshooting.

- Upgrading Cloud HIMS.

## Install the OS and the necessary packages.

Download and install the latest Ubuntu Server in your headless Linux server or a cloud VM (e.g., Ubuntu Server 22.10 x64).

You can install the minimised version to limit resource usage.

Select install `Open SSH Server` 

Connecting to the Ubuntu Server through SSH.

```shell
ssh USER_NAME@SERVER_IP_ADDRESS
```

Updating the system

```bash
sudo apt update

sudo apt upgrade
```

#### Install `JDK`.

```bash
java --version
```

```bash
sudo apt install openjdk-11-jdk
```

#### Install `git`.

```bash
sudo apt install git
```

#### Install `zip`.

```bash
sudo apt install zip
```

#### Installing `MySql server`.

```bash
sudo apt install mysql-server
```

#### Install Apache Maven.

```bash
sudo apt install maven
```

## Preparing the database.

```bash
sudo mysql -u root -p
```

Configure the root password

```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'ROOT_PASSWORD';
```

Create additional user

```sql
CREATE USER 'USER_NAME'@'localhost' IDENTIFIED WITH mysql_native_password BY 'USER_PASSWORD';
```

Create the database

```sql
create database DATABASE_NAME ;
```

```sql
GRANT ALL PRIVILEGES ON DATABASE_NAME.* TO 'USER_NAME'@'localhost';
```

Uploading a sample/backup dataset - Optional.

```sql
use DATABASE_NAME;

source ./SOURCE_PATH/SOURCE.sql;

Exit;
```

## Downloading and configuring Payara Server 5.x.

```bash
wget https://s3.eu-west-1.amazonaws.com/payara.fish/Payara+Downloads/5.2022.5/payara-5.2022.5.zip?hsCtaTracking=7cca6202-06a3-4c29-aee0-ca58af60528a%7Cb9609f35-f630-492f-b3c0-238fc55f489b -O payara-5.2022.5.zip
```

```bash
unzip payara-5.2022.5.zip
```

Start Payara Server

```bash
./payara5/bin/asadmin start-domain
```

Change admin password

```bash
./payara5/bin/asadmin change-admin-password
```

Enable secure admin

```shell
./payara5/bin/asadmin enable-secure-admin
```

Restart the Payara Server 

```shell
./payara5/bin/asadmin restart-domain
```

Once the server is up and running, navigate to `http://SERVER_IP_ADDRESS:4848` in your web browser to access the console.

## Configure JDBC MySQL connector.

Download JDBC MySQL connector

```shell
wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-j-8.0.31.zip
```

Extract the archive.

```shell
unzip mysql-connector-j-8.0.31.zip
```

Add the JDBC MySQL connector to Payara Server

```shell
./payara5/bin/asadmin add-library ./mysql-connector-j-8.0.31/mysql-connector-j-8.0.31.jar
```

Restart the Payara server.

```shell
./payara5/bin/asadmin restart-domain
```

## Configuring Payara Server.

After configuring the JDBC MySQL connector, navigate to `http://SERVER_IP_ADDRESS:4848` in your web browser to access the console.

### Configuring JDBC Connection Pool.

- **Pool Name:** chims (A name for your Pool)

- **Resource Type:** javax.sql.DataSource

- **Database Driver Vendor:** MySql8

- **Additional Properties**:

| Name                   | Value                                     |
| ---------------------- | ----------------------------------------- |
| ServerName             | localhost                                 |
| PortNumber             | 3306                                      |
| DatabaseName           | DATABASE_NAME                             |
| User                   | DATABASE_USER                             |
| Password               | DATABASE_PASSWORD                         |
| UseSSL                 | false                                     |
| allowPublicKeyRetrival | true                                      |
| URL                    | jdbc:mysql://localhost:3306/DATABASE_NAME |

Click **Ping** and look for **✅ Ping Succeeded**

### Configuring JDBC Resources.

- **JNDI Name:** jdbc/chims

- **Pool Name:** chims

## Installing cHIMS.

Obtaining the source code.

```bash
git clone https://github.com/lk-gov-health-hiu/chims.git`
```

Packaging and deploying the project.

```shell
mvn package -f chims
```

Deploying the cHIMS Application.

```shell
./payara5/bin/asadmin deploy ./chims/target/WAR_FILE_NAME.war
```

In the Payara Server Console, go to **Applications**, and under the **Deployed Applications** section, click **Launch** for the deployed application (chims-0.1). 

It will open a new window, and it will have the links to the launched application. 

E.g., `http://SERVER_NAME:8080/chims`

While the application is deployed on a remote server, we need to replace the server name with the IP address. 

E.g., `http://SERVER_IP_ADDRESS:8080/chims`

After creating the user and the institution, you can log in and use/customise the system.

## Troubleshooting.

Look at the error message and check the log file.

Sometimes you may need to edit the following files before packaging the project into a .`war` file.

`./pom.xml`
`./src/main/resources/META-INF/persistence.xml`
`./src/main/webapp/WEB-INF/glassfish-web.xml`

## Upgrading cHIMS.

Back up the database and the previous installation.

Change to the chims directory.

```shell
cd chims
```

Pull the updated source codes from GitHub.

```shell
git pull
```

Package the project into a .`war` file

```shell
mvn package -f chims
```

Force deploy the new `.war` file.

```shell
./payara5/bin/asadmin deploy --force ./chims/target/chims-0.1.war
```
