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