# DID Auth HTTP proxy - Modes

The DID Auth HTTP proxy can run in two modes:

## Signing mode

The DID Auth proxy signs an incoming HTTP request using a pre-configured signing key, before forwarding the request. In this mode, the DID
Auth proxy adds an `Authorization` header to the HTTP request, e.g.:

	Authorization: Signature keyId="did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI",algorithm="rsa-sha256",headers="(request-target) user-agent host accept-encoding",signature="vydQPt44ND67cn6qIzb4vVMoj3Xe7KKkoc1QXjkVeAORxLJupZGnb7EfFUKPNQWLwu0s/FJMlw3uwq1JnaXX2v8fjfjNlWcKO8RO/OL2Um7TViuLlBNmGFbliOGMjICNTXegS5q7tdnF6MoKwwtjWqkWi9o2YihZBWiwC6FxyOA3xBHqs6Xnf5VZ6MqQddFAjSLtnRuL0HwQEQ2ZkdLSxLKcOvet8siMEpTQRsFBAfxz6C/bsDiIvwzd0L2HVrv1qjzeaV+SeGBW0WXRpxuQCbcuTiKqxTogtCzu3WPDWUwrhb1ZTat+kv+umzoYCjOJKuxmOnXm4VSWDlOyqUNvfQ=="

## Verifying mode

The DID Auth proxy verifies a signature on an incoming HTTP request by discovering a DID's public key using the DIF [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/). In this mode, the DID Auth proxy looks
for the `Authorization` header above, and if successfully verified, adds a `Verified-Did` header to the HTTP request, e.g.:

	Verified-Did: did:v1:test:nym:rZdPg5VF6SqrVuEYEHAuDaeikkA2D8QBLRJQRnhz3pI

It is possible to run two instances of the DID Auth proxy in a chained mode, where one instance signs an HTTP request, and the other verifies it. In this case,
it is expected that the HTTP client has a close trust relationship with a singing DID Auth proxy, and the protected target service has a close trust relationship
with a verifying DID Auth proxy.
