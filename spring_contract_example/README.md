### **Blog Post: Embracing Spring Contracts for Web Development. Part 1.**

Application migration is a very challenging task, to say the least. Even when business requirements is simply porting same functionality from one platform to another. Specially when you recognized  that the original application has lack of tests and you are short on both - time and human resources. We would like to share some examples of how Spring Cloud Contract can come to the rescue and demonstrate some non-obvious ways it can assist during the code transition process.

---

#### **What is Spring Cloud Contract?**

>[Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract) is an umbrella project holding solutions that help users in successfully implementing the Consumer Driven Contracts approach. Currently Spring Cloud Contract consists of the Spring Cloud Contract Verifier project. Spring Cloud Contract Verifier is a tool that enables Consumer Driven Contract (CDC) development of JVM-based applications. It ensures that services (both providers and consumers) can communicate without integration issues by defining a contract between them.  


---

### **Why we have chosen Spring Cloud Contract?**

We were tasked with migrating a medium-sized, IBM WebSphere-deployed monolithic enterprise application to the Azure cloud. The migration process involved several stages, one of which was the migration of the application's front page. The UI part, already developed using React.js, required minimal intervention. For the Java component, it was decided to migrate the functionality to the [Spring Reactor project](https://spring.io/reactive). Ultimately, the Java part should become a separate REST API microservice — a resource service protected by Okta.

During the project analysis, we encountered an unpleasant discovery: a lack of tests. The team needed to find a way to quickly and efficiently implement testing. This was particularly crucial for verifying that nothing was broken in a way. 

Spring Contracts provides the ability to create integration tests from captured responses, which is a very useful feature, especially given the task we had and the time we spent. Let's explore how we adopted this approach and quickly enabled integration tests for token protected Spring Reactor microservices.

---

### How to enable Spring Contracts 

