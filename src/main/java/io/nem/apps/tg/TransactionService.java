package io.nem.apps.tg;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.nem.core.crypto.Hash;
import org.nem.core.crypto.KeyPair;
import org.nem.core.crypto.PrivateKey;
import org.nem.core.crypto.PublicKey;
import org.nem.core.messages.SecureMessage;
import org.nem.core.model.Account;
import org.nem.core.model.HashUtils;
import org.nem.core.model.MultisigSignatureTransaction;
import org.nem.core.model.TransferTransaction;
import org.nem.core.model.ncc.NemAnnounceResult;
import org.nem.core.model.ncc.TransactionMetaDataPair;
import org.nem.core.model.primitive.Amount;
import org.nem.core.node.NodeEndpoint;
import org.nem.core.serialization.JsonSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.opendevl.JFlat;

import io.nem.apps.api.TransactionApi;
import io.nem.apps.builders.ConfigurationBuilder;
import io.nem.apps.builders.MultisigSignatureTransactionBuilder;
import io.nem.apps.builders.MultisigTransactionBuilder;
import io.nem.apps.builders.TransferTransactionBuilder;
import io.nem.apps.crypto.SecureMessageDecoder;
import io.nem.apps.crypto.SecureMessageEncoder;
import io.nem.apps.factories.AttachmentFactory;
import io.nem.apps.tg.utils.KeyConvertor;
import io.nem.apps.util.Constants;
import io.nem.apps.util.GzipUtils;
import io.nem.apps.util.HexStringUtils;
import io.nem.apps.util.JsonUtils;
import io.nem.apps.util.NetworkUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/s")
@EnableAsync
@CrossOrigin(origins = { "http://localhost:4200", "http://alvinpreyes.com", "http://arcabots.com", "*",
		"http://botmill.io" })
public class TransactionService {


	private static String host = "alice2.nem.ninja";

