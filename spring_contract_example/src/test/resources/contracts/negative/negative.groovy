package contracts

import org.springframework.cloud.contract.spec.Contract

  Contract.make {
    description('Should return user contracts list')
    name('user-contract-list-possitive')
    request {
      method('GET')
      urlPath($(consumer('/api/contracts'), producer('/contracts')))
      headers {
         header(authorization(), "Bearer not-existing-user")
      }
    }
    response {
      status(401)
    }
  }