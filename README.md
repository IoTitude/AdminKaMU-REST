# AdminKaMU-REST
RESTful API for AdminKaMU

##Installation
Instructions for installing AdminKaMU-REST.

###Minimum requirements
* NetBeans IDE
* GlassFish 4.1
* Oracle JDK 7 Update 65+
* Cloned AdminKaMU-REST project

###Configuring NetBeans to run GlassFish server
Start NetBeans --> Tools --> Add Server

Choose GlassFish server and click next. In the installation location field browse to your downloaded GlassFish folder and click next, then enter the domain, host and ports

###Starting the program
Once you've configured the server, open AdminKaMU-REST project in NetBeans, deploy the project and then run it.

##Development tools
| Tool | Version | 
|:----:|:----:|:-----:|
|NetBeans IDE| 8.1 |
|GlassFish| 4.1 |
|Postman Chrome App| 4.4.2 |
|Google Chrome|Version 51.0.2704.106 (64-bit)|
|WizTools.org RESTClient|3.6|
|Ubuntu Linux|14.04 LTS|

## Problems with development

For some reason further development of the service proved very difficult. Trying to change the Kaa SDK resulted in a completely broken REST API. The main error was `exception java.lang.NoClassDefFoundError: Could not initialize class iotitude.com.RestController`. Most likely this was related to something being broken in the Kaa SDK that was required by AdminKaMU in order to control the KaMUs. Probably the service was unable to reach the Kaa server and that caused the exception during the initialization of the RestController.

At some point it became possible to run AdminKaMU on a local machine. However it just didn't run in a container and doing so resulted to the same kind of errors. The final problems could not be solved in time before the end of the project.
