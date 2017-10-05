package io.nem.apps.tg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.nem.apps.tg.service.TransactionService;

@RestController
@Scope("prototype")
@RequestMapping("/s")
@EnableAsync
@CrossOrigin(origins = { "http://localhost:4200", "http://arcabots.com" })
public class TransactionController {

	@Autowired
	private TransactionService transactionService;

	@RequestMapping(method = RequestMethod.GET, path = "/network/{network}/host/{host}/port/{port}/address/{address}")
	public ResponseEntity<String> grabLatestWithHost(@PathVariable String network, @PathVariable String host,
			@PathVariable String port, @PathVariable String address) {

		return transactionService.grabLatestWithHost(network, host, port, address);

	}

	@RequestMapping(method = RequestMethod.GET, path = "/network/{network}/host/{host}/port/{port}/address/{address}/{rec}")
	public ResponseEntity<String> grabLatestWithLimit(@PathVariable String network, @PathVariable String host,
			@PathVariable String port, @PathVariable String address, @PathVariable String rec) {
		return transactionService.grabLatestWithLimit(network, host, port, address, rec);
	}

	@RequestMapping(method = RequestMethod.GET, path = "/network/{network}/host/{host}/port/{port}/address/{address}/hash/{hash}")
	public ResponseEntity<String> grabLatestWithHash(@PathVariable String network, @PathVariable String host,
			@PathVariable String port, @PathVariable String address, @PathVariable String hash) {
		return transactionService.grabLatestWithHash(network, host, port, address, hash);

	}

	@RequestMapping(method = RequestMethod.GET, path = "/incoming/network/{network}/host/{host}/port/{port}/address/{address}")
	public ResponseEntity<String> grabIncomingOnlyWithHost(@PathVariable String network, @PathVariable String host,
			@PathVariable String port, @PathVariable String address) {

		return this.transactionService.grabIncomingOnlyWithHost(network, host, port, address);

	}

	@RequestMapping(method = RequestMethod.GET, path = "/incoming/network/{network}/host/{host}/port/{port}/address/{address}/{rec}")
	public ResponseEntity<String> grabIncomingOnlyWithHostWithLimit(@PathVariable String network,
			@PathVariable String host, @PathVariable String port, @PathVariable String address,
			@PathVariable String rec) {

		return this.transactionService.grabIncomingOnlyWithHostWithLimit(network, host, port, address, rec);

	}

	@RequestMapping(method = RequestMethod.GET, path = "/outgoing/network/{network}/host/{host}/port/{port}/address/{address}")
	public ResponseEntity<String> grabOutgoingOnlyWithHost(@PathVariable String network, @PathVariable String host,
			@PathVariable String port, @PathVariable String address) {

		return this.grabOutgoingOnlyWithHost(network, host, port, address);

	}
}
