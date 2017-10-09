package io.nem.apps.tg.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;
import org.nem.core.model.mosaic.MosaicFeeInformation;
import org.nem.core.model.mosaic.MosaicId;
import org.nem.core.model.namespace.NamespaceId;
import org.nem.core.model.primitive.Amount;
import org.nem.core.node.NodeEndpoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.nem.apps.builders.ConfigurationBuilder;
import io.nem.apps.model.NISQuery;
import io.nem.apps.tg.model.Mosaic;
import io.nem.apps.tg.model.Transaction;
import io.nem.apps.tg.model.TransactionResponse;
import io.nem.apps.tg.model.TransactionType;
import io.nem.apps.tg.utils.KeyConvertor;
import io.nem.apps.util.Constants;
import io.nem.apps.util.HexStringUtils;
import io.nem.apps.util.JsonUtils;
import io.nem.apps.util.NetworkUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

@Service
public class TransactionService {

	public ResponseEntity<String> grabLatestWithHost(String network, String host, String port, String address) {

		LinkedList<Transaction> allTransaction = new LinkedList<Transaction>();
		System.out.println("http://" + host + ":" + port + "/account/transfers/all?address=" + address);
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":" + port + "/account/transfers/all?address=" + address).getResponse());

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> itr = json.getJSONArray("data").iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		while (itr.hasNext()) {
			JSONObject element = itr.next();
			parse(host, port, address, element.toString(), allTransaction);
		}
		recurse(host, port, address, lastHash, allTransaction);

		String completeData = JsonUtils.toJson(new TransactionResponse(allTransaction));
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);
	}

	public ResponseEntity<String> grabLatestWithLimit(String network, String host, String port, String address,
			String rec) {
		int count = Integer.valueOf(rec);

		LinkedList<Transaction> allTransaction = new LinkedList<Transaction>();
		System.out.println("http://" + host + ":" + port + "/account/transfers/all?address=" + address);
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":" + port + "/account/transfers/all?address=" + address).getResponse());

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> itr = json.getJSONArray("data").iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		int parseCnt = 0;
		while (itr.hasNext()) {
			if (count == parseCnt) {
				break;
			}
			JSONObject element = itr.next();
			parse(host, port, address, element.toString(), allTransaction);
			parseCnt++;
		}
		String completeData = JsonUtils.toJson(new TransactionResponse(allTransaction));
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);
	}

	public ResponseEntity<String> grabLatestWithHash(String network, String host, String port, String address,
			String hash) {

		LinkedList<Transaction> allTransaction = new LinkedList<Transaction>();
		System.out
				.println("http://" + host + ":" + port + "/account/transfers/all?address=" + address + "&hash=" + hash);
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":" + port + "/account/transfers/all?address=" + address + "&hash=" + hash)
				.getResponse());

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> itr = json.getJSONArray("data").iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		while (itr.hasNext()) {
			JSONObject element = itr.next();
			parse(host, port, address, element.toString(), allTransaction);
		}
		String completeData = JsonUtils.toJson(new TransactionResponse(allTransaction));
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);
	}

	public ResponseEntity<String> grabIncomingOnlyWithHost(String network, String host, String port, String address) {

		LinkedList<Transaction> allTransaction = new LinkedList<Transaction>();
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":" + port + "/account/transfers/all?address=" + address).getResponse());
		net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> itr = jsonArrayMeta.iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		while (itr.hasNext()) {
			JSONObject element = itr.next();
			parse(host, port, address, element.toString(), allTransaction);
		}

		recurse(host, port, address, lastHash, allTransaction);
		String completeData = JsonUtils.toJson(new TransactionResponse(
				allTransaction.stream().filter(type -> TransactionType.INCOMING.equals(type.getTransactionType()))
						.collect(Collectors.toList())));
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);

	}

	public ResponseEntity<String> grabIncomingOnlyWithHostWithLimit(String network, String host, String port,
			String address, String rec) {

		LinkedList<Transaction> allTransaction = new LinkedList<Transaction>();
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":" + port + "/account/transfers/all?address=" + address).getResponse());
		net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> itr = jsonArrayMeta.iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		while (itr.hasNext()) {
			JSONObject element = itr.next();
			parse(host, port, address, element.toString(), allTransaction);
		}

		recurse(host, port, address, lastHash, allTransaction);
		String completeData = JsonUtils.toJson(new TransactionResponse(
				allTransaction.stream().filter(type -> TransactionType.INCOMING.equals(type.getTransactionType()))
						.collect(Collectors.toList())));
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);

	}

	public ResponseEntity<String> grabOutgoingOnlyWithHost(String network, String host, String port, String address) {

		LinkedList<Transaction> allTransaction = new LinkedList<Transaction>();
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":" + port + "/account/transfers/all?address=" + address).getResponse());
		net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> itr = jsonArrayMeta.iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		while (itr.hasNext()) {
			JSONObject element = itr.next();
			parse(host, port, address, element.toString(), allTransaction);
		}
		recurse(host, port, address, lastHash, allTransaction);
		String completeData = JsonUtils.toJson(new TransactionResponse(
				allTransaction.stream().filter(type -> TransactionType.OUTGOING.equals(type.getTransactionType()))
						.collect(Collectors.toList())));
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);

	}

	private String recurse(String host, String port, String address, String lastHash,
			LinkedList<Transaction> allTransaction) {
		String str = "";
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":" + port + "/account/transfers/all?address=" + address + "&hash=" + lastHash)
				.getResponse());

		try {
			net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

			@SuppressWarnings("unchecked")
			Iterator<JSONObject> itr = jsonArrayMeta.iterator();
			String lastHashr = "";
			if (json.getJSONArray("data").size() > 0) {
				lastHashr = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
						.getJSONObject("meta").getJSONObject("hash").getString("data");
			}
			while (itr.hasNext()) {
				JSONObject element = itr.next();
				str += parse(host, port, address, element.toString(), allTransaction);
			}
			if (!lastHashr.equals("")) {
				str += recurse(host, port, address, lastHashr, allTransaction);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return str;
	}

	private String parse(String host, String port, String address, String result,
			LinkedList<Transaction> allTransaction) {
		Transaction transactionModel = new Transaction();
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(result);
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String hash =
		String hash = json.getJSONObject("meta").getJSONObject("hash").getString("data");
		JSONObject transaction = json.getJSONObject("transaction");
		if (transaction.containsKey("signatures")) { // multisig transaction
			boolean hasMosaic = false;
			JSONArray signatures = transaction.getJSONArray("signatures");
			JSONObject otherTrans = transaction.getJSONObject("otherTrans");
			String recipient = "";
			if (otherTrans.containsKey("recipient")) {
				recipient = otherTrans.getString("recipient");
			}

			String sender = KeyConvertor.getAddressFromPublicKey(otherTrans.getString("signer"));

			double amount = 0l;
			double amountMicroNem = 0l;
			if (otherTrans.containsKey("amount")) {
				amountMicroNem = Amount.fromMicroNem(otherTrans.getLong("amount")).getNumMicroNem();
				amount = Amount.fromMicroNem(otherTrans.getLong("amount")).getNumMicroNem() / Math.pow(10, 6);
			}

			transactionModel.setRecipient(recipient);
			transactionModel.setSender(KeyConvertor.getAddressFromPublicKey(otherTrans.getString("signer")));

			transactionModel.setDate(
					dateFormat.format(new Date((otherTrans.getLong("timeStamp") + Constants.NEMSISTIME) * 1000)));

			double fee = 0l;
			double feeMicroNem = 0l;
			double transFee = 0l;
			double transFeeMicroNem = 0l;
			if (otherTrans.containsKey("fee")) {
				feeMicroNem = Amount.fromMicroNem(otherTrans.getLong("fee")).getNumMicroNem();
				fee = Amount.fromMicroNem(otherTrans.getLong("fee")).getNumMicroNem() / Math.pow(10, 6);

				// check Transaction Fee and add to the fee.
				if (transaction.containsKey("fee")) {
					transFeeMicroNem = Amount.fromMicroNem(transaction.getLong("fee")).getNumMicroNem();
					transFee = Amount.fromMicroNem(transaction.getLong("fee")).getNumMicroNem() / Math.pow(10, 6);
				}
			}

			// loop thru signatures
			Iterator<JSONObject> signaturesIter = signatures.iterator();

			double signatureFees = 0l;
			while (signaturesIter.hasNext()) {

				JSONObject signatureObject = (JSONObject) signaturesIter.next();
				// get the fee.
				signatureFees += Amount.fromMicroNem(signatureObject.getLong("fee")).getNumMicroNem();
			}

			// outJSON.put("hash", hash);
			// message
			if (otherTrans.containsKey("message") && otherTrans.getJSONObject("message").containsKey("type")) {
				JSONObject message = otherTrans.getJSONObject("message");
				// if message type is 1, convert to String
				if (message.getInt("type") == 1 && HexStringUtils.hex2String(message.getString("payload")) != null) {
					transactionModel.setMessage(HexStringUtils.hex2String(message.getString("payload")));
				}
			}
			transactionModel.setCurrencyType("nem:xem");
			transactionModel.setHash(hash);
			transactionModel.setIsMultisig("1");

			double totalFee = (feeMicroNem + transFeeMicroNem + signatureFees);
			double amountAfterFees = amountMicroNem + totalFee;
			if (address.equals(recipient)) {
				transactionModel.setTransactionType(TransactionType.INCOMING);
				transactionModel.setAmount(amountMicroNem);
				transactionModel.setAmountTotal(amountMicroNem);
			}
			if (address.equals(sender)) {
				transactionModel.setFee(totalFee);
				transactionModel.setTransactionType(TransactionType.OUTGOING);
				transactionModel.setAmount(amountAfterFees);
				if (amountAfterFees != 0.0) {
					if (amountAfterFees < totalFee) {
						transactionModel.setAmountTotal(((totalFee - amountAfterFees)));
					} else {
						transactionModel.setAmountTotal(((amountAfterFees - totalFee)));
					}
				}

			}
			// mosaic
			if (otherTrans.containsKey("mosaics")) {

				JSONArray outMosaicArray = new JSONArray();
				JSONArray mosaics = otherTrans.getJSONArray("mosaics");
				for (int i = 0; i < mosaics.size(); i++) {
					Transaction transactionModelMosaic = null;
					try {
						transactionModelMosaic = (Transaction) transactionModel.clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}

					Mosaic mosaicModel = new Mosaic();
					JSONObject outMosaic = new JSONObject();
					JSONObject mosaic = mosaics.getJSONObject(i);
					double quantity = mosaic.getDouble("quantity");
					String namespace = mosaic.getJSONObject("mosaicId").getString("namespaceId");
					String mosaicName = mosaic.getJSONObject("mosaicId").getString("name");
					MosaicId mosaicId = new MosaicId(new NamespaceId(namespace), mosaicName);

					MosaicFeeInformation m = NISQuery.findMosaicFeeInformationByNIS(host, port, mosaicId);
					int divisibility = 6;
					if (m != null) {
						divisibility = m.getDivisibility();
					}

					outMosaic.put("name", mosaicId.toString());
					outMosaic.put("quantity", quantity / Math.pow(10, divisibility));
					mosaicModel.setName(mosaicId.toString());
					mosaicModel.setQuantity(quantity);
					mosaicModel.setDivisibility(divisibility);
					transactionModelMosaic.setAmount(quantity);
					transactionModelMosaic.setCurrencyType(mosaicId.toString());
					transactionModel.addMosaic(mosaicModel);
					outMosaicArray.add(outMosaic);
					allTransaction.add(transactionModelMosaic);
				}
				if (outMosaicArray.size() != 0) {
					hasMosaic = true;
					return "";
				}
			}

			// JSONArray newj = new JSONArray();
			// outJSON.toJSONArray(newj);

			allTransaction.add(transactionModel);
			return "";

		} else { // normal transaction
			boolean hasMosaic = false;
			String recipient = "";
			if (transaction.containsKey("recipient")) {
				recipient = transaction.getString("recipient");
			}

			String sender = KeyConvertor.getAddressFromPublicKey(transaction.getString("signer"));
			double amount = 0l;
			double amountMicroNem = 0l;
			if (transaction.containsKey("amount")) {
				amountMicroNem = Amount.fromMicroNem(transaction.getLong("amount")).getNumMicroNem();
				amount = Amount.fromMicroNem(transaction.getLong("amount")).getNumMicroNem() / Math.pow(10, 6);
			}
			double fee = 0l;
			double feeMicroNem = 0l;
			if (transaction.containsKey("fee")) {
				feeMicroNem = Amount.fromMicroNem(transaction.getLong("fee")).getNumMicroNem();
				fee = Amount.fromMicroNem(transaction.getLong("fee")).getNumMicroNem() / Math.pow(10, 6);
			}

			transactionModel.setRecipient(recipient);
			transactionModel.setSender(KeyConvertor.getAddressFromPublicKey(transaction.getString("signer")));

			transactionModel.setDate(
					dateFormat.format(new Date((transaction.getLong("timeStamp") + Constants.NEMSISTIME) * 1000)));

			// outJSON.put("hash", hash);
			// message
			if (transaction.containsKey("message") && transaction.getJSONObject("message").containsKey("type")) {
				JSONObject message = transaction.getJSONObject("message");
				// if message type is 1, convert to String
				if (message.getInt("type") == 1 && HexStringUtils.hex2String(message.getString("payload")) != null) {
					transactionModel.setMessage(HexStringUtils.hex2String(message.getString("payload")));
				}
			}
			transactionModel.setCurrencyType("nem:xem");
			transactionModel.setHash(hash);
			transactionModel.setIsMultisig("0");

			double amountAfterFees = amountMicroNem + feeMicroNem;

			if (address.equals(recipient)) {
				transactionModel.setTransactionType(TransactionType.INCOMING);
				transactionModel.setAmount(((amountMicroNem)));
				transactionModel.setAmountTotal(amountMicroNem);
			}
			if (address.equals(sender)) {
				transactionModel.setFee(feeMicroNem);
				transactionModel.setTransactionType(TransactionType.OUTGOING);
				transactionModel.setAmount(amountAfterFees);

				if (amountAfterFees != 0.0) {
					if (amountAfterFees < feeMicroNem) {
						transactionModel.setAmountTotal(((feeMicroNem - amountAfterFees)));
					} else {
						transactionModel.setAmountTotal(((amountAfterFees - feeMicroNem)));
					}
				}

			}
			// mosaic
			if (transaction.containsKey("mosaics")) {

				JSONArray outMosaicArray = new JSONArray();
				JSONArray mosaics = transaction.getJSONArray("mosaics");
				for (int i = 0; i < mosaics.size(); i++) {
					Transaction transactionModelMosaic = null;
					try {
						transactionModelMosaic = (Transaction) transactionModel.clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}

					Mosaic mosaicModel = new Mosaic();
					JSONObject outMosaic = new JSONObject();
					JSONObject mosaic = mosaics.getJSONObject(i);
					double quantity = mosaic.getDouble("quantity");
					String namespace = mosaic.getJSONObject("mosaicId").getString("namespaceId");
					String mosaicName = mosaic.getJSONObject("mosaicId").getString("name");
					MosaicId mosaicId = new MosaicId(new NamespaceId(namespace), mosaicName);
					MosaicFeeInformation m = NISQuery.findMosaicFeeInformationByNIS(host, port, mosaicId);
					int divisibility = 6;
					if (m != null) {
						divisibility = m.getDivisibility();
					}
					outMosaic.put("name", mosaicId.toString());
					outMosaic.put("quantity", quantity / Math.pow(10, divisibility));
					mosaicModel.setName(mosaicId.toString());
					mosaicModel.setQuantity(quantity);
					mosaicModel.setDivisibility(divisibility);
					transactionModelMosaic.setAmount(quantity);
					transactionModelMosaic.setCurrencyType(mosaicId.toString());
					transactionModel.addMosaic(mosaicModel);
					outMosaicArray.add(outMosaic);
					allTransaction.add(transactionModelMosaic);
				}
				if (outMosaicArray.size() != 0) {
					hasMosaic = true;
					return "";
				}
			}

			allTransaction.add(transactionModel);
			return "";

		}
	}

}
