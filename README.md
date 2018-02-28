# DID Auth Proxy

This is an HTTP proxy that uses [HTTP Signatures](https://www.ietf.org/id/draft-cavage-http-signatures-09.txt) based on [Decentralized Identifiers](https://w3c-ccg.github.io/did-spec/) for authenticated HTTP requests.

It can run in two modes:

### Signing mode

The DID Auth proxy signs an incoming HTTP request using a pre-configured signing key, before forwarding the request. In this mode, the DID
Auth proxy adds an `Authorization` header to the HTTP request, e.g.:

	Authorization: Signature keyId="did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI",algorithm="rsa-sha256",headers="(request-target) user-agent host accept-encoding",signature="vydQPt44ND67cn6qIzb4vVMoj3Xe7KKkoc1QXjkVeAORxLJupZGnb7EfFUKPNQWLwu0s/FJMlw3uwq1JnaXX2v8fjfjNlWcKO8RO/OL2Um7TViuLlBNmGFbliOGMjICNTXegS5q7tdnF6MoKwwtjWqkWi9o2YihZBWiwC6FxyOA3xBHqs6Xnf5VZ6MqQddFAjSLtnRuL0HwQEQ2ZkdLSxLKcOvet8siMEpTQRsFBAfxz6C/bsDiIvwzd0L2HVrv1qjzeaV+SeGBW0WXRpxuQCbcuTiKqxTogtCzu3WPDWUwrhb1ZTat+kv+umzoYCjOJKuxmOnXm4VSWDlOyqUNvfQ=="

### Verifying mode

The DID Auth proxy verifies a signature on an incoming HTTP request by discovering a DID's public key using the DIF [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/). In this mode, the DID Auth proxy looks
for the `Authorization` header above, and if successfully verified, adds a `Verified-Did` header to the HTTP request, e.g.:

	Verified-Did: did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI

It is possible to run two instances of the DID Auth proxy in a chained mode, where one instance signs an HTTP request, and the other verifies it. In this case,
it is expected that the HTTP client has a close trust relationship with a singing DID Auth proxy, and the protected target service has a close trust relationship
with a verifying DID Auth proxy.

## Typical Deployment

                            sign HTTP request using              lookup DID key using UniR
                           pre-configured DID and key            and verify HTTP signature
                                       |                                     ^
                                       v                                     |
	 _________________          ________________                      ________________          ________________
	|                 |  HTTP  |                |        HTTP        |                |  HTTP  |                |
	| HTTP Client     | -----> | Signing        | =================> | Verifying      | -----> | Protected      |
	|                 |        | DID Auth Proxy |                    | DID Auth Proxy |        | Target Service |
	|_________________|        |________________|                    |________________|        |________________|
	
	         (trust relationship)               (untrusted connection)               (trust relationship)
	         (e.g. local network)                                                    (e.g. local network) 

This shows a deployment involving two instances of the DID Auth proxy in a chained mode.

## How to build

A Docker image can be built as follows:

	docker build -f ./Dockerfile -t http_did_auth_proxy .	

## Signing mode

Required parameters:

 * `signing=1` - Enables the DID Auth proxy in signing mode.
 * `signingDid=did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI` - The DID to use for signing HTTP requests.
 * `signingKey=...` - The DID's signing key to use for singing HTTP requests.
 * `signingType=rsa` - The DID's signing key type, must be one of 'rsa', 'ed25519', 'secp256k1'.

Example #1:

	docker run -ti -p 9091:8080 -e signing=1 -e signingDid=did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI -e "signingKey=-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDcNJDewMzDRRPh\nGnwmpjOoo9DqjZRBDAcADp5bcAlLYepc53lksGi7TlXiHmPpFRWLGCQAkg8FzzVl\n5mMMHcdnVkR4TNlfLEPwFYNyBP9Fl3R6tyHfiqMyQZI2ExmUYUfQnENVo4j9Pu28\ngOhcYNQi/B7xaCm07mTqCpqGs5/HUHbKehP6XwkxV+JvlhouyPbYpJXWgAyFXAtn\nh/hu9lktgwiW0fLYMem3T9ZvX7vmY5q3r/oN/z6DS008FmyeA9CZNwxgvL69pRTi\na/0enQwfvO4PZeXAji/FcFkdBfIaKHWh+clfThWpD6aWhkKyLy55tx7NBm+LUNYe\nDkXDylnzAgMBAAECggEBAJfKxHJIhN22KFXrY9lgiAufoMuOfLu6BrlLWm29dSq5\nfEw+Y7e8wvUasqkEOerZ1dfj9C1QgXqHs09i2LPpcyMQnHIwx0aLxOkia1GVkEHw\nfSJ6SqdFcemauab8040s4cwza1cFO9EWJ9rhIUtMk+7pzIqsOtO14WpTlOF1wJYq\n4auYdInXL4gssOWEfo6ttNzwVLdQCvZs8nMTP77cTYCSilVw7aNsHBcvUUkKpiQG\n9JaTDj3IK2OwpbKzJHrOQRkRRdqCWFzB/9+Ql+FevGLNlq6gkMrlKW0UsomD3FI7\nbgIuA82iTgjF6G6RPCMFOYxM5osCBJ0LvfNH8lSNFAECgYEA9ivQPaW4hSz2/Bwl\nfFE+TEwE5gRHO4bv3WlOkqQDKbTAy7VV8i5nAKAGc+9zpO0E56K2Cyegq9dpkRI6\n4NfFKIJUd/Ee4CWUAGam7lgfNKSr4Im8dVzebhjuUAuM31yyqhF8ApA09DYwqgo1\n99KJitnC+D8QDaOSQ3O9kW41m7MCgYEA5P9ZMAiDjjiIpsFjoMtLFpCVbpZ9h4/P\nAJ0h2nhULNCR8NsqHL7c6Sm33E9But0B5dMQHpYXnm+zcPsotXKiO5wEXBCM8ZgN\np1ssDywXrArPFW6AJKmOuqJovcdoFdVl1hhssnqPs/7W6Qd55MH9smUehYeFmBWe\nxuU3PYjEKMECgYBxzVVqemcwIdZYPEbUDtbm/KmzED1B9qKC0AED55CSwj3yrnT6\nDZuOfWweQo9Kqkv/LYhM5dfwORhTeYMAmJ9Ll9ymyjBE3PprqQj43IIomwveNK6L\n7w/hA+N/26cXR0pNNuIGaVYho7+hjDDgzVLKftsUWkr4kyq1xhbX2YQs4wKBgHnb\n9F8eOLunE/kBMn4vkI66c/q8dKJ+AZ8G/yveGpUajH8KcdeILdCaFbBUMNs6nrbp\nYUuVfY4fTPMThG9CSFjGRn+jgw1RZ/qmBsUwJoyz181E10YrQLvKj0hmY9oyjBWp\nO7aih/Q16bFp/BCittmG+/38xzfOUYbwFTxWmmRBAoGAKf1heaiIbtY5R/T6/k78\nYlhEQ5E9/2MTqoPZ0FmqbzjG5rq8Q7W7ZYKjwE5DQNyzw2yD1WQL1mb+MEAali6P\nFMUmurGezZcIJSVDlt1dLzze2fpQ1CePdDwTGYDCalvedrfYgWyLiGzAaiwjajLB\nKeo2UiboLAs92t1KatoJiWY=\n-----END PRIVATE KEY-----" -e signingKeyType=rsa http_did_auth_proxy

Example #2:

	docker run -ti -p 9091:8080 -e signing=1 -e signingDid=did:sov:DavnUKB3kjn7VmVZXzEDL7 -e signingKey=2SqJFQVkR4HvqoLYwfpw6cZwxfpzdjtXj1KUCvZ7jVWEMfQnKKmgocQ4SRqXcxPy7e4irSd4vmGJoVEtQLeJDtnF -e signingKeyType=ed25519 http_did_auth_prox

## Verifying mode

Required parameters:

 * `signing=0` - Enables the DID Auth proxy in verifying mode.
 * `resolverUri=https://uniresolver.io/1.0/identifiers/` - The DID to use for signing HTTP requests.
 * `targetHost=httpbin.org:80` - The target host and port to forward requests to.

Example:

	docker run -ti -p 9092:8080 -e signing=0 -e resolverUri=https://uniresolver.io/1.0/identifiers/ -e targetHost=httpbin.org:80 http_did_auth_proxy
