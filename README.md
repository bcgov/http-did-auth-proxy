
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)


# DID Auth HTTP Proxy

This is a DID Auth HTTP proxy that uses [HTTP Signatures](https://www.ietf.org/id/draft-cavage-http-signatures-09.txt) based on [Decentralized Identifiers](https://w3c-ccg.github.io/did-spec/) for authenticated HTTP requests.

## Technology Stack Used

 * [Decentralized Identifiers (DIDs)](https://w3c-ccg.github.io/did-spec/)
 * [DID Auth](https://github.com/WebOfTrustInfo/rebooting-the-web-of-trust-spring2018/blob/master/draft-documents/did_auth_draft.md)
 * [HTTP Signatures](https://www.ietf.org/id/draft-cavage-http-signatures-09.txt)

## Third-Party Products/Libraries used and the License they are covert by

 * [LittleProxy](https://github.com/adamfisk/LittleProxy) - Apache-2.0
 * [http-signatures-java](https://github.com/tomitribe/http-signatures-java) - Apache-2.0
 * [bitcoinj](https://github.com/bitcoinj/bitcoinj/) - Apache-2.0
 * [kalium](https://github.com/abstractj/kalium/) - Apache-2.0
 * [universal-resolver-java](https://github.com/decentralized-identity/universal-resolver/tree/master/implementations/java) - Apache-2.0

## Project Status

Under development for a [BCDevExchange opportunity](https://bcdevexchange.org/opportunities/opp-initial-reference-implementation-of-decentralized-authentication--did-auth--and-authorization-mechanisms).

All repositories related to this project:

 * https://github.com/bcgov/did-auth-extension - A DID Auth browser add-on
 * https://github.com/bcgov/did-auth-relying-party - A DID Auth relying party
 * https://github.com/bcgov/http-did-auth-proxy - A DID Auth HTTP proxy **(this repository)**

## Documentation

Architecture: [architecture.md](./docs/architecture.md)

Signing and verifying mode: [modes.md](./docs/modes.md)

## Deployment (Local Development)

Build with Docker:

	docker build -f ./Dockerfile -t http_did_auth_proxy .	

### Run in signing mode

Required parameters:

 * `signing=1` - Enables the DID Auth proxy in signing mode.
 * `signingDid=did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI` - The DID to use for signing HTTP requests.
 * `signingKey=...` - The DID's signing key to use for singing HTTP requests.
 * `signingType=rsa` - The DID's signing key type, must be one of 'rsa', 'ed25519', 'secp256k1'.

Example #1:

	docker run -ti -p 9091:8080 -e signing=1 -e signingDid=did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI -e "signingKey=-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDcNJDewMzDRRPh\nGnwmpjOoo9DqjZRBDAcADp5bcAlLYepc53lksGi7TlXiHmPpFRWLGCQAkg8FzzVl\n5mMMHcdnVkR4TNlfLEPwFYNyBP9Fl3R6tyHfiqMyQZI2ExmUYUfQnENVo4j9Pu28\ngOhcYNQi/B7xaCm07mTqCpqGs5/HUHbKehP6XwkxV+JvlhouyPbYpJXWgAyFXAtn\nh/hu9lktgwiW0fLYMem3T9ZvX7vmY5q3r/oN/z6DS008FmyeA9CZNwxgvL69pRTi\na/0enQwfvO4PZeXAji/FcFkdBfIaKHWh+clfThWpD6aWhkKyLy55tx7NBm+LUNYe\nDkXDylnzAgMBAAECggEBAJfKxHJIhN22KFXrY9lgiAufoMuOfLu6BrlLWm29dSq5\nfEw+Y7e8wvUasqkEOerZ1dfj9C1QgXqHs09i2LPpcyMQnHIwx0aLxOkia1GVkEHw\nfSJ6SqdFcemauab8040s4cwza1cFO9EWJ9rhIUtMk+7pzIqsOtO14WpTlOF1wJYq\n4auYdInXL4gssOWEfo6ttNzwVLdQCvZs8nMTP77cTYCSilVw7aNsHBcvUUkKpiQG\n9JaTDj3IK2OwpbKzJHrOQRkRRdqCWFzB/9+Ql+FevGLNlq6gkMrlKW0UsomD3FI7\nbgIuA82iTgjF6G6RPCMFOYxM5osCBJ0LvfNH8lSNFAECgYEA9ivQPaW4hSz2/Bwl\nfFE+TEwE5gRHO4bv3WlOkqQDKbTAy7VV8i5nAKAGc+9zpO0E56K2Cyegq9dpkRI6\n4NfFKIJUd/Ee4CWUAGam7lgfNKSr4Im8dVzebhjuUAuM31yyqhF8ApA09DYwqgo1\n99KJitnC+D8QDaOSQ3O9kW41m7MCgYEA5P9ZMAiDjjiIpsFjoMtLFpCVbpZ9h4/P\nAJ0h2nhULNCR8NsqHL7c6Sm33E9But0B5dMQHpYXnm+zcPsotXKiO5wEXBCM8ZgN\np1ssDywXrArPFW6AJKmOuqJovcdoFdVl1hhssnqPs/7W6Qd55MH9smUehYeFmBWe\nxuU3PYjEKMECgYBxzVVqemcwIdZYPEbUDtbm/KmzED1B9qKC0AED55CSwj3yrnT6\nDZuOfWweQo9Kqkv/LYhM5dfwORhTeYMAmJ9Ll9ymyjBE3PprqQj43IIomwveNK6L\n7w/hA+N/26cXR0pNNuIGaVYho7+hjDDgzVLKftsUWkr4kyq1xhbX2YQs4wKBgHnb\n9F8eOLunE/kBMn4vkI66c/q8dKJ+AZ8G/yveGpUajH8KcdeILdCaFbBUMNs6nrbp\nYUuVfY4fTPMThG9CSFjGRn+jgw1RZ/qmBsUwJoyz181E10YrQLvKj0hmY9oyjBWp\nO7aih/Q16bFp/BCittmG+/38xzfOUYbwFTxWmmRBAoGAKf1heaiIbtY5R/T6/k78\nYlhEQ5E9/2MTqoPZ0FmqbzjG5rq8Q7W7ZYKjwE5DQNyzw2yD1WQL1mb+MEAali6P\nFMUmurGezZcIJSVDlt1dLzze2fpQ1CePdDwTGYDCalvedrfYgWyLiGzAaiwjajLB\nKeo2UiboLAs92t1KatoJiWY=\n-----END PRIVATE KEY-----" -e signingKeyType=rsa http_did_auth_proxy

Example #2:

	docker run -ti -p 9091:8080 -e signing=1 -e signingDid=did:sov:DavnUKB3kjn7VmVZXzEDL7 -e signingKey=2SqJFQVkR4HvqoLYwfpw6cZwxfpzdjtXj1KUCvZ7jVWEMfQnKKmgocQ4SRqXcxPy7e4irSd4vmGJoVEtQLeJDtnF -e signingKeyType=ed25519 http_did_auth_proxy

### Run in verifying mode

Required parameters:

 * `signing=0` - Enables the DID Auth proxy in verifying mode.
 * `resolverUri=https://uniresolver.io/1.0/identifiers/` - The URL of the Universal Resolved used to resolve DIDs.
 * `targetHost=httpbin.org:80` - The target host and port to forward requests to.
 * `whitelist=did:sov:DavnUKB3kjn7VmVZXzEDL7,did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI` - Optional whitelist of DIDs from which to accept signed requests.

Example:

	docker run -ti -p 9092:8080 -e signing=0 -e resolverUri=https://uniresolver.io/1.0/identifiers/ -e targetHost=httpbin.org:80 http_did_auth_proxy

### How to test

You can use a `curl` command that will make a call to the proxy in verifying mode, passed through the proxy in signing mode:

	curl http://192.168.200.1:9092/get --proxy 192.168.200.1:9091

(Replace 192.168.200.1 with your actual IP address that is reachable by Docker containers)

## Getting Help or Reporting an Issue

To report bugs/issues/feature requests, please file an [issue](../../issues).

## How to Contribute

If you would like to contribute, please see our [CONTRIBUTING](./CONTRIBUTING.md) guidelines.

Please note that this project is released with a [Contributor Code of Conduct](./CODE_OF_CONDUCT.md). 
By participating in this project you agree to abide by its terms.

## License

    Copyright 2018 Province of British Columbia

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