	@RequestMapping(method = RequestMethod.GET, path = "/address/{address}")
	public ResponseEntity<String> grabLatest(@PathVariable String address) {

		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(
				NetworkUtils.get("http://" + host + ":7890/account/transfers/all?address=" + address).getResponse());
		net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

		@SuppressWarnings("unchecked")
		Iterator itr = json.getJSONArray("data").iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		String jsonData = "";
		while (itr.hasNext()) {
			JSONObject element = (JSONObject) itr.next();

			// lastHash =
			// element.getJSONObject("meta").getJSONObject("hash").getString("data");
			jsonData += parse(address, element.toString(), 0);
		}
		jsonData += recurse(address, lastHash, 0);
		String completeData = "{\"data\":[" + jsonData.substring(0, jsonData.length() - 1) + "]}";
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);

	}

	@RequestMapping(method = RequestMethod.GET, path = "/incoming/address/{address}")
	public ResponseEntity<String> grabIncomingOnly(@PathVariable String address) {

		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(
				NetworkUtils.get("http://" + host + ":7890/account/transfers/all?address=" + address).getResponse());
		net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

		@SuppressWarnings("unchecked")
		Iterator itr = jsonArrayMeta.iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		String jsonData = "";
		while (itr.hasNext()) {
			JSONObject element = (JSONObject) itr.next();

			// lastHash =
			// element.getJSONObject("meta").getJSONObject("hash").getString("data");
			jsonData += parse(address, element.toString(), 1);
		}

		jsonData += recurse(address, lastHash, 1);
		String completeData = "{\"data\":[" + jsonData.substring(0, jsonData.length() - 1) + "]}";
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);

	}

	@RequestMapping(method = RequestMethod.GET, path = "/outgoing/address/{address}")
	public ResponseEntity<String> grabOutgoingOnly(@PathVariable String address) {

		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(
				NetworkUtils.get("http://" + host + ":7890/account/transfers/all?address=" + address).getResponse());
		net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

		@SuppressWarnings("unchecked")
		Iterator itr = jsonArrayMeta.iterator();
		String lastHash = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		String jsonData = "";
		while (itr.hasNext()) {
			JSONObject element = (JSONObject) itr.next();

			// lastHash =
			// element.getJSONObject("meta").getJSONObject("hash").getString("data");
			jsonData += parse(address, element.toString(), 2);
		}
		jsonData += recurse(address, lastHash, 2);
		String completeData = "{\"data\":[" + jsonData.substring(0, jsonData.length() - 1) + "]}";
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(completeData);

	}

	private String recurse(String address, String lastHash, int type) {
		String str = "";
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":7890/account/transfers/all?address=" + address + "&hash=" + lastHash)
				.getResponse());

		net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

		@SuppressWarnings("unchecked")
		Iterator itr = jsonArrayMeta.iterator();
		String lastHashr = "";
		if (json.getJSONArray("data").size() > 0) {
			lastHashr = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1)
					.getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		while (itr.hasNext()) {
			JSONObject element = (JSONObject) itr.next();

			// lastHashr =
			// element.getJSONObject("meta").getJSONObject("hash").getString("data");
			str += parse(address, element.toString(), type);
		}
		if (!lastHashr.equals("")) {
			str += recurse(address, lastHashr, type);
		}
		return str;
	}

	private String parse(String address, String result, int type) {

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
		JSONObject outJSON = new JSONObject();
		if (transaction.containsKey("signatures")) { // multisig transaction
			JSONObject otherTrans = transaction.getJSONObject("otherTrans");
			String recipient = otherTrans.getString("recipient");
			String sender = KeyConvertor.getAddressFromPublicKey(transaction.getString("signer"));
			outJSON.put("sender", KeyConvertor.getAddressFromPublicKey(otherTrans.getString("signer")));
			outJSON.put("recipient", recipient);

			outJSON.put("amount", Amount.fromMicroNem(otherTrans.getLong("amount")).getNumNem());
			outJSON.put("date",
					dateFormat.format(new Date((otherTrans.getLong("timeStamp") + Constants.NEMSISTIME) * 1000)));
			// outJSON.put("hash", hash);
			// message
			if (otherTrans.containsKey("message") && otherTrans.getJSONObject("message").containsKey("type")) {
				JSONObject message = otherTrans.getJSONObject("message");
				// if message type is 1, convert to String
				if (message.getInt("type") == 1 && HexStringUtils.hex2String(message.getString("payload")) != null) {
					outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
				} else {
					outJSON.put("message", "");
				}
			} else {
				outJSON.put("message", "");
			}
			// mosaic
			// if (otherTrans.containsKey("mosaics")) {
			// JSONArray outMosaicArray = new JSONArray();
			// JSONArray mosaics = otherTrans.getJSONArray("mosaics");
			// for (int i = 0; i < mosaics.size(); i++) {
			// JSONObject outMosaic = new JSONObject();
			// JSONObject mosaic = mosaics.getJSONObject(i);
			// long quantity = mosaic.getLong("quantity");
			// String namespace =
			// mosaic.getJSONObject("mosaicId").getString("namespaceId");
			// String mosaicName =
			// mosaic.getJSONObject("mosaicId").getString("name");
			// MosaicId mosaicId = new MosaicId(new NamespaceId(namespace),
			// mosaicName);
			// MosaicFeeInformation m =
			// NISQuery.findMosaicFeeInformationByNIS(mosaicId);
			// outMosaic.put("name", mosaicId.toString());
			// outMosaic.put("quantity", quantity / Math.pow(10,
			// m.getDivisibility()));
			// outMosaicArray.add(outMosaic);
			// }
			// if (outMosaicArray.size() != 0) {
			// outJSON.put("mosaics", outMosaicArray);
			// }
			// }
			outJSON.put("hash", hash);
			outJSON.put("isMultisig", "1");
			// JSONArray newj = new JSONArray();
			// outJSON.toJSONArray(newj);

			if (type == 1) { // incoming only
				if (address.equals(recipient)) {
					return outJSON.toString() + ",";
				} else {
					return "";
				}
			} else if (type == 2) {
				if (address.equals(sender)) {
					return outJSON.toString() + ",";
				} else {
					return "";
				}
			}
			return outJSON.toString() + ",";

		} else { // normal transaction
			String recipient = transaction.getString("recipient");
			String sender = KeyConvertor.getAddressFromPublicKey(transaction.getString("signer"));
			outJSON.put("sender", KeyConvertor.getAddressFromPublicKey(transaction.getString("signer")));
			outJSON.put("recipient", recipient);
			outJSON.put("amount", Amount.fromMicroNem(transaction.getLong("amount")).getNumNem());
			outJSON.put("date",
					dateFormat.format(new Date((transaction.getLong("timeStamp") + Constants.NEMSISTIME) * 1000)));
			// outJSON.put("hash", hash);
			// message
			if (transaction.containsKey("message") && transaction.getJSONObject("message").containsKey("type")) {
				JSONObject message = transaction.getJSONObject("message");
				// if message type is 1, convert to String
				if (message.getInt("type") == 1 && HexStringUtils.hex2String(message.getString("payload")) != null) {
					outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
				} else {
					outJSON.put("message", "");
				}
			} else {
				outJSON.put("message", "");
			}

			// mosaic
			// if (transaction.containsKey("mosaics")) {
			// JSONArray outMosaicArray = new JSONArray();
			// JSONArray mosaics = transaction.getJSONArray("mosaics");
			// for (int i = 0; i < mosaics.size(); i++) {
			// JSONObject outMosaic = new JSONObject();
			// JSONObject mosaic = mosaics.getJSONObject(i);
			// long quantity = mosaic.getLong("quantity");
			// String namespace =
			// mosaic.getJSONObject("mosaicId").getString("namespaceId");
			// String mosaicName =
			// mosaic.getJSONObject("mosaicId").getString("name");
			// MosaicId mosaicId = new MosaicId(new NamespaceId(namespace),
			// mosaicName);
			// MosaicFeeInformation m =
			// NISQuery.findMosaicFeeInformationByNIS(mosaicId);
			// outMosaic.put("name", mosaicId.toString());
			// outMosaic.put("quantity", quantity / Math.pow(10,
			// m.getDivisibility()));
			// outMosaicArray.add(outMosaic);
			// }
			// if (outMosaicArray.size() != 0) {
			// outJSON.put("mosaics", outMosaicArray);
			// }
			// }
			outJSON.put("hash", hash);
			outJSON.put("isMultisig", "0");

			if (type == 1) { // incoming only
				if (address.equals(recipient)) {
					return outJSON.toString() + ",";
				} else {
					return "";
				}
			} else if (type == 2) {
				if (address.equals(sender)) {
					return outJSON.toString() + ",";
				} else {
					return "";
				}
			}
			return outJSON.toString() + ",";

		}
	}

}
