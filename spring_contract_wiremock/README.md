### **Blog Post: Embracing Spring Contracts for Web Development. Part 2**

Application migration is a very challenging task, to say the least. Even when business requirements is simply porting same functionality from one platform to another. Specially when you recognized  that the original application has lack of tests and you are short on both - time and human resources. We would like to share some examples of how Spring Cloud Contract can come to the rescue and demonstrate some non-obvious ways it can assist during the code transition process.

---

#### **What is Spring Cloud Contract?**

>[Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract) is an umbrella project holding solutions that help users in successfully implementing the Consumer Driven Contracts approach. Currently Spring Cloud Contract consists of the Spring Cloud Contract Verifier project. Spring Cloud Contract Verifier is a tool that enables Consumer Driven Contract (CDC) development of JVM-based applications. It ensures that services (both providers and consumers) can communicate without integration issues by defining a contract between them.  


---

### **Why we have chosen Spring Cloud Contract?**

We were tasked with migrating a medium-sized, IBM WebSphere-deployed monolithic enterprise application to the Azure cloud. The migration process involved several stages, one of which was the migration of the application's front page. The UI part, already developed using React.js, required minimal intervention. For the Java component, it was decided to migrate the functionality to the [Spring Reactor project](https://spring.io/reactive). Ultimately, the Java part should become a separate REST API microservice — a resource service protected by Okta.

In [Part 1](https://github.com/ibagroup-eu/app-modernization-examples/blob/feature-spring-contract-first/spring_contract_example/README.md) of this series, we described an approach to quickly enable integration tests for a migrated project using the Spring Cloud Contract framework to ensure compatibility between the original and migrated application. In this article we will show how Spring Contracts can be used to improve team collaboration. 

Since we had separate specialists for the UI and server side, we encountered an issue with unequal workload, as the majority of the tasks were concentrated on the server side. We resolved this by introducing Spring contracts, leveraging their integration capabilities with the [WireMock tool](https://wiremock.org/) to provide a lightweight replacement for back-end services while they are still in development. This server can be run locally or in any containerized environment and emulate back-end services by sending back captured responses.

As a result, front-end and server-side developers gained greater independence, which, in our case, allowed us to plan and implement additional UI enhancements during the migration.


---

### How to enable Spring Contracts 

We created sample project [spring_contract_wiremock](https://github.com/ibagroup-eu/app-modernization-examples/tree/main/spring_contract_wiremock) to demonstrate required settings and code examples. 

#### **Understanding the Project Structure**

Project structure:

- **/src/main/java**: The main source code for the example application.
- **/src/main/docker**: Contains docker compose file settings.

#### 1. **Maven settings**

Add required Spring Contracts libraries under dependencies section of your pom.xml file:

   ```xml
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-contract-wiremock</artifactId>
			<version>${spring-cloud-contract.version}</version>
		</dependency>
   ```
   
#### 2. **Spring boot application**

The Spring Cloud Contract WireMock modules allow you to use WireMock in a Spring Boot application. For a Spring Boot application that uses Tomcat as an embedded server (which is the default with `spring-boot-starter-web`), you should add the `@AutoConfigureWireMock` annotation to the main class to enable WireMock.

   ```java
		@SpringBootApplication
		@AutoConfigureWireMock
		public class SpringContractWireMockApp {
			
			public static void main(String[] args) {
				new SpringApplication(SpringContractWireMockApp.class).run(args);
			}
		}
   ```
   
Apart from auto-test generation, the Spring Cloud Contract plugin generates a JAR file containing stubs. This JAR file can be used to run a WireMock server in consumer tests. The stub JAR can be published to a centralized artifactory, such as a Maven repository, and added to consumer tests as needed.

We have stub jar created as result of enabling Spring Cloud Contract described in [Part 1](https://github.com/ibagroup-eu/app-modernization-examples/blob/feature-spring-contract-first/spring_contract_example/README.md) of this series.
Let's define it as a dependency in the application's `pom.xml` file.

   ```xml
		<!-- Spring contract stubs -->
		<dependency>
			<groupId>eu.ibagroup.app.modernization</groupId>
			<artifactId>spring-contract-example</artifactId>
			<version>1.0.0</version>
			<classifier>stubs</classifier>
		</dependency>
   ```

WireMock runs as a stub server, allowing to register stubs via the Java API or through static declarations. One way to declare stubs statically is to add the configuration to the `application.yml` file:

   ```yaml
wiremock:
   server:
      port: ${CONTRACT_WIREMOCK_PORT:8082}   
      stubs:      
      - "classpath*:/META-INF/**/mappings/**/*.json"
   ```

That's all. Now we can run the Spring Boot application.

For validation purposes, we can request a list of available stubs by calling the WireMock admin endpoint `__admin/mappings`:

```bash
curl 'http://localhost:8082/__admin/mappings'
   ```
   
### **Conclusion**

Spring contracts is a powerful tool which allows teams to detect and fix integration issues early in the development cycle, reduces integration testing overhead, even may serve as analogue of documentation. 

In this article, we build a lightweight back-end service replacement using contract stubs produced by the Spring Cloud Contract plugin. This approach allows our front-end team to start their implementation earlier. Using stubs generated from Spring Cloud Contracts ensures that both consumers and providers adhere to the defined expectations. This enables us to rely on the code developed using WireMock and reduces integration testing overhead when the back-end is ready.
