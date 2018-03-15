# DID Auth HTTP proxy - Architecture

                            sign HTTP request using       lookup DID key using Universal Resolver
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

This shows a deployment involving two instances of the DID Auth HTTP proxy in a chained mode.