We created pet project [spring_contract_example](https://github.com/ibagroup-eu/app-modernization-examples/tree/main/spring_contract_example) to demonstrate required settings and code examples. 

#### **Understanding the Project Structure**

Project structure:

- **/src/main/java**: The main source code for the example application.
- **/src/test/java**: Contains tests including *Spring contract base tests*.
- **/src/test/resources/contracts/**: Contains contract definitions written in Groovy and captured responses in JSON format.
- **/target/generated-test-sources/contracts**: Auto-generated tests based on the contract files

#### 1. **Maven settings**

First, add required Spring Contracts libraries under dependencies section of your pom.xml file:

   ```xml
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-contract-verifier</artifactId>
			<version>${spring-cloud-contract.version}</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
			<version>${spring-cloud-contract.version}</version>
			<scope>test</scope>
		</dependency>
   ```

Under plugins section add Spring Contracts maven plugin:

   ```xml
		<!--  Spring Cloud Contract Verifier Maven plugin: -->
		<plugin>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-contract-maven-plugin</artifactId>
			<version>${spring-cloud-contract.version}</version>
			<extensions>true</extensions>
			<configuration>
				<packageWithBaseClasses>eu.ibagroup.am.springcontracts.contracts</packageWithBaseClasses>
				<testMode>EXPLICIT</testMode>
			</configuration>
			<executions>
				<execution>
					<phase>package</phase>
					<goals>
						<goal>pushStubsToScm</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
   ```
    
Key settings here are:
-  testMode attribute value should be **EXPLICIT**. That`s required settings for *Spring Reactor* projects.
-  packageWithBaseClasses should points to *base tests* parent folder inside your */src/test/java* folder.


Finally add Groovy language support:
   
   ```xml
		<plugin>
			<groupId>org.codehaus.gmavenplus</groupId>
			<artifactId>gmavenplus-plugin</artifactId>
			<version>1.13.1</version>
			<executions>
				<execution>
					<goals>
						<goal>addSources</goal>
						<goal>addTestSources</goal>
						<goal>generateStubs</goal>
						<goal>compile</goal>
						<goal>generateTestStubs</goal>
						<goal>compileTests</goal>
						<goal>removeStubs</goal>
						<goal>removeTestStubs</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
   ```

... and declare auto-generated tests as additional project source folder:

   ```xml
		<plugin>
			<groupId>org.codehaus.mojo</groupId>
			<artifactId>build-helper-maven-plugin</artifactId>
			<executions>
				<execution>
					<id>add-test-source</id>
					<phase>generate-test-sources</phase>
					<goals>
						<goal>add-test-source</goal>
					</goals>
					<configuration>
						<sources>
							<directory>
								${project.build.directory}/generated-test-sources/contracts</directory>
						</sources>
					</configuration>
				</execution>
			</executions>
		</plugin>
   ```


#### 2. **Creating contract files**

The contract file is self-descriptive. The definition begins with the `Contract.make` method, within which you can declare the expected request and response, provide the required authorization headers, and define additional validation for the output.

   ```groovy
package contracts

import org.springframework.cloud.contract.spec.Contract

  Contract.make {
	    description('Should return user contracts list')
	    name('user-contract-list-possitive')
	    request {
		      method('GET')
		      urlPath($(consumer('/api/contracts'), producer('/contracts')))
		      headers {
		      		header(authorization(), "Bearer admin")
		      }
	    }
	    response {
		      status(200)
		      headers {
		        contentType('application/json')
		      }
		      body(file('example-list.json'))
		      bodyMatchers {
		      	      jsonPath('$', byType {
		             		minOccurrence(1)
		        		})
		        		jsonPath('$[*].id', byRegex('[0-9]+').asString())
		        		jsonPath('$[*].contractNumber', byRegex('[a-zA-Z]+').asString())
		        		jsonPath('$[*].country', byRegex('[a-zA-Z]{2}').asString())
		      }
	  	}
  }
   ```

This example contract specifies that when a GET request is made to `/api/user` with `Bearer` authorization header having value `admin`, the server should respond with a 200 status code and a JSON body from the `example-list.json` file. 

Content of this file was captured from original application. That is key point in validating compatibility between the original and migrated applications.

In addition in body matchers section we can apply additional validation to the expected output, in our case ensuring that the response body contains array with at least one item and that the attributes of the items match to corresponding regular expressions.


#### 3. **Implementing contract base tests**

Contract base test classes play a crucial role in the testing cycle. Each auto-generated test extends base test class, and actually it`s a place where developers should configure tests for execution and could manage additional test resources (test containers as an example). 

Lets have a look on base test class:

   ```java
	@TestInstance(Lifecycle.PER_CLASS)
	@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "server.port=0")
	public class PositiveBase extends SpringContractsBase{
	
	    @Value("${local.server.port:8080}")
	    private int port;
	    
	    @BeforeAll
	    public void init() {
			mockDecoder();
			initRestAssured(port);
	    }
	}
   ```
As we defined Spring Contract plugin *testMode* attribute as *EXPLICIT* Rest Assured client will be used as a test client in this case.

To run *black-box like application container*: 
-  Run application on random port `webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "server.port=0"`
-  Capture randomly generated port value in local variable.
-  Configure Rest Assured client to use that port number like `RestAssured.baseURI = "http://localhost:" + port;`.  


The last issue that needs to be resolved before we can run our tests is providing as part of the API call a *valid* token, which will be challenging to obtain in our case. 

When a request is made with a token, the application delegates the task to `ReactiveJwtDecoder` to decode and validate the token. It then returns the parsed object or raises an exception if something goes wrong.

One of the easiest way to address this issue would be to mock `ReactiveJwtDecoder.decode` method:

   ```java
public abstract class SpringContractsBase {
    
    private final String corporateEmailPostfix = "@company.com";

    @MockBean
    ReactiveJwtDecoder jwtDecoder;
    
    protected void mockDecoder() {
		when(jwtDecoder.decode(any())).then(answer -> {
			Map<String, Object> claims = new HashMap<>();
			String userId = Optional.of(answer.getArgument(0, String.class)).orElse("");
			if (!userId.endsWith(corporateEmailPostfix)) {
			    userId += corporateEmailPostfix; 
			}
			claims.putAll(Collections.singletonMap("email", userId));
			Jwt jwt = new Jwt("token", 
				Instant.now(), 
				Instant.now().plusSeconds(60),
				Collections.singletonMap("key", "value"),
				claims
			);
			return Mono.just(jwt);	
		});
    }
   ```


Here, we are simply creating a user ID that will be recognized by the application. This is done by concatenating the received authorization token value with some static text.



#### **4. Generating and Running Tests**

Run the following command to generate tests from the contract files:

   ```bash
   mvn clean install
   ```
The plugin will generate test classes in the `${project.build.directory}/generated-test-sources/` These tests verify that the service adheres to the defined contract.

To execute the tests run:

```bash
mvn test    

```

### **Conclusion**

Spring contracts is a powerful tool which allows teams to detect and fix integration issues early in the development cycle
reduces integration testing overhead, even may serve as analogue of documentation. 

In this article, we demonstrate an approach how quickly with reasonable effort build integration testing to ensure compatibility between the original and migrated applications. 

In the next part of this series, we will demonstrate how to improve team collaboration using the Spring Cloud contracts.

